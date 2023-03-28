import * as React from "react";

import { Stomp, Client } from '@stomp/stompjs';

type MyState = {
   message: string;
   messages: string[];
};

export default class FirstComponent extends React.Component <{}, MyState> {

  private stompClient: Client | null = null;

  state: MyState = {
     message: "Blah",
     messages: [],
  };

  constructor(props: {}) {
     super(props);
  }

  componentDidMount() {
        var sessionId = this.generateMyUniqueSessionId();

        const client = new Client({
          brokerURL: 'ws://127.0.0.1:8080/stream-ws',
          onConnect: (frame) => {
            client.publish({
                destination: '/app/stream/',
                body: JSON.stringify({
                    sessionId: sessionId
                })
            });
            client.subscribe('/user/' + sessionId + '/user/stream/', this.handleMessage);
          },
        });
        client.activate();

        this.stompClient = client;
  }

  componentWillUnmount() {
     if (this.stompClient) {
       this.stompClient.deactivate();
       this.stompClient = null;
     }
   }

  handleMessage = (message: any) => {
     const data = new DataView(message.binaryBody.buffer);

     const frameNum = data.getUint32(0, false);
     const chunkSize = data.getUint32(4, false);
     var x = new Float32Array(chunkSize);
     var y = new Float32Array(chunkSize);
     var flakes = new Uint8Array(chunkSize);
     var ptr = 8;
     for (var i = 0; i < chunkSize; ++i) {
        x[i] = data.getFloat32(ptr, false);
        y[i] = data.getFloat32(ptr + 4, false);
        flakes[i] = data.getUint8(ptr + 8);
        ptr += 9;
     }

     console.log(frameNum, chunkSize, x, y, flakes);
  };

  handleError = (event: Event) => {
    console.error("WebSockets STOMP error:", event);
  };

  handleClick() {
    this.setState({ message: "bam" });
//     this.stompClient.publish({ destination: '/app/news', body: 'Next Message' });
  }

  generateMyUniqueSessionId(): string {
    return Math.random().toString(36).slice(2)
        + Math.random().toString(36).slice(2)
        + Math.random().toString(36).slice(2);
  }

  render() {
    const { message } = this.state;
    return (
      <div>
        <h3>A Simple React Component Example with Typescript</h3>
        <h2>{message}</h2>
        <hr/>
            <div>
                {this.state.messages.map((message, i) => (
                  <p key={i}>{message}</p>
                ))}
            </div>
        <hr/>

        <button onClick={this.handleClick.bind(this)}>
          Click me
        </button>

      </div>
    );
  }
}