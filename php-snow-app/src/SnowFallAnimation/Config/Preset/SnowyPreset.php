<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class SnowyPreset extends DefaultConfig
{

    public function snowProducingTempo(): int
    {
        return parent::snowProducingTempo() * 10;
    }

    public function snowProbabilityOfProducingFromTop(): int
    {
        return parent::snowProbabilityOfProducingFromTop() - 10;
    }

    public function snowMaxNumOfFlakesAtOnce(): int
    {
        return (int)(parent::snowMaxNumOfFlakesAtOnce() / 4);
    }

    public function snowIsPressedAfterFramesNumMin(): int
    {
        return parent::snowIsPressedAfterFramesNumMin() * 3;
    }

    public function snowIsPressedAfterFramesNumMax():int
    {
        return parent::snowIsPressedAfterFramesNumMax() * 3;
    }

}