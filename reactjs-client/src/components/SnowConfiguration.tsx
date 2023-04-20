import * as React from "react";
import { useEffect, useRef } from "react";
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormHelperText from '@mui/material/FormHelperText';
import useSessionInput from '../snow/snowSessionInput'
import { snowConstraints, useDelayedSnowSession, useSnowSessionDispatch, useSnowSession } from '../snow/SnowSessionsProvider'

interface SnowConfigurationProps {
    sessionIdx: number
}

export default function SnowConfiguration({ sessionIdx } : SnowConfigurationProps): JSX.Element {
    let { width: userWidth,
          height: userHeight,
          fps: userFps,
          presetName: userPresetName } = useSnowSession(sessionIdx);
    let { status,
          widthError, heightError, fpsError,
          foundWidth, foundHeight, foundFps, foundPresetName } = useDelayedSnowSession(sessionIdx);
    const dispatch = useSnowSessionDispatch(sessionIdx);

    const isEditable = status === 'stopped';
    const isAvailable = status !== 'checking' && status !== 'error';
    const width = status === 'found' ? foundWidth : userWidth;
    const height = status === 'found' ? foundHeight : userHeight;
    const fps = status === 'found' ? foundFps : userFps;
    const presetName = status === 'found' ? foundPresetName : userPresetName;

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
                    { isAvailable && Object.entries(snowConstraints.presets).map(([presetName, presetLabel]) =>
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
    const restoreOnceAvailableRef = useRef(false);
    const [
        inputRef,
        handleBlur,
        handleChange
    ] = useSessionInput(sessionIdx, varName, value);

    useEffect(() => {
        if (!isAvailable) {
            inputRef.current.value = '?';
            restoreOnceAvailableRef.current = true;
        } else if (restoreOnceAvailableRef.current) {
            restoreOnceAvailableRef.current = false;
            inputRef.current.value = value;
        }
    });

    return <TextField
        helperText={errorMsg != null ? errorMsg : helperText}
        defaultValue={isAvailable ? value : ""}
        inputProps={{ inputMode: 'numeric' }}
        InputLabelProps={{ shrink: true }}
        error={errorMsg != null}
        onChange={handleChange}
        disabled={!isEditable}
        inputRef={inputRef}
        onBlur={handleBlur}
        sx={{ width: 180 }}
        label={label}
        autoComplete="off"
        variant="outlined"
        size="small"
    />
}