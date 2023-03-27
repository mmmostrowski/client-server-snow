import * as React from "react";
import * as ReactDOM from "react-dom";
import FirstComponent from './components/FirstComponent'
import UserComponent from './components/UserComponent'
ReactDOM.render(
    <div>
      <h1>Hello ZIOMEK, Welcome to React and TypeScript</h1>
      <FirstComponent/>
      <UserComponent name="Maciej Ostrowski" age={37.5} address="87 Summer St, Boston, MA 02110" dob={new Date()} />
    </div>,
    document.getElementById("root")
);

