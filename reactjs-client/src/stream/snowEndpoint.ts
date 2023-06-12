import {Client, IFrame, IMessage} from '@stomp/stompjs';

const snowEndpointUrl=process.env.REACT_APP_SNOW_ENDPOINT_URL;

export interface SnowAnimationConfiguration {
    presetName: string,
    width: number,
    height: number,
    fps: number,
}

interface EndpointResponse {
    status: boolean,
    sessionId: string,
    running: boolean,
    message?: string,
}

export interface DetailsEndpointResponse extends EndpointResponse, SnowAnimationConfiguration {
    exists: boolean,
    message: string,
    streamTextUrl: string,
    streamWebsocketsStompBrokerUrl: string,
    streamWebsocketsUrl: string,
    duration: number,
}

export type StartEndpointResponse = DetailsEndpointResponse;
export type StopEndpointResponse = EndpointResponse;

export const AbortedEndpointResponse: EndpointResponse = {
    status: false,
    sessionId: "",
    running: false,
    message: "Request has been aborted.",
}

export async function startSnowSession(sessionId: string, config: SnowAnimationConfiguration, controller?: AbortController)
    : Promise<StartEndpointResponse>
{
    const url= "/fps/" + config.fps
        + "/width/" + config.width
        + "/height/" + config.height
        + "/presetName/" + config.presetName;

    const response = await askSnowEndpoint(controller, 'start', sessionId, url);
    if (!response.running) {
        throw Error("Server did not start animation!");
    }
    return response as StartEndpointResponse;
}

export async function fetchSnowDetails(sessionId: string, controller?: AbortController): Promise<DetailsEndpointResponse> {
    return await askSnowEndpoint(controller, 'details', sessionId) as DetailsEndpointResponse;
}

export async function stopSnowSession(sessionId: string, controller?: AbortController): Promise<StopEndpointResponse> {
    const response = await askSnowEndpoint(controller, 'stop', sessionId);
    if (response.running) {
        throw Error("Server did not stop animation!");
    }
    return response as StopEndpointResponse;
}

async function askSnowEndpoint(controller: AbortController, action: string, sessionId: string, subUrl: string = "")
    : Promise<EndpointResponse>
{
    if (!sessionId) {
        throw Error("Session id is missing!");
    }

    if (!action) {
        throw Error("Action is missing!");
    }

    const data: EndpointResponse = await fetchEndpoint(controller,
        `${snowEndpointUrl}/${action}/${sessionId}${subUrl}`);

    if (!data) {
        throw Error("Invalid server response! JSON Response expected!");
    }

    if (data === AbortedEndpointResponse) {
        return data;
    }

    if (data.status === false) {
        throw Error(data.message
            ? "Server respond with error: " + data.message
            : "Server respond with error!" );
    }

    return data;
}

async function fetchEndpoint(controller: AbortController, url: string): Promise<EndpointResponse> {
    try{
        const response: Response = await fetch(url, { signal: controller?.signal });
        return await response.json() as EndpointResponse;
    } catch (error) {
        switch(error.name) {
            case 'AbortError':
                return AbortedEndpointResponse;
            case 'TypeError':
                console.error(error);
                error = new Error("Server error!");
        }
        throw error;
    }
}


export type SnowClientHandler = number;
const stompClients = new Map<SnowClientHandler, Client>();
let stompClientsCounter = 0;

export function startSnowDataStream(startResponse: StartEndpointResponse, handleMessage: (data: DataView) => void): SnowClientHandler {
    const stompClient = new Client({
        brokerURL: startResponse.streamWebsocketsStompBrokerUrl,
        onConnect: (frame: IFrame) => {
            const userId = frame.headers['user-name'];

            stompClient.subscribe('/user/' + userId + '/stream/',
                (message: IMessage) => handleMessage(new DataView(message.binaryBody.buffer)));

            stompClient.publish({
                destination: startResponse.streamWebsocketsUrl,
            });
        },
    });

    stompClient.activate();

    const handler: SnowClientHandler = stompClientsCounter++;
    stompClients.set(handler, stompClient);
    return handler;
}

export function stopSnowDataStream(handler: SnowClientHandler): void {
    void stompClients.get(handler).deactivate();
    stompClients.delete(handler);
}