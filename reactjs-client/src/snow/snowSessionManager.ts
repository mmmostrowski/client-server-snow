import { useRef } from 'react'
import { useSnowSessions, useSnowSessionsDispatch } from './SnowSessionsProvider'

type SessionsManager = {
    createNewSession: () => void,
    deleteSession: (sessionIdx: number) => void,
}

export function useSessionsManager(): SessionsManager {
    const sessions = useSnowSessions();
    const dispatch = useSnowSessionsDispatch();
    const createdCounter = useRef<number>(1);

    return {
        createNewSession: () => {
            let newSessionId: string;
            do {
                newSessionId = 'session-' + createdCounter.current++;
            } while( sessions.map(s => s.sessionId).indexOf(newSessionId) !== -1);

            return dispatch({
                type: 'new-session',
                newSessionId: newSessionId,
        })},
        deleteSession: (sessionIdx: number) => dispatch({
            type: 'delete-session',
            sessionIdx: sessionIdx,
        })
    };
}

