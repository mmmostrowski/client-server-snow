import * as React from "react";
import * as ReactDOMClient from 'react-dom/client';
import App from './App'
import './index.css'
import { SnowSessionsProvider, useSnowSessions, useSnowSessionsDispatch } from './snow/SnowSessionsProvider'


const container = document.getElementById('root');
const root = ReactDOMClient.createRoot(container);

root.render(
    <SnowSessionsProvider>
        <App maxTabs={5} />
    </SnowSessionsProvider>
);
