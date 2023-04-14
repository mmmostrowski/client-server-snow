import * as React from "react";
import { useState, useRef, forwardRef, useEffect } from 'react';
import { useSnowSessions, useSnowSessionsDispatch } from './snow/SnowSessionsProvider'
import SnowAnimation from './components/SnowAnimation'
import SnowConfiguration from './components/SnowConfiguration'
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import Tooltip from '@mui/material/Tooltip';
import IconButton from '@mui/material/IconButton';
import ClearIcon from '@mui/icons-material/Clear';
import Paper from '@mui/material/Paper';


interface AppProps {
    maxTabs : number
}

export default function App({ maxTabs } : AppProps) {
    const [ currentTab, setCurrentTab ] = useState(0);
    const [ isInitialized, setInitialized ] = useState(false);
    const createdCount = useRef(1);
    const sessions = useSnowSessions();
    const dispatch = useSnowSessionsDispatch();
    const currentSessionId=sessions[currentTab] !== undefined
        ? sessions[currentTab].validatedSessionId
        : undefined;

    useEffect(() => {
        if (!isInitialized) {
            setInitialized(true);
            const initialSessionId = window.location.pathname.substring(1);
            if (initialSessionId !== "") {
                // TODO: start session initialSessionId
            }
            return;
        }
        window.history.pushState({
            session: currentSessionId,
        }, "Session: " + currentSessionId, "/" + currentSessionId)
    }, [ currentSessionId, isInitialized ]);

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
            setCurrentTab(newTabIdx);
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
            <div className="snow-animation-header" >
                <h1>Snow Animation</h1>
                <Paper elevation={2} sx={{ mt: 1 }} >
                        <Tabs value={sessions.length > 0 ? currentTab : undefined} onChange={handleTabChange}  >
                        {
                            sessions.map((s, idx) =>
                                <Tab key={idx}
                                     label={s.validatedSessionId}
                                     data-value={idx}
                                     component={TabButton}
                                     sx={{ borderRight: 1, borderColor: 'divider' }}
                                     />
                            )
                        }
                            <Tooltip title="Add new session" >
                                <Tab key="new" label="+" className="add-new-session-button" onClick={handleNewSession}
                                    sx={{   height: 40,
                                            backgroundColor: 'primary.main', color: 'primary.contrastText',
                                           "&:hover": { backgroundColor: 'primary.dark', color: 'primary.contrastText' },
                                           "&:selected": { backgroundColor: 'primary.dark', color: 'primary.contrastText' },
                                        }} />
                            </Tooltip>
                        </Tabs>
                </Paper>
            </div>
            {
                sessions.map((s, idx) =>
                    currentTab === idx &&
                    (
                        <div key={s.validatedSessionId} className="snow-session-wrapper" >
                            <div className="snow-configuration-wrapper" >
                                <Paper elevation={3} sx={{ mt: 1 }} >
                                    <SnowConfiguration
                                        key={idx}
                                        sessionIdx={idx}
                                     />
                                </Paper>
                            </div>
                            <div className="snow-animation-wrapper" >
                                <Paper elevation={3} sx={{ marginTop: 1 }} >
                                    <SnowAnimation
                                        key={idx}
                                        sessionIdx={idx}
                                    />
                                </Paper>
                            </div>
                        </div>
                    )
                )
            }
        </>
    );
}
