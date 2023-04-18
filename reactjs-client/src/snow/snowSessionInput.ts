import { useEffect, useState, useMemo, useRef, MutableRefObject, FocusEvent, FocusEventHandler } from "react";
import { useSnowSession, useSnowSessionDispatch, useDelayedSnowSession } from '../snow/SnowSessionsProvider'

export default function useSessionInput(sessionIdx: number, varName: string, value: any):
    [ MutableRefObject<any>, FocusEventHandler<HTMLInputElement | HTMLTextAreaElement>, FocusEventHandler<HTMLInputElement | HTMLTextAreaElement> ]
{
    const dispatch = useSnowSessionDispatch(sessionIdx);
    const valueRef = useRef(value);
    const inputRef = useRef(null);
    const needSyncRef = useRef(true);

    if (needSyncRef.current) {
        needSyncRef.current = false;
        valueRef.current = value;
        if (inputRef.current) {
            inputRef.current.value = valueRef.current;
        }
    }

    function handleBlur(e : FocusEvent<HTMLInputElement | HTMLTextAreaElement>) {
        dispatch({ type: 'accept-or-reject-session-changes' });
        needSyncRef.current = true;
    }

    function handleChange(e : FocusEvent<HTMLInputElement | HTMLTextAreaElement>) {
        valueRef.current = e.target.value;
        let changes : any = {};
        changes[varName] = valueRef.current;
        setTimeout(() => {
            dispatch({
                type: 'session-changed',
                changes : changes,
            })
        }, 50);
    }

    return [ inputRef, handleBlur, handleChange ];
}
