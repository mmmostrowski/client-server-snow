import { useSnowSessions, useSnowSessionDispatch } from './SnowSessionsProvider'

export type SessionStatusUpdater = (status: string, params?: object) => void;
export type SessionErrorStatusUpdated = (error: Error|string, status?:string) => void;

export function useSessionStatusUpdater(sessionIdx: number): SessionStatusUpdater {
    const dispatch = useSnowSessionDispatch(sessionIdx);

    return ( status: string, params?: object ) => {
        dispatch({
            type : 'session-changed',
            changes: {
                status: status,
                ...params,
            },
        });
    }
}

export function useSessionErrorStatusUpdater(sessionIdx: number): SessionErrorStatusUpdated {
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);

    return ( error: Error|string, status:string = "error" ) => {
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
    }
}

