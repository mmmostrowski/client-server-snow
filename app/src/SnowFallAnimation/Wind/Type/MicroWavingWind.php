<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind\Type;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;


final class MicroWavingWind implements IWind
{

    private readonly SnowParticles $particles;

    private float $microMovementConstant;

    private float $microMovementFrequency;

    private float $microMovementTime = 0.0;


    public function initialize(AnimationContext $context): void
    {
        $this->particles = $context->snowParticles();
    }

	public function onConfigChange(Config $config): void
    {
        $this->microMovementConstant = $config->microMovementPower();
        $this->microMovementFrequency = $config->microMovementFrequency();
	}

    public function update(): void
    {
        $this->microMovementTime += 0.5;
    }

    public function moveParticle(int $idx): void
    {
        if ($this->microMovementFrequency <= 0.0) {
            return;
        }

        $offset = $idx;
        $microMovement = sin($this->microMovementTime * $this->microMovementFrequency + $offset) * $this->microMovementConstant;

        $this->particles->moveByX($idx, $microMovement * SnowParticles::perParticleFactor($idx, 0.1));
    }

}