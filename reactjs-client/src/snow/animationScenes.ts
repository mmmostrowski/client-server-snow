export interface AnimationScene {
    key: string;
    label: string;
    scene: string;
}

const animationScenes = new Map<string, AnimationScene>();

export function addAnimationScene(scene: AnimationScene): void {
    animationScenes.set(scene.key, scene);
}

export function animationSceneToBase64(sceneKey: string): string {
    if (!animationScenes.has(sceneKey)) {
        return "";
    }
    return btoa(animationScenes.get(sceneKey).scene);
}

export function animationSceneFromBase64(base64: string): string {
    for (const key of Array.from(animationScenes.keys())) {
        if (animationSceneToBase64(key) === base64) {
            return key;
        }
    }
    return ""
}

export function animationSceneLabels(): Record<string, string> {
    return Object.fromEntries(
        Array.from(animationScenes, ([key, scene]) => [ key, scene.label ])
    );
}
