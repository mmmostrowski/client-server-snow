
export function validateSnowSessionId(sessionId : string): string|null {
    if (sessionId === "") {
        return "Cannot be empty";
    }

    if (sessionId.match(/[A-Z]/)) {
        return "Allowed only lowercase characters"
    }

    if (sessionId.match(/ /)) {
        return "Cannot have spaces"
    }

    if (!sessionId.match(/^[a-z0-9-]+$/)) {
        return "Allowed only alphanumeric characters and dashes"
    }

    return null;
}

export function validateNumberBetween(value: string, min : number, max : number): string|null {
    const number = Number(value);

    if (isNaN(number)) {
        return "Expected number";
    }

    if (!Number.isInteger(number)) {
        return "Only whole numbers";
    }

    if (number < min || number > max) {
        return `Allowed only numbers between ${min} and ${max}`;
    }

    return null;
}

