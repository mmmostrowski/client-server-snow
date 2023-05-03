import { Client, IMessage } from '@stomp/stompjs';

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

export const AbortedDetailsEndpointResponse: DetailsEndpointResponse = {
    ...AbortedEndpointResponse,
    message: "Request has been aborted.",
    streamTextUrl: "",
    streamWebsocketsStompBrokerUrl: "",
    streamWebsocketsUrl: "",
    exists: false,
    duration: 0,
    presetName: "",
    width: 0,
    height: 0,
    fps: 0,
};

export function startSnowSession(sessionId: string, config: SnowAnimationConfiguration, controller?: AbortController): Promise<StartEndpointResponse> {
    const url= "/fps/" + config.fps
        + "/width/" + config.width
        + "/height/" + config.height
        + "/presetName/" + config.presetName;

    return askSnowEndpoint(controller, 'start', sessionId, url)
            .then(( response: StartEndpointResponse ) => {
                if (!response.running) {
                    throw Error("Server did not start animation!");
                }
                return response;
            }) as Promise<StartEndpointResponse>;
}

export function fetchSnowDetails(sessionId: string, controller?: AbortController): Promise<DetailsEndpointResponse> {
    if (!sessionId) {
        return Promise.reject(new Error("Session id is missing!"));
    }
    return askSnowEndpoint(controller, 'details', sessionId) as Promise<DetailsEndpointResponse>;
}

export function stopSnowSession(sessionId: string, controller?: AbortController): Promise<StopEndpointResponse>{
    return askSnowEndpoint(controller, 'stop', sessionId)
            .then(( response: StartEndpointResponse ) => {
                if (response.running) {
                    throw Error("Server did not stop animation!");
                }
                return response;
            }) as Promise<StartEndpointResponse>;
}

function askSnowEndpoint(controller: AbortController, action: string, sessionId: string, subUrl: string = ""): Promise<EndpointResponse> {
    const url = `${snowEndpointUrl}/${action}/${sessionId}${subUrl}`;

    return fetch(url, { signal: controller?.signal })
        .then((response: Response) => {
            if (!response) {
                throw Error("Invalid server response!");
            }
            return response;
        })
        .then((response: Response) => response.json())
        .then((data: EndpointResponse) => {
            if (!data) {
                throw Error("Invalid server response! JSON Response expected!");
            }
            if (!data.status) {
                if (data.message) {
                    throw Error("Server respond with error: " + data.message);
                }
                throw Error("Server respond with error!");
            }
            // console.log(url, data);
            return data;
        })
        .catch((error: Error) => {
            if (error.name === 'AbortError') {
                return AbortedEndpointResponse;
            }
            console.error(error);
            throw error;
        });
}

export type SnowClientHandler = number;
const stompClients = new Map<SnowClientHandler, Client>();
let stompClientsCounter = 0;

export function startSnowDataStream(startSessionResponse: StartEndpointResponse, handleMessage: (data: DataView) => void): SnowClientHandler {
    const stompClient = new Client({
        brokerURL: startSessionResponse.streamWebsocketsStompBrokerUrl,
        onConnect: (frame) => {
            let userId = frame.headers['user-name'];

            stompClient.subscribe('/user/' + userId + '/stream/',
                (message: IMessage) => handleMessage(new DataView(message.binaryBody.buffer)));

            stompClient.publish({
                destination: startSessionResponse.streamWebsocketsUrl,
            });
        },
    });
    stompClient.activate();

    const handler = ++stompClientsCounter;
    stompClients.set(handler, stompClient);
    return handler;
}

export function stopSnowDataStream(handler: SnowClientHandler): void {
    stompClients.get(handler).deactivate();
    stompClients.delete(handler);
}