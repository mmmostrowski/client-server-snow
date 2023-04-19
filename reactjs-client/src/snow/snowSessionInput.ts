import { useEffect, useState, useMemo, useRef, MutableRefObject, FocusEvent, FocusEventHandler } from "react";
import { useSnowSession, useSnowSessionDispatch, useDelayedSnowSession } from '../snow/SnowSessionsProvider'

type UseSessionInputResponse = [
    MutableRefObject<any>,
    FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>,
    FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>,
    () => boolean,
]

export default function useSessionInput(sessionIdx: number, varName: string, value: any): UseSessionInputResponse {
    const dispatch = useSnowSessionDispatch(sessionIdx);
    const valueRef = useRef(value);
    const orgValueRef = useRef(value);
    const inputRef = useRef(null);
    const needSyncRef = useRef(true);
    const underEditRef = useRef(false);
    const underFocusRef = useRef(false);
    const timeoutRef = useRef(null);

    useEffect(() => {
        if (orgValueRef.current !== value) {
            valueRef.current = value;
            inputRef.current.value = value;
            orgValueRef.current = value;
        }

        if (needSyncRef.current) {
            needSyncRef.current = false;
            valueRef.current = value;
            if (inputRef.current) {
                inputRef.current.value = valueRef.current;
            }
        }
    });

    function handleBlur(e : FocusEvent<HTMLInputElement | HTMLTextAreaElement>) {
        underFocusRef.current = false;
        updateInput();
    }

    function handleChange(e : FocusEvent<HTMLInputElement | HTMLTextAreaElement>) {
        underEditRef.current = true;
        underFocusRef.current = true;
        valueRef.current = e.target.value;
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = setTimeout(() => {
            updateInput();
            underEditRef.current = false;
        }, 25);
    }

    function updateInput() {
        if (underFocusRef.current) {
            dispatch({
                type: 'session-changed',
                changes : {
                    [varName]: valueRef.current,
                },
            })
        } else {
            dispatch({ type: 'accept-or-reject-session-changes' });
            needSyncRef.current = true;
        }
    }

    function isUnderEdit(): boolean {
        return underEditRef.current;
    }

    return [ inputRef, handleBlur, handleChange, isUnderEdit ];
}
