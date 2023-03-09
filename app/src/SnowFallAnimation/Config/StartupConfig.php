<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;


final class StartupConfig
{

    public function __construct(
        protected readonly int $targetFps,
        protected readonly int $animationDurationSec,
    ) {

    }
    
    public function minRequiredConsoleWidth(): int
    {
        return 170;
    }

    public function minRequiredConsoleHeight(): int
    {
        return 40;
    }

    public function targetFps(): int
    {
        return $this->targetFps;
    }

    public function animationDurationInFrames(): int
    {
        return $this->animationDurationSec * $this->targetFps;
    }

    public function showScene(): bool
    {
        return true;
    }

    public function sliderMinDurationSec(): int
    {
        return 5;
    }

    public function sliderMaxDurationSec(): int
    {
        return 25;
    }

    public function sliderMinFadeTimeSec(): int
    {
        return 3;
    }

    public function sliderMaxFadeTimeSec(): int
    {
        return 15;
    }

}