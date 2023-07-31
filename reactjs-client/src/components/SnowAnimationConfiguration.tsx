import * as React from "react";
import Container from '@mui/material/Container';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormHelperText from '@mui/material/FormHelperText';
import DebouncedInput from './DebouncedInput'
import {
    DispatchActionWithoutSessionIdx,
    useDebouncedSession,
    useSession,
    useSessionDispatch
} from '../snow/SessionsProvider'
import {applicationConfig} from "../config/application";

interface Props {
    sessionIdx: number
}

export default function SnowAnimationConfiguration({ sessionIdx } : Props): JSX.Element {
    const {
        isSessionExists,
        width: userWidth,
        height: userHeight,
        fps: userFps,
        presetName: userPresetName,
        sceneName: userSceneName,
        foundWidth, foundHeight, foundFps,
        foundPresetName, foundSceneName,
    } = useSession(sessionIdx);
    const { status, widthError, heightError, fpsError } = useDebouncedSession(sessionIdx);
    const dispatch: (action: DispatchActionWithoutSessionIdx) => void = useSessionDispatch(sessionIdx);
    const width = isSessionExists ? foundWidth : userWidth;
    const height = isSessionExists ? foundHeight : userHeight;
    const fps = isSessionExists ? foundFps : userFps;
    const presetName = isSessionExists ? foundPresetName : userPresetName;
    const sceneName = isSessionExists ? foundSceneName : userSceneName;
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

    function handleSceneChange(e : React.ChangeEvent<HTMLInputElement>) {
        dispatch({
            type: 'session-changed',
            changes: {
                sceneName: e.target.value
            }
        });
    }
    return (
        <>
            <Container sx={{ padding: 2, paddingRight: [ 4, 4, 4 ] }} >
                <ConfigSelectField
                    label="Animation preset"
                    value={presetName}
                    entries={applicationConfig.presets}
                    isAvailable={isAvailable}
                    handleChange={handlePresetChange}
                    isEditable={isEditable}
                />
            </Container>
            <Container sx={{ padding: 2, paddingRight: [ 4, 4, 4 ] }} >
                <ConfigSelectField
                    label="Scene"
                    value={sceneName}
                    entries={applicationConfig.scenes}
                    isAvailable={isAvailable}
                    handleChange={handleSceneChange}
                    isEditable={isEditable}
                />
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

type ConfigSelectFieldProps = {
    label: string,
    entries: Record<string, string>,
    isAvailable: boolean,
    isEditable: boolean,
    value: string|number,
    handleChange: (e: React.ChangeEvent<HTMLInputElement>) => void,
}

function ConfigSelectField(props: ConfigSelectFieldProps): JSX.Element {
    const { label, entries, handleChange, isAvailable, isEditable, value } = props;
    const isUnknown = !isAvailable || (!isEditable && value === "");
    return <>
        <Select
            value={isUnknown ? "?" : value}
            onChange={handleChange}
            disabled={!isEditable}
            variant="outlined"
            size="small"
            sx={{ width: 180 }} >

            { isUnknown && <MenuItem key="?" value="?">?</MenuItem> }
            { isAvailable && Object.entries(entries).map(([key, label]) =>
                <MenuItem key={key} value={key}>{label}</MenuItem>
            ) }

        </Select>
        <FormHelperText className={isEditable ? "" : "Mui-disabled"} sx={{ pl: 2 }} >{label}</FormHelperText>
    </>;
}

type ConfigNumberFieldProps = {
    isAvailable: boolean,
    value: string|number,
    isEditable: boolean,
    helperText: string,
    sessionIdx: number,
    errorMsg: string,
    varName: string,
    label: string,
}

function ConfigNumberField(props: ConfigNumberFieldProps): JSX.Element {
    const { sessionIdx, varName, isAvailable, isEditable,
        value, errorMsg, label, helperText } = props;

    return <DebouncedInput
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