<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class MassiveSnowPreset extends DefaultConfig
{

    public function snowProducingTempo(): int
    {
        return parent::snowProducingTempo() * 38;
    }

}