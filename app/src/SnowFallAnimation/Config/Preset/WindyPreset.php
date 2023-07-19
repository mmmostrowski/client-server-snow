<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class WindyPreset extends DefaultConfig
{

    public function snowProducingTempo(): int
    {
        return parent::snowProducingTempo() * 5;
    }

    public function snowMaxNumOfFlakesAtOnce(): int
    {
        return parent::snowMaxNumOfFlakesAtOnce() * 4;
    }

    public function windBlowsMaxStrength(): int
    {
        return parent::windBlowsMaxStrength() * 2;
    }

    public function windBlowsMinStrength(): int
    {
        return parent::windBlowsMinStrength() * 2;
    }

    public function windBlowsMaxNumAtSameTime(): int
    {
        return 3;
    }

    public function windBlowsMinAnimationDuration(): int
    {
        return (int)(parent::windBlowsMinAnimationDuration() / 2);
    }

    public function windBlowsMaxAnimationDuration(): int
    {
        return parent::windBlowsMaxAnimationDuration() * 2;
    }

    public function windBlowsFrequency(): int
    {
        return parent::windBlowsFrequency() * 2;
    }

    public function windGlobalStrengthMax(): float
    {
        return parent::windGlobalStrengthMax() * 2;
    }

    public function windGlobalStrengthMin(): float
    {
        return parent::windGlobalStrengthMin() / 2;
    }

    public function windGlobalVariation(): float
    {
        return parent::windGlobalVariation() * 1.5;
    }

    public function windFieldStrengthMax(): int
    {
        return parent::windFieldStrengthMax() * 3;
    }

    public function windFieldStrengthMin(): int
    {
        return parent::windFieldStrengthMin() * 3;
    }

}