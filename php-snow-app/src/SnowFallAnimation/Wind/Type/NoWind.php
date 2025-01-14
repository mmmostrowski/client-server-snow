<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind\Type;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\SnowFallAnimation\Config\Config;

final class NoWind implements IWind
{

    public function initialize(AnimationContext $context): void
    {
    }

    public function update(): void
    {
    }

    public function moveParticle(int $idx): void
    {
    }

	public function onConfigChange(Config $config): void 
    { 
	}

}