

export function validateSnowSessionId(sessionId : string) : string|null {
    if (sessionId == "") {
        return "Cannot be empty";
    }

    if (sessionId.match(/[A-Z]/)) {
        return "Only lowercase characters are allowed"
    }

    if (sessionId.match(/ /)) {
        return "Cannot have spaces"
    }

    if (!sessionId.match(/^[a-z0-9-]+$/)) {
        return "Only alphanumeric characters and dashes are allowed"
    }

    return null;
}


