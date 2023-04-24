import * as React from "react";
import SnowCanvas from '../SnowCanvas'
import LinearProgress from '@mui/material/LinearProgress';
import AnimationCircularProgress from './AnimationCircularProgress'
import AnimationControlButtons from './AnimationControlButtons'
import AnimationSessionId from './AnimationSessionId'


interface AnimationPanelProps {
    sessionIdx: number,
    handleIsEditingSessionId?: (underEdit: boolean) => void,
    animationProgress: number,
    handleStart: () => void,
    handleStop: () => void,
}

export default function AnimationPanel(props : AnimationPanelProps): JSX.Element {
    const { sessionIdx, handleIsEditingSessionId, animationProgress, handleStart, handleStop } = props;
    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <AnimationCircularProgress sessionIdx={sessionIdx}/>
                <AnimationSessionId sessionIdx={sessionIdx} isEditing={handleIsEditingSessionId} />
                <AnimationControlButtons sessionIdx={sessionIdx} handleStart={handleStart} handleStop={handleStop} />
            </div>
            <SnowCanvas sessionIdx={sessionIdx} />
            <LinearProgress value={animationProgress}
                title="Animation progress"
                variant="determinate" />
        </div>
    )
}
