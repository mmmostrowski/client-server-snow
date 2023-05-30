import * as React from "react";
import {
    createContext,
    PropsWithChildren,
    Reducer,
    useCallback,
    useContext,
    useEffect,
    useReducer,
    useRef,
    useState
} from "react";
import {validateNumberBetween, validateSessionId} from './sessionValidator';
import {applicationConfig} from "../config/application";
import {SnowAnimationController} from "./SnowAnimationController";

export const SessionsContext = createContext([]);
export const SessionsDispatchContext = createContext(null);

export type Session = ValidatedSession & SessionExtras;

interface ValidatedSession extends DraftSession, SessionErrors {
    validatedSessionId: string,
    validatedWidth: number,
    validatedHeight: number,
    validatedFps: number,
}

export interface DraftSession {
    sessionId: string,
    presetName: string,
    width: string,
    height: string,
    fps: string,
    animationProgressRef: { current: number },
    bufferLevelRef: { current: number },
    status: SessionStatus,
    errorMsg: string|null,

    foundPresetName: string|null,
    foundWidth: number|null,
    foundHeight: number|null,
    foundFps: number|null,

    snowController: SnowAnimationController
}

interface SessionErrors {
    sessionIdError: string|null,
    widthError: string|null,
    heightError: string|null,
    fpsError: string|null,
}

interface SessionExtras {
    isSessionExists: boolean|null,
    isStopped: boolean,
    isInitializing: boolean;
    hasError: boolean,
    hasSessionIdError: boolean,
    hasConfigError: boolean;
    cannotStartSession: boolean,
}

export type SessionStatus =
        | "stopped-not-checked"
        | "stopped-not-found"
        | "stopped-found"
        | "buffering"
        | "playing"
        | "initializing-new"
        | "initializing-existing"
        | "checking"
        | SessionErrorStatus;

export type SessionErrorStatus =
        | "error"
        | "error-cannot-start-new"
        | "error-cannot-start-existing"
        | "error-cannot-stop";


type DispatchAction =
    | DispatchNewAction
    | DispatchChangeAction
    | DispatchDeleteAction

interface DispatchNewAction {
    type: string,
    newSessionId: string,
}

interface DispatchChangeAction {
    type: string,
    sessionIdx : number,
    changes: Partial<DraftSession>,
}

interface DispatchDeleteAction {
    type: string,
    sessionIdx : number,
}

type DispatchActionWithoutSessionIdx = DistributiveOmit<DispatchAction, "sessionIdx">;

type DistributiveOmit<T, K extends keyof any> = T extends any
  ? Omit<T, K>
  : never;


interface SessionsProviderProps {
    initialSessionId: string,
}

export function SessionsProvider({ initialSessionId, children }: PropsWithChildren<SessionsProviderProps>): JSX.Element {
    const [ sessions, dispatch ] = useReducer<Reducer<Session[], DispatchAction>>(
        sessionsReducer,
        [ createNewSession(initialSessionId) ]
    );

    return (
        <SessionsContext.Provider value={sessions} >
            <SessionsDispatchContext.Provider value={dispatch} >
                {children}
            </SessionsDispatchContext.Provider>
        </SessionsContext.Provider>
    );
}

export function useSessions(): Session[] {
    return useContext(SessionsContext);
}

export function useSession(sessionIdx : number): Session {
    const sessions = useSessions();
    return sessions[sessionIdx];
}

export function useSessionsDispatch(): (action: DispatchAction) => void {
    return useContext(SessionsDispatchContext);
}

export function useSessionDispatch(sessionIdx : number): (action: DispatchActionWithoutSessionIdx) => void {
    const dispatch = useSessionsDispatch();

    return useCallback(
        (action: DispatchActionWithoutSessionIdx) => dispatch({ ...action, sessionIdx })
    , [ dispatch, sessionIdx ]);
}

export function useDelayedSession(sessionIdx: number, delayMs: number = 70): Session {
    const targetSession = useSession(sessionIdx);
    const [ currentSession, setCurrentSession ] = useState(targetSession);
    const isWaitingRef = useRef<boolean>(false);
    const targetSessionRef = useRef<Session>();

    targetSessionRef.current = targetSession;

    useEffect(() => {
        if (isWaitingRef.current) {
            return;
        }
        isWaitingRef.current = true;
        setTimeout(() => {
            setCurrentSession(targetSessionRef.current);
            isWaitingRef.current = false;
        }, delayMs);
    }, [ targetSession, delayMs ]);

    return currentSession;
}

function sessionsReducer(sessions: Session[], action: DispatchAction): Session[] {
    switch(action.type) {
       case 'new-session':
            const newSessionAction = action as DispatchNewAction;
            return [
               ...sessions,
               createNewSession(newSessionAction.newSessionId),
           ];

        case 'session-changed':
            const sessionIdChangeAction = action as DispatchChangeAction;
            const idx = sessionIdChangeAction.sessionIdx;
            const previous: Session = sessions[idx];
            const changes: DraftSession = {
                ...previous,
                ...sessionIdChangeAction.changes,
            }
            const updatedSession = updateSession(previous, changes);

            // console.log("changes:", sessionIdChangeAction.changes, updatedSession);

            return [
               ...sessions.slice(0, idx),
                updatedSession,
               ...sessions.slice(idx + 1),
           ];

       case 'delete-session':
            const deleteSessionAction = action as DispatchDeleteAction;
            return [
               ...sessions.slice(0, deleteSessionAction.sessionIdx),
               ...sessions.slice(deleteSessionAction.sessionIdx + 1),
           ];

       case 'accept-or-reject-session-changes':
            return sessions.map(applyChangesToSession);

    }

    throw Error("Unknown action type: " + action.type)
}

function createNewSession(sessionId: string): Session {
    return commitChangesToSession({
        sessionId : sessionId,
        snowController: new SnowAnimationController(sessionId),

        presetName: applicationConfig.defaultPreset,
        width: '' + applicationConfig.defaultWidth,
        height: '' + applicationConfig.defaultHeight,
        fps: '' + applicationConfig.defaultFps,

        status: "stopped-not-checked",
        animationProgressRef: { current: 0 },
        bufferLevelRef: { current: 0 },
        errorMsg: "",

        foundPresetName: null,
        foundWidth: null,
        foundHeight: null,
        foundFps: null,
    });
}

function updateSession(session: ValidatedSession, changes: DraftSession): Session {
    return sessionPostprocessing({
        ...session,
        ...changes,
        ...( sessionErrors(changes) ),
    });
}

function sessionErrors(draft: DraftSession): SessionErrors {
    return {
       sessionIdError: validateSessionId(draft.sessionId),
       widthError: validateNumberBetween(draft.width, applicationConfig.minWidth, applicationConfig.maxWidth),
       heightError: validateNumberBetween(draft.height, applicationConfig.minHeight, applicationConfig.maxHeight),
       fpsError: validateNumberBetween(draft.fps, applicationConfig.minFps, applicationConfig.maxFps),
   };
}

function applyChangesToSession(validatedSession: ValidatedSession): Session {
    const numOfErrors = Object.values(sessionErrors(validatedSession))
         .filter( value => value !== null )
         .length;

    return numOfErrors === 0
        ? commitChangesToSession(validatedSession)
        : revertChangesFromSession(validatedSession);
}

function commitChangesToSession(draft: DraftSession): Session {
    return sessionPostprocessing({
        ...draft,
        ...( sessionErrors(draft) ),
        validatedSessionId: draft.sessionId,
        validatedWidth: parseInt(draft.width),
        validatedHeight: parseInt(draft.height),
        validatedFps: parseInt(draft.fps),
    });
}

function revertChangesFromSession(session: ValidatedSession): Session {
    const revertedSession: ValidatedSession = {
        ...session,
        sessionId: session.validatedSessionId,
        width: '' + session.validatedWidth,
        height: '' + session.validatedHeight,
        fps: '' + session.validatedFps,
    };
    return sessionPostprocessing({
        ...revertedSession,
        ...( sessionErrors(revertedSession) ),
    });
}

function sessionPostprocessing(session: ValidatedSession): Session {
    return {
        ...session,
        hasSessionIdError: session.sessionIdError !== null,
        animationProgressRef: session.status === 'playing' ? session.animationProgressRef : { current: 0 },
        hasConfigError:
               session.sessionIdError !== null
            || session.widthError !== null
            || session.heightError !== null
            || session.fpsError !== null
        ,
        isInitializing:
               session.status === 'initializing-new'
            || session.status === 'initializing-existing',
        isSessionExists:
               session.status === 'stopped-found'
            || session.status === 'error-cannot-start-existing'
            || session.status === 'initializing-existing',
        cannotStartSession:
               session.status === 'error-cannot-start-new'
            || session.status === 'error-cannot-start-existing',
        hasError:
               session.status === 'error'
            || session.status === 'error-cannot-start-new'
            || session.status === 'error-cannot-start-existing'
            || session.status === 'error-cannot-stop',
        isStopped:
               session.status === 'stopped-not-checked'
            || session.status === 'stopped-not-found'
            || session.status === 'stopped-found'
    };
}
