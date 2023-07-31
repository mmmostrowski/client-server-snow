import * as React from "react";
import {useSession} from '../../snow/SessionsProvider'
import DebouncedInput from '../DebouncedInput'


interface Props {
    sessionIdx: number,
    isEditing?: (underEdit: boolean) => void,
}

export default function AnimationSessionIdInput({ sessionIdx, isEditing } : Props): JSX.Element {
    const {
        status, hasError, isStopped,
        sessionId, sessionIdError, hasSessionIdError,
    } = useSession(sessionIdx);
    const isSessionIdInputActive: boolean =
           isStopped
        || hasError
        || status === "checking";

    return <DebouncedInput
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

