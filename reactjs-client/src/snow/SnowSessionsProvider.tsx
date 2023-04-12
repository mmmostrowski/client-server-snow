import * as React from "react";
import { ReactNode, useContext, createContext, useReducer } from 'react';

export const SnowSessionsContext = createContext([]);
export const SnowSessionsDispatchContext = createContext(null);

interface SnowSession {
    sessionId: string,
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
    changedSessionId: string,
}

interface SnowSessionDispatchNewSessionAction extends SnowSessionDispatchAnyAction {
    newSessionId: string,
}

interface SnowSessionDispatchDeleteSessionAction extends SnowSessionDispatchAnyAction {
    sessionIdx : number,
}

export function SnowSessionsProvider({ children } : any) {
    const [ sessions, dispatch ] = useReducer(snowSessionsReducer, [
        {
            "sessionId" : "session-abc",
        },
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
        case 'session-id-changed':
            const sessionIdChangeAction = action as SnowSessionDispatchSessionIdChangeAction;
            const idx = sessionIdChangeAction.sessionIdx;
            return [
               ...sessions.slice(0, idx),
               {
                   ...sessions[idx],
                   sessionId : sessionIdChangeAction.changedSessionId,
               },
               ...sessions.slice(idx + 1),
           ];
       case 'new-session':
            const newSessionAction = action as SnowSessionDispatchNewSessionAction;
            return [
               ...sessions,
               {
                   sessionId : newSessionAction.newSessionId,
               },
           ];
       case 'delete-session':
            const deleteSessionAction = action as SnowSessionDispatchDeleteSessionAction;
            return [
               ...sessions.slice(0, deleteSessionAction.sessionIdx),
               ...sessions.slice(deleteSessionAction.sessionIdx + 1),
           ];
    }

    throw Error("Unknown action type: " + action.type)
}
