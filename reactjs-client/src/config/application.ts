import {animationSceneLabels} from "../snow/animationScenes";
import "../config/scenes/scene_a"
import "../config/scenes/scene_b"

export const applicationConfig = {
    defaultSessionId: "session-abc",

    minWidth: 40,
    minHeight: 20,
    defaultWidth: 200,
    defaultHeight: 90,
    maxWidth: 700,
    maxHeight: 350,

    minFps: 1,
    defaultFps: 33,
    maxFps: 60,

    defaultPreset: "slideshow:random",
    presets: {
        "slideshow:random": "Shuffle",
        classical: "Classical",
        massiveSnow: "Massive Snow",
        calm: "Calm",
        windy: "Windy",
        snowy: "Snowy",
        noSnow: "No snow",
    },
    defaultScene: "scene_b",
    scenes: animationSceneLabels(),
};