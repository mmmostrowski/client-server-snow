import {useCallback} from 'react'
import {DraftSession, SessionErrorStatus, SessionStatus, useSession, useSessionDispatch} from './SessionsProvider'

export type SessionStatusUpdater = (status: SessionStatus, params?: Partial<DraftSession>) => void;
export type SessionErrorStatusUpdater = (error: Error|string, status?:SessionErrorStatus) => void;
export type SessionPlayingStatusUpdater = (progressPercent: number, bufferPercent: number) => void;

export function useSessionStatusUpdater(sessionIdx: number): SessionStatusUpdater {
    const dispatch = useSessionDispatch(sessionIdx);
    const { status: currentStatus } = useSession(sessionIdx);
    return useCallback(( status: SessionStatus, params?: object ) => {
        if (params === undefined && status === currentStatus ) {
            return;
        }
        dispatch({
            type : 'session-changed',
            changes: {
                status,
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

export function useSessionPlayingStatusUpdater(sessionIdx: number): SessionPlayingStatusUpdater {
    const { animationProgressRef, bufferLevelRef } = useSession(sessionIdx);
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(0);

    return useCallback(( progressPercent: number, bufferPercent: number ) => {
        animationProgressRef.current = progressPercent;
        bufferLevelRef.current = bufferPercent;
    }, [ setSessionStatus ]);
}

