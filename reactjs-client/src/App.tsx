import * as React from "react";
import { useState } from 'react';
import SnowAnimation from './components/SnowAnimation'
import { SnowSessionProvider } from './snow/SnowSessionProvider'

export default function App() {
    return (
        <>
          <h1>Snow Animation</h1>
          <SnowSessionProvider>
              <SnowAnimation
                    presetName="massiveSnow"
                    fps={1}
                    width={180}
                    height={80}
                    isAnimationRunning={true}
              />
          </SnowSessionProvider>
        </>
    );
}
