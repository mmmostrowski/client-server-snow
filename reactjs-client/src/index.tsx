import * as React from "react";
import * as ReactDOMClient from 'react-dom/client';
import App from './App'
import './index.css'
import { SnowSessionsProvider, useSnowSessions, useSnowSessionsDispatch } from './snow/SnowSessionsProvider'


const container = document.getElementById('root');
const root = ReactDOMClient.createRoot(container);
const initialSessionId = window.location.pathname.substring(1);

root.render(
    <SnowSessionsProvider initialSessionId={initialSessionId} >
        <App maxTabs={5} />
    </SnowSessionsProvider>
);
