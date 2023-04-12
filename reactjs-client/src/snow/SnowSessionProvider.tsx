import * as React from "react";
import { ReactNode, useContext, createContext, useReducer } from 'react';

export const SnowSessionContext = createContext(null);
export const SnowSessionDispatchContext = createContext(null);

interface SnowSession {
    sessionId: string,
}

interface SnowSessionDispatchAction extends SnowSessionDispatchSessionIdChangeAction {
}

interface SnowSessionDispatchSessionIdChangeAction {
    type: string,
    newSessionId: string,
}


export function SnowSessionProvider({ children } : any) {
    const [ session, dispatch ] = useReducer(snowSessionReducer, {
        "sessionId" : "session-xyz",
    });

    return (
        <SnowSessionContext.Provider value={session} >
            <SnowSessionDispatchContext.Provider value={dispatch} >
                {children}
            </SnowSessionDispatchContext.Provider>
        </SnowSessionContext.Provider>
    );
}

export function useSnowSession(): SnowSession {
    return useContext(SnowSessionContext);
}

export function useSnowSessionDispatch() : (action : SnowSessionDispatchAction) => void {
    return useContext(SnowSessionDispatchContext);
}

export function snowSessionReducer(session : SnowSession, action : SnowSessionDispatchAction) {
    switch(action.type) {
        case 'on-session-id-changed':
            return {
                ...session,
                sessionId : action.newSessionId,
            };
    }

    throw Error("Unknown action type: " + action.type)
}
