<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind\Type;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\SnowFallAnimation\Config\Config;


final class Friction implements IWind
{

    private readonly SnowParticles $particles;

    private float $friction;

    public function initialize(AnimationContext $context): void
    {
        $this->particles = $context->snowParticles();
    }

    public function update(): void
    {
    }

    public function onConfigChange(Config $config): void
    {
        $this->friction = 1.0 - $config->friction();
    }

    public function moveParticle(int $idx): void
    {
        $this->particles->multiplyMomentum($idx, $this->friction, $this->friction);
    }
}