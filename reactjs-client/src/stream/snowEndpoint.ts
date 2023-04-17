
const snowEndpointUrl="http://127.0.0.1:8080"

interface SnowStreamEndpointAction {
    action: string,
    sessionId: string,
    fps?: number,
    width?: number,
    height?: number,
    presetName?: number,
}

async function askSnowEndpoint(action: SnowStreamEndpointAction) {
    let url = `${snowEndpointUrl}/${action.action}/${action.sessionId}`;

    url += action.fps ? "/fps/" + action.fps : ""
    url += action.fps ? "/width/" + action.width : ""
    url += action.fps ? "/height/" + action.height : ""
    url += action.fps ? "/presetName/" + action.presetName : ""

    return fetch(url)
        .then((response) => response.json());
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
