import { useCallback } from 'react'
import { useSnowSessionDispatch } from './SnowSessionsProvider'

export type SessionStatusUpdater = (status: string, params?: object) => void;
export type SessionErrorStatusUpdater = (error: Error|string, status?:string) => void;

export function useSessionStatusUpdater(sessionIdx: number): SessionStatusUpdater {
    const dispatch = useSnowSessionDispatch(sessionIdx);
    return useCallback(( status: string, params?: object ) => {
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

    return useCallback(( error: Error|string, status:string = "error" ) => {
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

