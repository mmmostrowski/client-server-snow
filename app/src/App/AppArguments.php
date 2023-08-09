<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use TechBit\Snow\SnowFallAnimation\Wind\IWind;

final class AppArguments
{

    /**
     * @param string[]|class-string<IWind>[]|null $windForces
     */
    public function __construct(
        private readonly bool $isDeveloperMode,
        private readonly ?array $windForces,
        private readonly string $presetName,
        private readonly ?string $customScene,
        private readonly int $targetFps,
        private readonly int $animationDurationSec,
        private readonly ?string $serverSessionId,
        private readonly int $serverCanvasWidth,
        private readonly int $serverCanvasHeight,
        private readonly String $serverPipesDir, 
    )
    {
    }

    public function targetFps(): int
    {
        return $this->targetFps;
    }

    public function animationDurationSec(): int
    {
        return $this->animationDurationSec;
    }

    public function isDeveloperMode(): bool
    {
        return $this->isDeveloperMode;
    }

    /**
     * @return string[]|class-string<IWind>[]
     */
    public function windForces(): array
    {
        if (implode('', $this->windForces) === '') {
            return [];
        }
        return (array)$this->windForces;
    }

    public function useDefaultWindForces(): bool
    {
        return $this->windForces === null;
    }

    public function presetName(): string
    {
        return $this->presetName;
    }

    public function customScene(): ?string
    {
        return $this->customScene;
    }

    public function isServer(): bool
    {
        return $this->serverSessionId != null;
    }

    public function serverSessionId(): string
    {
        return $this->serverSessionId;
    }

    public function serverCanvasWidth(): int
    {
        return $this->serverCanvasWidth;
    }

    public function serverCanvasHeight(): int
    {
        return $this->serverCanvasHeight;
    }

    public function serverPipesDir(): string
    {
        return $this->serverPipesDir;
    }

}