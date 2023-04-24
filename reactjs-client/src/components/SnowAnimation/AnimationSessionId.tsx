import * as React from "react";
import { MutableRefObject } from 'react';
import TextField from '@mui/material/TextField';
import useSessionInput from '../../snow/snowSessionInput'
import { useSnowSession } from '../../snow/SnowSessionsProvider'
import {
    useSessionStatusUpdater,
    SessionStatusUpdater } from '../../snow/snowSessionStatus'


interface AnimationSessionIdProps {
    sessionIdx: number,
    underEditRef?: MutableRefObject<boolean>,
}

export default function AnimationSessionId({ sessionIdx, underEditRef } : AnimationSessionIdProps): JSX.Element {
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

    function handleSessionIdUnderEdit(underEdit: boolean): void {
        if (underEditRef) {
            underEditRef.current = underEdit;
        }
    }

    const {
        inputRef,
        handleBlur: handleSessionIdInputBlur,
        handleChange: handleSessionIdInputChange,
    } = useSessionInput(sessionIdx, 'sessionId', sessionId, handleSessionIdChange, handleSessionIdUnderEdit);

    return (
            <TextField
                InputLabelProps={{ shrink: true }}
                inputRef={inputRef}
                variant="standard"
                label="Session id"
                defaultValue={sessionId}
                required
                disabled={!isSessionIdInputActive}
                error={hasSessionIdError}
                helperText={sessionIdError}
                onChange={handleSessionIdInputChange}
                onBlur={handleSessionIdInputBlur}
                autoComplete="off"
            />
    )
}
