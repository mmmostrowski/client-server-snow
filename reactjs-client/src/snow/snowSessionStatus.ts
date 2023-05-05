import { useCallback } from 'react'
import {useSessionDispatch, SessionStatus, SessionErrorStatus, DraftSession} from './SessionsProvider'

export type SessionStatusUpdater = (status: SessionStatus, params?: Partial<DraftSession>) => void;
export type SessionErrorStatusUpdater = (error: Error|string, status?:SessionErrorStatus) => void;

export function useSessionStatusUpdater(sessionIdx: number): SessionStatusUpdater {
    const dispatch = useSessionDispatch(sessionIdx);
    return useCallback(( status: SessionStatus, params?: object ) => {
        dispatch({
            type : 'session-changed',
            changes: {
                status: status,
                ...params,
            },
        });
    }, [ dispatch ]);
}

export function useSessionErrorStatusUpdater(sessionIdx: number): SessionErrorStatusUpdater {
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);

    return useCallback(( error: Error|string, status:SessionErrorStatus = "error" ) => {
        setSessionStatus(status, {
            errorMsg: typeof error === "string"
                ? error
                : error.message
        });
    }, [ setSessionStatus ]);
}

