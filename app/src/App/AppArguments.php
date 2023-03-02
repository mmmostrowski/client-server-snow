<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use TechBit\Snow\SnowFallAnimation\Wind\IWind;

final class AppArguments
{

    /**
     * @param string[]|class-string<IWind>[] $windForces
     */
    public function __construct(
        private readonly string $projectRootDir,
        private readonly bool $isDeveloperMode,
        private readonly array $windForces,
        private readonly string $presetName,
        private readonly ?string $customScene,
        private readonly ?string $serverSessionId,
        private readonly int $serverCanvasWidth,
        private readonly int $serverCanvasHeight,
    )
    {
    }

    public function projectRootDir(): string
    {
        return realpath($this->projectRootDir);
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
        return $this->windForces;
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

}