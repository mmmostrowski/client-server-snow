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

        const client = new Client({
          webSocketFactory: () => {
              const ws = new WebSocket('ws://127.0.0.1:8080/mywebsockets');
              ws.binaryType = 'arraybuffer';
              return ws;
          },
          onConnect: () => {
            client.publish({ destination: '/app/news', body: 'First Message' });
            client.subscribe('/topic/news2', this.handleMessage);
          },
        });

        client.activate();

        this.stompClient = client;
  }

  componentWillUnmount() {
     if (this.stompClient) {
       this.stompClient.deactivate();
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

     console.log(frameNum);
     console.log(chunkSize);
     console.log(x);
     console.log(y);
     console.log(flakes);
  };

  handleError = (event: Event) => {
    console.error("STOMP error:", event);
  };

  handleClick() {
    this.setState({ message: "bam" });
    this.stompClient.publish({ destination: '/app/news', body: 'Next Message' });
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