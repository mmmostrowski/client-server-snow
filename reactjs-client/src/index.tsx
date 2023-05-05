import * as React from "react";
import * as ReactDOMClient from 'react-dom/client';
import App from './App'
import './index.css'
import {SessionsProvider} from './snow/SessionsProvider'
import {applicationConfig} from "./config/application";


const container = document.getElementById('root');
const root = ReactDOMClient.createRoot(container);
const requestedSessionId = window.location.pathname.substring(1);
const initialSessionId = requestedSessionId ? requestedSessionId : applicationConfig.defaultSessionId;

root.render(
    <SessionsProvider initialSessionId={initialSessionId} >
        <App maxTabs={5} />
    </SessionsProvider>
);
