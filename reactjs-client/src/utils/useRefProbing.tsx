import {MutableRefObject, useEffect, useState} from "react";

export function useRefProbing<T = undefined>(ref: MutableRefObject<T | undefined>, timeout: number): T | undefined {
    const [value, setValue] = useState<T | undefined>(ref.current);

    useEffect(() => {
        setValue(ref.current);
        const timer = setInterval(() => {
            setValue(ref.current);
        }, timeout);
        return () => {
            clearInterval(timer);
        };
    }, [ref, timeout]);

    return value;
}