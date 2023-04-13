import * as React from "react";
import { useContext, createContext, useReducer } from 'react';
import { validateSnowSessionId, validateNumberBetween } from './snowSessionValidator';

export const SnowSessionsContext = createContext([]);
export const SnowSessionsDispatchContext = createContext(null);

export const snowConstraints = {
    defaultSessionId: "session-abc",

    minWidth: 40,
    minHeight: 20,
    defaultWidth: 180,
    defaultHeight: 100,
    maxWidth: 200,
    maxHeight: 150,
};

interface SnowSessionDraft {
    sessionId: string,
    width: string,
    height: string,
}

interface SnowSession extends SnowSessionDraft {
    validatedSessionId: string,
    sessionIdError: string|null,

    validatedWidth: number,
    widthError: string|null,

    validatedHeight: number,
    heightError: string|null,
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

export function useSnowSessions(): SnowSession[] {
    return useContext(SnowSessionsContext);
}

export function useSnowSession(sessionIdx : number): SnowSession {
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

export function snowSessionsReducer(sessions : SnowSession[], action : SnowSessionDispatchAction): SnowSession[] {
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
       case 'commit-session-changes':
            return sessions.map(sessionWithAppliedChanges);
    }

    throw Error("Unknown action type: " + action.type)
}

function createSession(initialSessionId : string) : SnowSession {
    return sessionWithCommittedDraftChanges({
        sessionId : initialSessionId,
        width: '' + snowConstraints.defaultWidth,
        height: '' + snowConstraints.defaultHeight,
    });
}

function draftSession(draft : SnowSessionDraft, last : SnowSession) : SnowSession {
    return {
        ...last,
        ...draft,
        ...( sessionErrors(draft) ),
    }
}

function sessionErrors(draft : SnowSessionDraft) {
    return {
       sessionIdError: validateSnowSessionId(draft.sessionId),
       widthError: validateNumberBetween(draft.width, snowConstraints.minWidth, snowConstraints.maxWidth),
       heightError: validateNumberBetween(draft.height, snowConstraints.minHeight, snowConstraints.maxHeight),
   };
}

function sessionWithAppliedChanges(session: SnowSession): SnowSession {
    const numOfErrors = Object.values(sessionErrors(session))
         .filter( value => value !== null )
         .length;
    if (numOfErrors === 0) {
        return sessionWithCommittedDraftChanges(session);
    } else {
        return sessionWithRevertedDraftChanges(session);
    }
}

function sessionWithCommittedDraftChanges(draft: SnowSessionDraft): SnowSession {
    return {
        ...draft,
        ...( sessionErrors(draft) ),
        validatedSessionId: draft.sessionId,
        validatedWidth: parseInt(draft.width),
        validatedHeight: parseInt(draft.height),
    }
}

function sessionWithRevertedDraftChanges(session: SnowSession): SnowSession {
    const revertedSession={
        ...session,
        sessionId: session.validatedSessionId,
        width: '' + session.validatedWidth,
        height: '' + session.validatedHeight,
    };
    return {
        ...revertedSession,
        ...( sessionErrors(revertedSession) ),
    };
}
