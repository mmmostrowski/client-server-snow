import * as React from "react";
import { useContext, createContext, useReducer, useState, useEffect } from 'react';
import { validateSnowSessionId, validateNumberBetween } from './snowSessionValidator';

export const SnowSessionsContext = createContext([]);
export const SnowSessionsDispatchContext = createContext(null);

export const snowConstraints = {
    defaultSessionId: "session-abc",

    minWidth: 40,
    minHeight: 20,
    defaultWidth: 120,
    defaultHeight: 60,
    maxWidth: 200,
    maxHeight: 150,

    minFps: 1,
    defaultFps: 25,
    maxFps: 66,

    defaultPreset: "massiveSnow",
    presets: {
        classical: "Classical",
        massiveSnow: "Massive Snow",
        calm: "Calm",
        windy: "Windy",
        snowy: "Snowy",
        noSnow: "No snow",
    },
};

interface SnowSession {
    sessionId: string,
    presetName: string,
    width: string,
    height: string,
    fps: string,
    animationProgress: number,
    bufferLevel: number,
    status: "stopped"|"buffering"|"playing"|"error"|"initializing"|"checking"|"found",
    errorMsg: string|null,

    foundPresetName: string|null,
    foundWidth: number|null,
    foundHeight: number|null,
    foundFps: number|null,
}

export interface ValidatedSnowSession extends SnowSession {
    validatedSessionId: string,
    sessionIdError: string|null,

    validatedWidth: number,
    widthError: string|null,

    validatedHeight: number,
    heightError: string|null,

    validatedFps: number,
    fpsError: string|null,
}

type SnowSessionDispatchAction =
    | SnowSessionDispatchSessionIdChangeAction
    | SnowSessionDispatchNewSessionAction
    | SnowSessionDispatchDeleteSessionAction

interface SnowSessionDispatchAnyAction {
    type: string,
}

interface SnowSessionDispatchSessionIdChangeAction extends SnowSessionDispatchAnyAction {
    sessionIdx : number,
    changes: object,
}

interface SnowSessionDispatchNewSessionAction extends SnowSessionDispatchAnyAction {
    newSessionId: string,
}

interface SnowSessionDispatchDeleteSessionAction extends SnowSessionDispatchAnyAction {
    sessionIdx : number,
}

export function SnowSessionsProvider({ children } : any) {
    const [ sessions, dispatch ] = useReducer(snowSessionsReducer, [
        createSession(snowConstraints.defaultSessionId),
    ]);

    return (
        <SnowSessionsContext.Provider value={sessions} >
            <SnowSessionsDispatchContext.Provider value={dispatch} >
                {children}
            </SnowSessionsDispatchContext.Provider>
        </SnowSessionsContext.Provider>
    );
}

export function useSnowSessions(): ValidatedSnowSession[] {
    return useContext(SnowSessionsContext);
}

export function useSnowSession(sessionIdx : number): ValidatedSnowSession {
    const sessions = useSnowSessions();
    return sessions[sessionIdx];
}

export function useSnowSessionsDispatch() : (action : SnowSessionDispatchAction) => void {
    return useContext(SnowSessionsDispatchContext);
}

export function useSnowSessionDispatch(sessionIdx : number) {
    const dispatch = useSnowSessionsDispatch();
    return (props : any) => dispatch({ ...props, sessionIdx: sessionIdx })
}

export function useDelayedSnowSession(sessionIdx: number, delayMs : number = 70) {
    const targetSession = useSnowSession(sessionIdx);
    const [ session, setSession ] = useState(targetSession);

    useEffect(() => {
        const handler = setTimeout(() => {
            setSession(targetSession);
        }, delayMs);
        return () => {
            clearTimeout(handler);
        };
    }, [ targetSession ]);

    return session;
}

function snowSessionsReducer(sessions : ValidatedSnowSession[], action : SnowSessionDispatchAction): ValidatedSnowSession[] {
    switch(action.type) {
       case 'new-session':
            const newSessionAction = action as SnowSessionDispatchNewSessionAction;
            return [
               ...sessions,
               createSession(newSessionAction.newSessionId),
           ];
        case 'session-changed':
            const sessionIdChangeAction = action as SnowSessionDispatchSessionIdChangeAction;
            const idx = sessionIdChangeAction.sessionIdx;
            const last = sessions[idx];
            const changed = {
                ...last,
                ...sessionIdChangeAction.changes,
            }
            const draft = draftSession(changed, last);

            console.log("changes:", sessionIdChangeAction.changes, "status change: " + changed.status);

            return [
               ...sessions.slice(0, idx),
                draft,
               ...sessions.slice(idx + 1),
           ];
       case 'delete-session':
            const deleteSessionAction = action as SnowSessionDispatchDeleteSessionAction;
            return [
               ...sessions.slice(0, deleteSessionAction.sessionIdx),
               ...sessions.slice(deleteSessionAction.sessionIdx + 1),
           ];
       case 'accept-or-reject-session-changes':
            return sessions.map(sessionWithAppliedChanges);
    }

    throw Error("Unknown action type: " + action.type)
}

function createSession(initialSessionId : string) : ValidatedSnowSession {
    return sessionWithCommittedDraftChanges({
        sessionId : initialSessionId,
        presetName: snowConstraints.defaultPreset,
        width: '' + snowConstraints.defaultWidth,
        height: '' + snowConstraints.defaultHeight,
        fps: '' + snowConstraints.defaultFps,
        animationProgress: 79,
        bufferLevel: 90,
        status: "stopped",
        errorMsg: "",

        foundPresetName: null,
        foundWidth: null,
        foundHeight: null,
        foundFps: null,
    });
}

function draftSession(draft : SnowSession, last : ValidatedSnowSession) : ValidatedSnowSession {
    return {
        ...last,
        ...draft,
        ...( sessionErrors(draft) ),
    }
}

function sessionErrors(draft : SnowSession) {
    return {
       sessionIdError: validateSnowSessionId(draft.sessionId),
       widthError: validateNumberBetween(draft.width, snowConstraints.minWidth, snowConstraints.maxWidth),
       heightError: validateNumberBetween(draft.height, snowConstraints.minHeight, snowConstraints.maxHeight),
       fpsError: validateNumberBetween(draft.fps, snowConstraints.minFps, snowConstraints.maxFps),
   };
}

function sessionWithAppliedChanges(session: ValidatedSnowSession): ValidatedSnowSession {
    const numOfErrors = Object.values(sessionErrors(session))
         .filter( value => value !== null )
         .length;
    if (numOfErrors === 0) {
        return sessionWithCommittedDraftChanges(session);
    } else {
        return sessionWithRevertedDraftChanges(session);
    }
}

function sessionWithCommittedDraftChanges(draft: SnowSession): ValidatedSnowSession {
    return postProcessedSession({
        ...draft,
        ...( sessionErrors(draft) ),
        validatedSessionId: draft.sessionId,
        validatedWidth: parseInt(draft.width),
        validatedHeight: parseInt(draft.height),
        validatedFps: parseInt(draft.fps),
    });
}

function sessionWithRevertedDraftChanges(session: ValidatedSnowSession): ValidatedSnowSession {
    const revertedSession={
        ...session,
        sessionId: session.validatedSessionId,
        width: '' + session.validatedWidth,
        height: '' + session.validatedHeight,
        fps: '' + session.validatedFps,
    };
    return postProcessedSession({
        ...revertedSession,
        ...( sessionErrors(revertedSession) ),
    });
}

function postProcessedSession(session: ValidatedSnowSession): ValidatedSnowSession {
    return {
        ...session,
        animationProgress: session.status === 'playing' ? session.animationProgress : 0,
    };
}
