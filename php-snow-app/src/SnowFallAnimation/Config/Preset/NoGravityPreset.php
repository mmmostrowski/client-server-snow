<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class NoGravityPreset extends DefaultConfig
{

    public function gravity(): float
    {
        return 0.0;
    }

}