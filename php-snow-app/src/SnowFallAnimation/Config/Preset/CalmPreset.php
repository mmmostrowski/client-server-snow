<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class CalmPreset extends DefaultConfig
{

    public function snowProducingTempo(): int
    {
        return (int)(parent::snowProducingTempo() / 4);
    }

    public function windBlowsMaxStrength(): int
    {
        return (int)(parent::windBlowsMaxStrength() / 3);
    }

    public function windBlowsMinStrength(): int
    {
        return (int)(parent::windBlowsMinStrength() / 3);
    }

    public function windBlowsFrequency(): int
    {
        return (int)(parent::windBlowsFrequency() / 2);
    }

    public function windBlowsMaxAnimationDuration(): int
    {
        return (int)(parent::windBlowsMaxAnimationDuration() / 6);
    }

    public function windBlowsMinAnimationDuration(): int
    {
        return (int)(parent::windBlowsMinAnimationDuration() / 6);
    }

    public function windFieldStrengthMax(): int
    {
        return (int)(parent::windFieldStrengthMax() / 10);
    }

    public function windFieldStrengthMin(): int
    {
        return (int)(parent::windFieldStrengthMin() / 10);
    }

    public function windGlobalStrengthMin(): float
    {
        return parent::windGlobalStrengthMin() / 10;
    }

    public function windGlobalStrengthMax(): float
    {
        return parent::windGlobalStrengthMax() / 10;
    }

    public function windFieldVariation(): float
    {
        return parent::windFieldVariation() / 2;
    }

}