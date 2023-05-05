import { useRef } from 'react'
import { useSessions, useSessionsDispatch } from './SessionsProvider'

type SessionsManager = {
    createNewSession: () => void,
    deleteSession: (sessionIdx: number) => void,
}

export function useSessionsManager(): SessionsManager {
    const sessions = useSessions();
    const dispatch = useSessionsDispatch();
    const createdCounter = useRef<number>(1);

    function uniqueSessionId(): string {
        do {
            const newSessionId = 'session-' + createdCounter.current++;

            const isUniqueName = sessions
                .map(s => s.sessionId)
                .indexOf(newSessionId) === -1;

            if (isUniqueName) {
                return newSessionId;
            }
        } while( true );
    }

    return {
        createNewSession: () => {
            return dispatch({
                type: 'new-session',
                newSessionId: uniqueSessionId(),
        })},
        deleteSession: (sessionIdx: number) => dispatch({
            type: 'delete-session',
            sessionIdx: sessionIdx,
        })
    };
}

