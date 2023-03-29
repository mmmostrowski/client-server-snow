import * as React from "react";

import { Stomp, Client } from '@stomp/stompjs';

type MyState = {
   message: string;
   messages: string[];
   isFirstFrame: boolean;
};

export default class FirstComponent extends React.Component <{}, MyState> {

  private stompClient: Client | null = null;

  state: MyState = {
     isFirstFrame: true,
     message: "Blah",
     messages: [],
  };

  constructor(props: {}) {
     super(props);
  }

  set isFirstFrame(value: boolean) {
     this.setState({ isFirstFrame: value });
  }

  get isFirstFrame() {
     return this.state.isFirstFrame;
  }

  componentDidMount() {
        var clientId = this.generateMyUniqueSessionId();
        var sessionId = "session-abc";

        fetch('http://127.0.0.1:8080/start/' + sessionId + '/fps/3')
             .then((response) => response.json())
             .then((data) => data.running)
             .then((running) => {
                if (!running) {
                    throw "Snow session is not running!"
                }

                this.isFirstFrame = true;
                const client = new Client({
                  brokerURL: 'ws://127.0.0.1:8080/ws/',
                  onConnect: (frame) => {
                    client.publish({
                        destination: '/app/stream/' + sessionId,
                        body: clientId
                    });
                    client.subscribe('/user/' + clientId + '/user/stream/', this.handleMessage.bind(this));
                  },
                });
                client.activate();

                this.stompClient = client;
             })
             .catch((err) => {
                console.log(err.message);
             });
  }

  componentWillUnmount() {
     if (this.stompClient) {
       this.stompClient.deactivate();
       this.stompClient = null;
     }
   }

  handleMessage(message: any) {
     const data = new DataView(message.binaryBody.buffer);
     if (this.isFirstFrame) {
         this.isFirstFrame = false;
         this.handleSnowMetadata(data);
     } else {
         this.handleSnowDataFrame(data);
     }
  };

  handleSnowMetadata(data : DataView) {
  }

  handleSnowDataFrame(data : DataView) {
     const frameNum = data.getInt32(0, false);
     const chunkSize = data.getUint32(4, false);
     var x = new Float32Array(chunkSize);
     var y = new Float32Array(chunkSize);
     var flakes = new Uint8Array(chunkSize);
     var ptr = 8;
     console.log("chunkSize", chunkSize);
     for (var i = 0; i < chunkSize; ++i) {
        x[i] = data.getFloat32(ptr, false);
        y[i] = data.getFloat32(ptr + 4, false);
        flakes[i] = data.getUint8(ptr + 8);
        ptr += 9;
     }

     console.log(frameNum);
//      console.log(frameNum, chunkSize, x, y, flakes);
  }

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