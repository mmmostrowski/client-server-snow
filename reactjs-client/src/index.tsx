import * as React from "react";
import * as ReactDOM from "react-dom";
import FirstComponent from './components/FirstComponent'
import SnowAnimation from './components/SnowAnimation'


ReactDOM.render(
    <div>
      <h1>Snow Animation</h1>
      <SnowAnimation sessionId="session-xyz" presetName="massiveSnow" fps={33} />
    </div>,
    document.getElementById("root")
);

