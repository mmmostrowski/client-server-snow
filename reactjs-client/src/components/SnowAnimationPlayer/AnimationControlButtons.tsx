import * as React from "react";
import Button from '@mui/material/Button';
import { useSession, useDelayedSession } from '../../snow/SessionsProvider'


interface AnimationControlButtonsProps {
    sessionIdx: number,
    handleStart: () => void,
    handleStop: () => void,
}

export default function AnimationControlButtons(props: AnimationControlButtonsProps): JSX.Element {
    const { sessionIdx, handleStart, handleStop } = props;
    const { isStopped, isSessionExists } = useSession(sessionIdx);
    const { status } = useDelayedSession(sessionIdx);

    const isStartActive: boolean =
           isStopped
        || status === "error-cannot-start-new"
        || status === "error-cannot-start-existing"
        || status === "error-cannot-stop";

    const isStopActive: boolean =
           status === "buffering"
        || status === "playing"
        || status === "error-cannot-stop";

    return (
        <>
            <Button
                className="start-button"
                variant="contained"
                title={isSessionExists ? "Active animation found on server. Attach to it!" : "Start new animation on server!"}
                onClick={handleStart}
                disabled={!isStartActive}>{isSessionExists ? "Play" : "Start"}</Button>

            <Button
                className="stop-button"
                variant="contained"
                title="Stop animation on server!"
                onClick={handleStop}
                disabled={!isStopActive}>Stop</Button>
        </>
    );
}