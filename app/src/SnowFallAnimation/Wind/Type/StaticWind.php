<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind\Type;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\Math\PerlinNoise1D;


final class StaticWind implements IWind
{

    private readonly SnowParticles $particles;

    private float $strengthMax;

    private float $strengthMin;

    private float $windVariation;

    private float $time = 0.0;

    private float $directionX = 0.0;

    private float $directionY = 0.0;


    public function __construct(
        private readonly PerlinNoise1D $windStrengthNoise = new PerlinNoise1D(),
        private readonly PerlinNoise1D $windDirectionNoise = new PerlinNoise1D())
    {
    }

    public function initialize(AnimationContext $context): void
    {
        $this->particles = $context->snowParticles();
        $this->generateNewWindDirection();
    }

	public function onConfigChange(Config $config): void 
    {
        $this->strengthMin = $config->windGlobalStrengthMin();
        $this->strengthMax = $config->windGlobalStrengthMax();
        $this->windVariation = $config->windGlobalVariation();
	}

    private function generateNewWindDirection(): void
    {
        $direction = $this->windStrengthNoise->generateInRange($this->time / 10,
            -(M_PI / 2),
            +(M_PI / 2)
        );

        $strength = $this->windDirectionNoise->generateInRange($this->time,
            $this->strengthMin * abs($direction),
            $this->strengthMax * abs($direction)
        );

        $this->directionX = -sin($direction) * $strength * 0.11;
        $this->directionY = cos($direction) * $strength * 0.033;
    }

    public function update(): void
    {
        $this->time += 0.01 * $this->windVariation;
        $this->generateNewWindDirection();
    }

    public function moveParticle(int $idx): void
    {
        $factor = SnowParticles::perParticleFactor($idx, 0.2);

        $this->particles->addMomentum($idx, $this->directionX * $factor, $this->directionY * $factor);
    }

}