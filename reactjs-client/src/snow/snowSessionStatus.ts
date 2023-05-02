import { useCallback } from 'react'
import {useSnowSessionDispatch, SessionStatus, SessionErrorStatus, SnowSession} from './SnowSessionsProvider'

export type SessionStatusUpdater = (status: SessionStatus, params?: Partial<SnowSession>) => void;
export type SessionErrorStatusUpdater = (error: Error|string, status?:SessionErrorStatus) => void;

export function useSessionStatusUpdater(sessionIdx: number): SessionStatusUpdater {
    const dispatch = useSnowSessionDispatch(sessionIdx);
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
        if (typeof error === "string") {
            setSessionStatus(status, {
                errorMsg: error,
            });
        } else {
            console.error(error);
            setSessionStatus(status, {
                errorMsg: error.message,
            });
        }
    }, [ setSessionStatus ]);
}

