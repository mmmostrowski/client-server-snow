<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class NoSnowPreset extends DefaultConfig
{

    public function snowMaxNumOfFlakesAtOnce(): int
    {
        return 0;
    }


    public function snowProducingTempo(): int
    {
        return 0;
    }

}