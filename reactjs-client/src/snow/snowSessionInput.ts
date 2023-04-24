import { useEffect, useRef, MutableRefObject, FocusEvent, FocusEventHandler } from "react";
import { useSnowSessionDispatch } from '../snow/SnowSessionsProvider'

type UseSessionInputResponse = [
    MutableRefObject<any>,
    FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>,
    FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>,
    () => boolean,
]

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

    function isFocused(value?: boolean): boolean {
        return value === undefined
            ? underFocusRef.current
            : underFocusRef.current = value
        ;
    }

    function isUnderEdit(value?: boolean): boolean {
        return value === undefined
            ? underEditRef.current
            : underEditRef.current = value
        ;
    }

    function needsSyncWithOutside(value?: boolean): boolean {
        return value === undefined
            ? needSyncRef.current
            : needSyncRef.current = value
        ;
    }

    function isValueChangedOutside(): boolean {
        return prevValueRef.current !== value;
    }

    function runDelayed(callback: () => void) {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            callback();
        }, 25);
    }

    return [ inputRef, handleBlur, handleChange, () => isUnderEdit() ];
}
