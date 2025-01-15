<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class TestPerformancePreset extends DefaultConfig
{

    public function showFps(): bool
    {
        return true;
    }

    public function snowProducingTempo(): int
    {
        return parent::snowProducingTempo() * 150;
    }

    public function snowMaxNumOfFlakesAtOnce(): int
    {
        return parent::snowMaxNumOfFlakesAtOnce() * 150;
    }

    public function snowHowManyFlakesNeedsToFallToFormAHill(): int
    {
        return parent::snowHowManyFlakesNeedsToFallToFormAHill() * 30;
    }

}
