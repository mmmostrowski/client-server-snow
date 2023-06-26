import * as React from "react";
import {useSession} from '../../snow/SessionsProvider'
import AnimationDebouncedInput from './AnimationDebouncedInput'


interface AnimationSessionIdProps {
    sessionIdx: number,
    isEditing?: (underEdit: boolean) => void,
}

export default function AnimationSessionId({ sessionIdx, isEditing } : AnimationSessionIdProps): JSX.Element {
    const {
        status, hasError, isStopped,
        sessionId, sessionIdError, hasSessionIdError,
    } = useSession(sessionIdx);
    const isSessionIdInputActive: boolean =
           isStopped
        || hasError
        || status === "checking";

    return <AnimationDebouncedInput
        sessionIdx={sessionIdx}
        varName="sessionId"
        varValue={sessionId}
        isEditing={isEditing}
        variant="standard"
        label="Session id"
        defaultValue={sessionId}
        required
        disabled={!isSessionIdInputActive}
        error={hasSessionIdError}
        helperText={sessionIdError}
        autoComplete="off"
        InputLabelProps={{ shrink: true }}
    />
}

