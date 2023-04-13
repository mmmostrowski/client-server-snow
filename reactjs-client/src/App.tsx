import * as React from "react";
import { useState, useRef, forwardRef } from 'react';
import { useSnowSessions, useSnowSessionsDispatch } from './snow/SnowSessionsProvider'
import SnowAnimation from './components/SnowAnimation'
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import ClearIcon from '@mui/icons-material/Clear';

interface AppProps {
    maxTabs : number
}

export default function App({ maxTabs } : AppProps) {
    const [ currentTab, setCurrentTab ] = useState(0);
    const createdCount = useRef(1);
    const sessions = useSnowSessions();
    const dispatch = useSnowSessionsDispatch();

    function handleNewSession() {
        if (sessions.length >= maxTabs) {
            alert('You can have maximum ' + maxTabs + ' tabs opened!')
            return;
        }

         dispatch({
            type: 'new-session',
            newSessionId : 'session-' + createdCount.current++,
        })
    }

    function handleDeleteSession(e : any, value : number) {
        e.stopPropagation();
        const sessionId = sessions[value].sessionId;
        if (window.confirm('Are you sure you want to delete session ' + sessionId + ' ?')) {
             if (currentTab >= value) {
                 setCurrentTab(currentTab > 0 ? currentTab - 1 : 0);
             }
             dispatch({
                 type: 'delete-session',
                 sessionIdx: value
             })
        }
    }

    function handleTabChange(e : any, newTabIdx : number) {
        if (newTabIdx < maxTabs) {
            setCurrentTab(newTabIdx)
        }
    }

    const TabButton = forwardRef<HTMLDivElement, any>((props, ref) => (
        <span ref={ref}>
            <div role="button" {...props} >
                <IconButton className="tab-delete-button" onClick={(e) => { handleDeleteSession(e, props['data-value'])} }  >
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
                <Tabs value={currentTab} onChange={handleTabChange}  >
                {
                    sessions.map((s, idx) =>
                        <Tab key={idx}
                             label={s.sessionId !== "" ? s.sessionId : '?'}
                             data-value={idx}
                             component={TabButton} /> )
                }
                <Tab key="new" label="+" className="add-new-session-button" onClick={handleNewSession}
                        sx={{  backgroundColor: 'primary.main', color: 'primary.contrastText',
                               "&:hover": { backgroundColor: 'primary.dark' }  }} />
                </Tabs>
            </Box>
            {
                sessions.map((s, idx) =>
                    currentTab === idx && <SnowAnimation
                        key={idx}
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
