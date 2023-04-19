
const snowEndpointUrl="http://127.0.0.1:8080"

interface SnowStreamEndpointAction {
    action: string,
    sessionId: string,
    fps?: number,
    width?: number,
    height?: number,
    presetName?: string,
}

interface StartSnowStreamAction {
    sessionId: string,
    fps: number,
    width: number,
    height: number,
    presetName: string,
}

interface StopSnowStreamAction {
    sessionId: string,
}

async function askSnowEndpoint(action: SnowStreamEndpointAction) {
    let url = `${snowEndpointUrl}/${action.action}/${action.sessionId}`;

    url += action.fps ? "/fps/" + action.fps : ""
    url += action.fps ? "/width/" + action.width : ""
    url += action.fps ? "/height/" + action.height : ""
    url += action.fps ? "/presetName/" + action.presetName : ""

    return fetch(url)
        .then((response) => response.json())
        .then((data) => {
            if (!data) {
                throw Error("Invalid server response!");
            }
            if (!data.status) {
                if (data.message) {
                    throw Error("Error respond with error: " + data.message);
                }
                throw Error("Error respond with error!");
            }
            console.log(url, data);
            return data;
        })
        ;
}

export async function fetchSnowDataDetails(sessionId: string) {
    if (!sessionId) {
        return {
            running: false,
        };
    }
    return askSnowEndpoint({
        action: "details",
        sessionId: sessionId,
    });
}

export async function startStreamSnowData(action: StartSnowStreamAction) {
    return askSnowEndpoint({
        action: 'start',
        ...action,
    });
}

export async function stopStreamSnowData(action: StopSnowStreamAction) {
    return askSnowEndpoint({
        action: 'stop',
        ...action,
    });
}
