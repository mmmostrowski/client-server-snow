import * as React from "react";
import Container from '@mui/material/Container';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormHelperText from '@mui/material/FormHelperText';
import AnimationDebouncedInput from './SnowAnimationPlayer/AnimationDebouncedInput'
import {useDebouncedSession, useSession, useSessionDispatch} from '../snow/SessionsProvider'
import {applicationConfig} from "../config/application";

interface SnowConfigurationProps {
    sessionIdx: number
}

export default function SnowConfiguration({ sessionIdx } : SnowConfigurationProps): JSX.Element {
    const {
        isSessionExists,
        width: userWidth,
        height: userHeight,
        fps: userFps,
        presetName: userPresetName,
        foundWidth, foundHeight, foundFps, foundPresetName,
    } = useSession(sessionIdx);
    const { status, widthError, heightError, fpsError } = useDebouncedSession(sessionIdx);
    const dispatch = useSessionDispatch(sessionIdx);
    const width = isSessionExists ? foundWidth : userWidth;
    const height = isSessionExists ? foundHeight : userHeight;
    const fps = isSessionExists ? foundFps : userFps;
    const presetName = isSessionExists ? foundPresetName : userPresetName;
    const isAvailable = status !== 'checking' && status !== 'error';
    const isEditable =
           status === 'stopped-not-checked'
        || status === 'stopped-not-found'
        || status === 'error-cannot-start-new';


    function handlePresetChange(e : React.ChangeEvent<HTMLInputElement>) {
        dispatch({
            type: 'session-changed',
            changes: {
                presetName: e.target.value
            }
        });
    }

    return (
        <>
            <Container sx={{ padding: 2, paddingRight: [ 4, 4, 4 ] }} >
                <Select
                    value={isAvailable ? presetName : "?"}
                    onChange={handlePresetChange}
                    disabled={!isEditable}
                    variant="outlined"
                    size="small"
                    sx={{ width: 180 }} >

                    { !isAvailable && <MenuItem key="?" value="?">?</MenuItem> }
                    { isAvailable && Object.entries(applicationConfig.presets).map(([presetName, presetLabel]) =>
                            <MenuItem key={presetName} value={presetName}>{presetLabel}</MenuItem>
                    ) }

                </Select>
                <FormHelperText className={isEditable ? "" : "Mui-disabled"} sx={{ pl: 2 }} >Animation preset</FormHelperText>
            </Container>
            <Container sx={{ padding: 2 }} >
                <ConfigNumberField
                    label="Width"
                    varName="width"
                    value={width}
                    errorMsg={widthError}
                    sessionIdx={sessionIdx}
                    isEditable={isEditable}
                    isAvailable={isAvailable}
                    helperText="Horizontal canvas size"
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                <ConfigNumberField
                    label="Height"
                    varName="height"
                    value={height}
                    errorMsg={heightError}
                    sessionIdx={sessionIdx}
                    isEditable={isEditable}
                    isAvailable={isAvailable}
                    helperText="Vertical canvas size"
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                <ConfigNumberField
                    label="Fps"
                    varName="fps"
                    value={fps}
                    errorMsg={fpsError}
                    sessionIdx={sessionIdx}
                    isEditable={isEditable}
                    isAvailable={isAvailable}
                    helperText="Frames per second"
                />
            </Container>
          </>
    );
}


type ConfigNumberFieldProps = {
    sessionIdx: number,
    varName: string,
    label: string,
    isAvailable: boolean,
    isEditable: boolean,
    value: string|number,
    helperText: string
    errorMsg: string,
}

function ConfigNumberField(props: ConfigNumberFieldProps): JSX.Element {
    const { sessionIdx, varName, isAvailable, isEditable, value, errorMsg, label, helperText } = props;

    return <AnimationDebouncedInput
        sessionIdx={sessionIdx}
        varName={varName}
        varValue={isAvailable ? value : '?'}

        helperText={errorMsg != null ? errorMsg : helperText}
        defaultValue={isAvailable ? value : ""}
        inputProps={{ inputMode: 'numeric' }}
        InputLabelProps={{ shrink: true }}
        error={errorMsg != null}
        disabled={!isEditable}
        label={label}
        autoComplete="off"
        variant="outlined"
        size="small"
    />
}