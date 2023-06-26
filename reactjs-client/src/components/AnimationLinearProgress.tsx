import LinearProgress from "@mui/material/LinearProgress";
import * as React from "react";
import {useEffect, useState} from "react";
import {useSession} from "../snow/SessionsProvider";

interface Props {
    sessionIdx: number;
}

export default function AnimationLinearProgress({ sessionIdx } : Props): JSX.Element {

    const {
        animationProgressRef,
    } = useSession(sessionIdx);

    const [ animationProgress, setAnimationProgress ] = useState<number>(animationProgressRef.current);

    useEffect(() => {
        const timer = setInterval(() => {
            setAnimationProgress(animationProgressRef.current);
        }, 40);
        return () => {
            clearInterval(timer);
        };
    }, [ animationProgressRef ]);

    return <LinearProgress value={animationProgress}
                           title="Animation progress"
                           variant="determinate"/>
}