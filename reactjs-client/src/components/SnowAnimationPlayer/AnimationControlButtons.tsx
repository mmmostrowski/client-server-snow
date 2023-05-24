import * as React from "react";
import Button from '@mui/material/Button';
import {useDelayedSession, useSession} from '../../snow/SessionsProvider'


interface Props {
    sessionIdx: number,
    handleStart: () => void,
    handleStop: () => void,
}

export default function AnimationControlButtons(props: Props): JSX.Element {
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
                onClick={handleStart}
                title={isSessionExists ? "Active animation found on server. Attach to it!" : "Start new animation on server!"}
                disabled={!isStartActive}>{isSessionExists ? "Play" : "Start"}</Button>

            <Button
                className="stop-button"
                variant="contained"
                onClick={handleStop}
                title="Stop animation on server!"
                disabled={!isStopActive}>Stop</Button>
        </>
    );
}