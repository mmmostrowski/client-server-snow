
const snowEndpointUrl="http://127.0.0.1:8080"

export interface SnowAnimationConfiguration {
    presetName: string,
    width: number,
    height: number,
    fps: number,
}

interface SnowStreamEndpointAction {
    action: string,
    sessionId: string,
    fps?: number,
    width?: number,
    height?: number,
    presetName?: string,
}

interface SnowStreamEndpointResponse {
    status: boolean,
    sessionId: string,
    running: boolean,
}

export interface SnowStreamDetailsResponse extends SnowStreamEndpointResponse {
    exists: boolean,
    message: string,
    streamTextUrl: string,
    streamWebsocketsStompBrokerUrl: string,
    streamWebsocketsUrl: string,
    width?: number,
    height?: number,
    fps?: number,
    presetName?: string,
    duration?: number,
}

export interface SnowStreamStartResponse extends SnowStreamDetailsResponse {
}

export interface SnowStreamStopResponse extends SnowStreamEndpointResponse {
}

interface SnowStreamAction {
    sessionId: string,
}

interface StartSnowStreamAction extends SnowAnimationConfiguration, SnowStreamAction {
}

interface StopSnowStreamAction extends SnowStreamAction {
}

async function askSnowEndpoint(action: SnowStreamEndpointAction, controller?: AbortController): Promise<SnowStreamEndpointResponse> {
    let url = `${snowEndpointUrl}/${action.action}/${action.sessionId}`;

    url += action.fps ? "/fps/" + action.fps : ""
    url += action.fps ? "/width/" + action.width : ""
    url += action.fps ? "/height/" + action.height : ""
    url += action.fps ? "/presetName/" + action.presetName : ""

    return fetch(url, controller)
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
//             console.log(url, data);
            return data;
        });
}

export async function fetchSnowDataDetails(sessionId: string, controller?: AbortController): Promise<SnowStreamDetailsResponse> {
    if (!sessionId) {
        return {
            running: false,
            exists: false,
            status: false,
            message: "Session id is missing!",
            streamTextUrl: "",
            streamWebsocketsStompBrokerUrl: "",
            streamWebsocketsUrl: "",
            sessionId: "",
        };
    }
    return askSnowEndpoint({
        action: "details",
        sessionId: sessionId,
    }) as Promise<SnowStreamDetailsResponse>;
}

export async function startStreamSnowData(action: StartSnowStreamAction, controller?: AbortController): Promise<SnowStreamStartResponse> {
    return askSnowEndpoint({
        action: 'start',
        ...action,
    }) as Promise<SnowStreamStartResponse>;
}

export async function stopStreamSnowData(action: StopSnowStreamAction, controller?: AbortController): Promise<SnowStreamStopResponse>{
    return askSnowEndpoint({
        action: 'stop',
        ...action,
    }) as Promise<SnowStreamStopResponse>;
}
