import LinearProgress from "@mui/material/LinearProgress";
import * as React from "react";
import {useDebouncedSession} from "../../snow/SessionsProvider";
import {useRefProbing} from "../../utils/useRefProbing";

interface Props {
    sessionIdx: number;
}

export default function AnimationLinearProgress({ sessionIdx } : Props): JSX.Element {
    const { animationProgressRef} = useDebouncedSession(sessionIdx);
    const animationProgress = useRefProbing<number>(animationProgressRef, 500);

    return <LinearProgress value={animationProgress}
                           title="Animation progress"
                           variant="determinate"/>
}