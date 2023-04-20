import * as React from "react";
import { useState, forwardRef, useEffect, MouseEvent, HTMLProps } from 'react';
import { useSnowSessions, useSessionsManager } from './snow/SnowSessionsProvider'
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

export default function App({ maxTabs } : AppProps): JSX.Element {
    const [ currentTab, setCurrentTab ] = useState(0);
    const sessions = useSnowSessions();
    const { createNewSession, deleteSession } = useSessionsManager();

    useUrlSwitcher(currentTab);

    function handleNewSession(): void {
        if (sessions.length >= maxTabs) {
            alert(`You can have maximum ${maxTabs} tabs opened!`)
            return;
        }
        createNewSession();
    }

    function handleDeleteSession(e: MouseEvent<HTMLElement>, sessionIdx: number): void {
        e.stopPropagation();
        const sessionId = sessions[sessionIdx].sessionId;
        if (!window.confirm(`Are you sure you want to delete session ${sessionId}?`)) {
            return;
        }
        deleteSession(sessionIdx);
        if (currentTab >= sessionIdx) {
             setCurrentTab(currentTab > 0 ? currentTab - 1 : 0);
        }
    }

    function handleTabChange(e: MouseEvent<HTMLElement>, newTabIdx: number): void {
        if (newTabIdx >= maxTabs) {
            return;
        }
        setCurrentTab(newTabIdx);
    }

    interface TabButtonProps extends HTMLProps<HTMLDivElement> {
        tabIdx: number;
    }

    const TabButton = forwardRef<HTMLDivElement, TabButtonProps>(
        ({ tabIdx, children, ...divProps }, ref ): JSX.Element => (
            <span ref={ref}>
                <div role="button" {...divProps} >
                    <IconButton className="tab-delete-button" onClick={ e => { handleDeleteSession(e, tabIdx) } }  >
                        <ClearIcon fontSize="inherit" />
                    </IconButton>
                    {children}
                </div>
            </span>
    ));

    return (
        <>
            <div className="snow-animation-header" >
                <h1>Snow Animation</h1>
                <Paper elevation={2} sx={{ mt: 1 }} >
                        <Tabs value={sessions.length > 0 ? currentTab : false} onChange={handleTabChange} >
                        {
                            sessions.map((s, idx) =>
                                <Tab key={idx}
                                     tabIdx={idx}
                                     component={TabButton}
                                     label={s.validatedSessionId}
                                     sx={{ borderRight: 1, borderColor: 'divider' }}
                                 />
                            )
                        }
                            <Tooltip title="Add new session" >
                                <Tab key="new"
                                     label="+"
                                     onClick={handleNewSession}
                                     className="add-new-session-button"
                                     sx={{ height: 40,
                                           backgroundColor: 'primary.main', color: 'primary.contrastText',
                                           "&:hover": { backgroundColor: 'primary.dark', color: 'primary.contrastText' },
                                           "&:selected": { backgroundColor: 'primary.dark', color: 'primary.contrastText' }}}
                               />
                            </Tooltip>
                        </Tabs>
                </Paper>
            </div>
            {
                sessions.map((s, idx) =>
                    currentTab === idx &&
                    (
                        <div key={idx} className="snow-session-wrapper" >
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
                                        refreshPeriodMs={1000}
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

function useUrlSwitcher(currentTab: number) {
    const sessions = useSnowSessions();
    const currentSessionId = sessions[currentTab]?.validatedSessionId;

    useEffect(() => {
        if (currentSessionId !== undefined) {
            window.history.replaceState({
                session: currentSessionId,
            }, "Session: " + currentSessionId, "/" + currentSessionId)
        }
    }, [ currentSessionId]);
}
