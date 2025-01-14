<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind;


use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\Config;

final class WindComposition implements IWind
{

    /**
     * @param IWind[] $windForces
     */
    public function __construct(private readonly array $windForces)
    {
    }

    public function initialize(AnimationContext $context): void
    {
        foreach ($this->windForces as $wind) {
            $wind->initialize($context);
        }
    }

	public function onConfigChange(Config $config): void 
    {
        foreach ($this->windForces as $wind) {
            $wind->onConfigChange($config);
        }        
	}

    public function update(): void
    {
        foreach ($this->windForces as $wind) {
            $wind->update();
        }
    }

    public function moveParticle(int $idx): void
    {
        foreach ($this->windForces as $wind) {
            $wind->moveParticle($idx);
        }
    }

}