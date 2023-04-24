import { useEffect, useRef, MutableRefObject, FocusEvent, FocusEventHandler } from "react";
import { useSnowSessionDispatch } from '../snow/SnowSessionsProvider'

type UseSessionInputResponse = {
    inputRef: MutableRefObject<any>,
    handleBlur: FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>,
    handleChange: FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>,
    isUnderEdit: () => boolean,
}

export default function useSessionInput(sessionIdx: number, varName: string, value: string|number, onChange?: (value:string|number) => void ): UseSessionInputResponse {
    const dispatch = useSnowSessionDispatch(sessionIdx);
    const valueRef = useRef<string|number>(value);
    const prevValueRef = useRef<string|number>(value);
    const inputRef = useRef<HTMLInputElement>(null);
    const needSyncRef = useRef<boolean>(true);
    const underEditRef = useRef<boolean>(false);
    const underFocusRef = useRef<boolean>(false);
    const timeoutRef = useRef<ReturnType<typeof setTimeout> >(null);

    useEffect(() => {
        if (isValueChangedOutside() || needsSyncWithOutside()) {
            synchronizeValue();
        }
    });

    function synchronizeValue(): void {
        needsSyncWithOutside(false);
        prevValueRef.current = value;
        valueRef.current = value;
        if (inputRef.current) {
            inputRef.current.value = '' + value;
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

    function handleBlur(e : FocusEvent<HTMLInputElement | HTMLTextAreaElement>): void {
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

        if (onChange && isUnderEdit()) {
            onChange(valueRef.current);
        }
    }

    function isValueChangedOutside(): boolean {
        return prevValueRef.current !== value;
    }

    function isFocused(value?: boolean): boolean {
        return refFlag(underFocusRef, value);
    }

    function isUnderEdit(value?: boolean): boolean {
        return refFlag(underEditRef, value);
    }

    function needsSyncWithOutside(value?: boolean): boolean {
        return refFlag(needSyncRef, value);
    }

    function runDelayed(callback: () => void) {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            callback();
        }, 25);
    }

    function refFlag(ref: MutableRefObject<boolean>, value?: boolean): boolean {
        return value === undefined ? ref.current : ref.current = value;
    }

    return {
        inputRef: inputRef,
        handleBlur: handleBlur,
        handleChange: handleChange,
        isUnderEdit: () => isUnderEdit(),
    }
}
