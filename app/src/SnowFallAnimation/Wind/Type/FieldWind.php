<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind\Type;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\Console\IConsole;
use TechBit\Snow\Math\PerlinNoise3D;


final class FieldWind implements IWind
{

    private readonly SnowParticles $particles;

    private readonly IConsole $console;

    private int $gridSize = -1;

    private float $strengthMin;

    private float $strengthMax;

    private int $updateGridEveryNthFrame;

    private float $fieldWindVariation;

    private int $updateGridFrameCounter = 0;

    private float $time = 0.0;

    private array $grid = [];


    public function __construct(
        private readonly PerlinNoise3D $vectorDirectionNoise = new PerlinNoise3D(),
        private readonly PerlinNoise3D $vectorPowerNoise = new PerlinNoise3D())
    {
    }

    public function initialize(AnimationContext $context): void
    {
        $this->time = (float)time();

        $this->particles = $context->snowParticles();
        $this->console = $context->console();

        $this->vectorDirectionNoise->initialize(20);
        $this->vectorPowerNoise->initialize(3);

        $this->updateGridFrameCounter = $this->updateGridEveryNthFrame;        
    }

	public function onConfigChange(Config $config): void 
    {
        $this->updateGridEveryNthFrame = $config->windFieldGridUpdateEveryNthFrame();
        $this->fieldWindVariation = $config->windFieldVariation();
        $this->strengthMin = $config->windFieldStrengthMin() * 0.002;
        $this->strengthMax = $config->windFieldStrengthMax() * 0.002;

        $previousGridSize = $this->gridSize;
        $this->gridSize = $config->windFieldGridSize();
        if ($this->gridSize != $previousGridSize) {
            $this->updateGrid();
        }
	}

    public function update(): void
    {
        $this->time += 0.01 * $this->fieldWindVariation;
        if (--$this->updateGridFrameCounter <= 0) {
            $this->updateGridFrameCounter = $this->updateGridEveryNthFrame;
            $this->updateGrid();
        }
    }

    private function updateGrid(): void
    {
        $squeezeFactor = 0.3;
        $howMuchUpRad = M_PI * 0.7;
        $strengthRange = $this->strengthMax - $this->strengthMin;

        for ($gy = 0; $gy < $this->gridSize; ++$gy) {
            for ($gx = 0; $gx < $this->gridSize; ++$gx) {
                $direction = $this->vectorDirectionNoise->generate($gx, $gy, $this->time);
                $power = $this->vectorPowerNoise->generate($gx, $gy, $this->time);

                $directionRad = -$howMuchUpRad + (0.5 + ($direction / 2)) * (M_PI + 2 * $howMuchUpRad);
                $powerScaled = exp($this->strengthMin + (0.5 + ($power / 2)) * ($strengthRange)) - 1;

                $this->grid[$gx][$gy] = [
                    -cos($directionRad) * $powerScaled,
                    sin($directionRad) * $powerScaled * $squeezeFactor,
                ];
            }
        }
    }

    public function moveParticle(int $idx): void
    {
        $particleX = (int)(($this->gridSize) * ($this->particles->x($idx) - $this->console->minX()) / $this->console->width());
        if ($particleX >= $this->gridSize || $particleX < 0) {
            return;
        }

        $particleY = (int)(($this->gridSize) * ($this->particles->y($idx) - $this->console->minY()) / $this->console->height());
        if ($particleY >= $this->gridSize || $particleY < 0) {
            return;
        }

        $forceVector = $this->grid[$particleX][$particleY];
        $perParticleFactor = SnowParticles::perParticleFactor($idx, 0.2);
        $this->particles->addMomentum($idx,
            $forceVector[0] * $perParticleFactor,
            $forceVector[1] * $perParticleFactor,
        );
    }
}

