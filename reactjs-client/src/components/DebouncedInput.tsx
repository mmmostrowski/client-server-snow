import * as React from "react";
import {FocusEvent, MutableRefObject, useEffect, useRef} from "react";
import TextField from '@mui/material/TextField';
import {TextFieldProps} from '@mui/material';
import {useSessionDispatch} from '../snow/SessionsProvider'

type Props = TextFieldProps & {
    sessionIdx: number,
    varName: string,
    varValue: string|number,
    onChangeValue?: (value:string|number) => void,
    isEditing?: (underEdit: boolean) => void,
}

export default function DebouncedInput(props: Props): JSX.Element {
    const {
        sessionIdx,
        varName,
        varValue,
        onChangeValue,
        isEditing,
        ...childProps
    } = {
        onChangeValue: () => {},
        isEditing: () => {},
        ...props,
    };

    const dispatch = useSessionDispatch(sessionIdx);
    const valueRef = useRef<string|number>(varValue);
    const prevValueRef = useRef<string|number>(varValue);
    const needSyncRef = useRef<boolean>(true);
    const underEditRef = useRef<boolean>(false);
    const underFocusRef = useRef<boolean>(false);
    const timeoutRef = useRef<ReturnType<typeof setTimeout> >(null);
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (isValueChangedOutside() || needsSyncWithOutside()) {
            synchronizeValue();
        }
    });

    function synchronizeValue(): void {
        needsSyncWithOutside(false);
        prevValueRef.current = varValue;
        valueRef.current = varValue;
        if (inputRef.current) {
            inputRef.current.value = '' + varValue;
        }
    }

    function handleChange(e : FocusEvent<HTMLInputElement | HTMLTextAreaElement>): void {
        valueRef.current = e.target.value;
        isFocused(true);
        isUnderEdit(true);
        runDelayed(() => {
            updateInput();
            isUnderEdit(false);
        });
    }

    function handleBlur(): void {
        isFocused(false);
        updateInput();
    }

    function updateInput(): void {
        if (isFocused()) {
            dispatch({
                type: 'session-changed',
                changes : {
                    [ varName ]: valueRef.current,
                },
            })
        } else {
            dispatch({ type: 'accept-or-reject-session-changes' });
            needsSyncWithOutside(true);
        }

        if (isUnderEdit() && onChangeValue) {
            onChangeValue(valueRef.current);
        }
    }

    function isValueChangedOutside(): boolean {
        return prevValueRef.current !== varValue;
    }

    function isFocused(value?: boolean): boolean {
        return refFlag(underFocusRef, value);
    }

    function isUnderEdit(value?: boolean): boolean {
        if (value !== undefined) {
            isEditing(value);
        }
        return refFlag(underEditRef, value);
    }

    function needsSyncWithOutside(value?: boolean): boolean {
        return refFlag(needSyncRef, value);
    }

    function runDelayed(callback: () => void): void {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            callback();
        }, 35);
    }

    function refFlag(ref: MutableRefObject<boolean>, value?: boolean): boolean {
        return value === undefined ? ref.current : ref.current = value;
    }

    return <TextField
        {...childProps}
        onChange={handleChange}
        onBlur={handleBlur}
        inputRef={inputRef}
    />;
}
