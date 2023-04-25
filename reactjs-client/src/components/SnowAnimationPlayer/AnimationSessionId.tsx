import * as React from "react";
import { useSnowSession } from '../../snow/SnowSessionsProvider'
import AnimationInput from './AnimationInput'
import {
    useSessionStatusUpdater,
    SessionStatusUpdater
} from '../../snow/snowSessionStatus'


interface AnimationSessionIdProps {
    sessionIdx: number,
    isEditing?: (underEdit: boolean) => void,
}

export default function AnimationSessionId({ sessionIdx, isEditing } : AnimationSessionIdProps): JSX.Element {
    const {
        status, hasError, isStopped,
        sessionId, sessionIdError, hasSessionIdError,
    } = useSnowSession(sessionIdx);
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const isSessionIdInputActive: boolean =
           isStopped
        || hasError
        || status === "checking";

    function handleSessionIdChange(value: string): void {
        setSessionStatus("stopped-not-checked");
    }

    return <AnimationInput
        sessionIdx={sessionIdx}
        varName="sessionId"
        varValue={sessionId}
        onChangeValue={handleSessionIdChange}
        isEditing={isEditing}

        InputLabelProps={{ shrink: true }}
        variant="standard"
        label="Session id"
        defaultValue={sessionId}
        required
        disabled={!isSessionIdInputActive}
        error={hasSessionIdError}
        helperText={sessionIdError}
        autoComplete="off"
    />
}

