import * as React from "react";
import * as ReactDOM from "react-dom";
import SnowAnimation from './components/SnowAnimation'


ReactDOM.render(
    <div>
      <h1>Snow Animation</h1>
      <SnowAnimation sessionId="session-xyz" presetName="massiveSnow" fps={3} />
    </div>,
    document.getElementById("root")
);

