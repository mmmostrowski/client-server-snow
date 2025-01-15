<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class NoWindPreset extends DefaultConfig
{

    public function hasWind(): bool
    {
        return false;
    }

    public function snowProducingTempo(): int 
    {
        return parent::snowProducingTempo() * 3;
    }

}