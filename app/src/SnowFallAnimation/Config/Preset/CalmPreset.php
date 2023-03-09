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

    public function windBlowsMaxanimationDuration(): int
    {
        return (int)(parent::windBlowsMaxanimationDuration() / 6);
    }

    public function windBlowsMinanimationDuration(): int
    {
        return (int)(parent::windBlowsMinanimationDuration() / 6);
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