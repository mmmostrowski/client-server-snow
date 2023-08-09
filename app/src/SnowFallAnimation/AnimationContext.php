<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation;

use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Snow\ISnowFlakeShape;
use TechBit\Snow\SnowFallAnimation\Snow\SnowBasis;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\Console\IConsole;

final class AnimationContext
{

    public function __construct(
        private readonly IConsole $console,
        private readonly IFramePainter $painter,
        private readonly IWind $wind,
        private readonly ISnowFlakeShape $snowFlakeShape,
        private readonly StartupConfig $startupConfig,
        private readonly SnowBasis $snowBasis,
        private readonly SnowParticles $snowParticles,
    )
    {
    }
    
    public function config(): StartupConfig
    {
        return $this->startupConfig;
    }

    public function console(): IConsole
    {
        return $this->console;
    }

    public function painter(): IFramePainter
    {
        return $this->painter;
    }

    public function snowParticles(): SnowParticles
    {
        return $this->snowParticles;
    }

    public function snowFlakeShape(): ISnowFlakeShape
    {
        return $this->snowFlakeShape;
    }

    public function snowBasis(): SnowBasis
    {
        return $this->snowBasis;
    }

    public function wind(): IWind
    {
        return $this->wind;
    }


}