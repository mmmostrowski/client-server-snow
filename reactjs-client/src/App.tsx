import * as React from "react";
import { useState, useRef, forwardRef } from 'react';
import SnowAnimation from './components/SnowAnimation'
import { SnowSessionsProvider, useSnowSessions, useSnowSessionsDispatch } from './snow/SnowSessionsProvider'
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import ClearIcon from '@mui/icons-material/Clear';

interface AppProps {
    maxTabs : number
}

export default function App({ maxTabs } : AppProps) {
    const [ currentTab, setCurrentTab ] = useState(0);
    const createdCount = useRef(0);
    const sessions = useSnowSessions();
    const dispatch = useSnowSessionsDispatch();

    function handleNewSession() {
        if (sessions.length >= maxTabs) {
            alert('You can have maximum ' + maxTabs + ' tabs opened!')
            return;
        }

        createdCount.current = createdCount.current + 1;
        dispatch({
            type: 'new-session',
            newSessionId : 'session-' + createdCount.current,
        })
    }

    function handleDeleteSession(e : any, value : number) {
        e.stopPropagation();
        if (window.confirm('Are you sure you want to delete session ' + e.target.parentElement.parentElement.innerText + ' ?')) {
             dispatch({
                 type: 'delete-session',
                 sessionIdx: value
             })
             if (currentTab >= value) {
                 setCurrentTab(currentTab > 0 ? currentTab - 1 : 0);
             }
        }
    }

    const TabButton = forwardRef<HTMLDivElement, any>((props, ref) => (
        <span ref={ref}>
            <div role="button" {...props} >
                <IconButton onClick={(e) => { handleDeleteSession(e, props['data-value'])} }
                    sx={{ position: 'absolute', right: -7, top: -6, verticalAlign: 'top', fontSize: 13, color: 'red' }} >
                    <ClearIcon fontSize="inherit" />
                </IconButton>
                {props.children}
            </div>
        </span>
    ));

    return (
        <>
            <h1>Snow Animation</h1>
            <Box sx={{ border: 1, borderColor: 'divider' }}>
              <Tabs value={currentTab} onChange={(e, newValue) => setCurrentTab(newValue)}  >
                {
                    sessions.map((s, idx) =>
                        <Tab label={s.sessionId !== "" ? s.sessionId : '?'} data-value={idx} component={TabButton} /> )
                }
                <Button onClick={handleNewSession} variant="contained" sx={{ fontWeight: 'bold', fontSize: 18 }} >
                    +
                </Button>
              </Tabs>
            </Box>
            {
                sessions.map((s, idx) =>
                    currentTab === idx && <SnowAnimation
                        sessionIdx={idx}
                        presetName="massiveSnow"
                        fps={1}
                        width={180}
                        height={80}
                        isAnimationRunning={true} />
                )
            }
        </>
    );
}
