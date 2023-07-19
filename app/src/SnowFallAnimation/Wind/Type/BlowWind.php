<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind\Type;

use TechBit\Snow\Math\PerlinNoise1D;
use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\Console\IConsole;


final class BlowWind implements IWind
{
    const TIME = 0;
    const X = 1;
    const Y = 2;
    const ANIMATION_LENGTH = 3;
    const RADIUS = 4;
    const RADIUS_SQUARE = 5;
    const CORE_RADIUS = 6;
    const STRENGTH = 7;

    private readonly SnowParticles $particles;
    
    private readonly IConsole $console;

    private int $maxNumOfWindBlows;

    private int $frequencyOfWindBlows;

    private float $minStrength;

    private float $maxStrength;

    private int $minanimationDuration;

    private int $maxanimationDuration;

    private float $time = 0.0;

    private array $blowSources = [];

    public function __construct(
        private readonly PerlinNoise1D $windWaveNoise = new PerlinNoise1D(),
        private readonly PerlinNoise1D $blowNoise = new PerlinNoise1D(),
    ) {
        
    }


    public function initialize(AnimationContext $context): void
    {
        $this->particles = $context->snowParticles();
        $this->console = $context->console();

        $this->time = 0.0;
    }

	public function onConfigChange(Config $config): void 
    {
        $this->frequencyOfWindBlows = $config->windBlowsFrequency();
        $this->maxNumOfWindBlows = $config->windBlowsMaxNumAtSameTime();
        $this->minStrength = $config->windBlowsMinStrength();
        $this->maxStrength = $config->windBlowsMaxStrength();
        $this->minanimationDuration = $config->windBlowsMinAnimationDuration();
        $this->maxanimationDuration = $config->windBlowsMaxAnimationDuration();
	}

    public function update(): void
    {
        $this->time += 1.0;

        if (count($this->blowSources) >= $this->maxNumOfWindBlows) {
            return;
        }

        if (rand(0, 20) == 0 && rand(0, 100) < $this->frequencyOfWindBlows) {
            $this->generateNewWindBlow();
        }
    }

    private function generateNewWindBlow(): void
    {
        $minAfterEdge = 8;
        $maxAfterEdge = 128;
        $aboveEdge = 20;
        $belowEdge = 10;
        $strength = rand((int)($this->minStrength * 10000), (int)($this->maxStrength * 10000)) / 10000;
        $coreRadiusRatio = (float)(rand(70, 100) / 100);
        $animationDuration = (int)(rand((int)($this->minanimationDuration * 10000), (int)($this->maxanimationDuration * 10000)) / 10000);

        $left = rand(1, 2) == 1;
        $y = rand((int)$this->console->minY() - $aboveEdge, (int)$this->console->maxY() + $belowEdge) / 1.5;
        if ($left) {
            $x = rand((int)$this->console->minX() - $minAfterEdge, (int)$this->console->minX() - $maxAfterEdge);
        } else {
            $x = rand((int)$this->console->maxX() + $minAfterEdge, (int)$this->console->maxX() + $maxAfterEdge);
        }

        $radius = $this->console->width() + $maxAfterEdge * 2.0 + 24.0;
        $this->addBlowSource($x, $y, $strength, $radius, $radius * $coreRadiusRatio, $animationDuration);
    }

    private function addBlowSource(float $x, float $y, float $strength, float $radius, float $coreRadius, int $animationDuration): void
    {
        $this->blowSources[] = [
            self::X => $x,
            self::Y => $y,
            self::STRENGTH => $strength,
            self::RADIUS => $radius,
            self::RADIUS_SQUARE => $radius * $radius,
            self::CORE_RADIUS => $coreRadius,
            self::ANIMATION_LENGTH => $animationDuration,
            self::TIME => $this->time,
        ];
    }

    public function moveParticle(int $idx): void
    {
        $x = $this->particles->x($idx);
        $y = $this->particles->y($idx);

        foreach ($this->blowSources as $blowId => $blowSource) {
            $this->blowFromSource($blowId, $idx, $x, $y, $blowSource);
        }
    }

    private function blowFromSource(int $blowId, int $particleIdx, float $x, float $y, array $blowSource): void
    {
        $bx = $blowSource[self::X];
        $by = $blowSource[self::Y];

        $deltaTime = ($this->time - $blowSource[self::TIME]) / $blowSource[self::ANIMATION_LENGTH];
        if ($deltaTime > 1) {
            $this->removeSource($blowId);
            return;
        }

        $waveFactor = sin( $deltaTime * M_PI * $this->windWaveNoise->generateInRange($this->time / 100, 50, 350) );

        $perParticleFactor = SnowParticles::perParticleFactor($particleIdx, 0.8);
        $by += $waveFactor * 200 * $perParticleFactor;

        $vx = (float)($x - $bx);
        $vy = (float)($y - $by);

        $distSquare = $vx * $vx + $vy * $vy;

        if (abs($distSquare) < 0.00001 || $blowSource[self::RADIUS_SQUARE] < $distSquare) {
            return;
        }

        $dist = sqrt($distSquare);
        $power = $blowSource[self::RADIUS] - $dist;
        $power *= $this->blowNoise->generateInRange($this->time, 0.5, 1.0);
        if ($power > $blowSource[self::CORE_RADIUS]) {
            $power = $blowSource[self::CORE_RADIUS];
        }

        $animationFactor = 0.5 + cos($deltaTime * M_PI) / 2;
        $ratio = (1.0 / $dist) * $blowSource[self::STRENGTH] * $power * $animationFactor * 0.001;
        $perParticleFactor = SnowParticles::perParticleFactor($particleIdx, 0.1);

        $this->particles->updateMomentum(
            $particleIdx,
            $vx * $ratio * $perParticleFactor,
            $vy * $ratio * $perParticleFactor,
        );
    }

    private function removeSource(int $blowId): void
    {
        unset($this->blowSources[$blowId]);
    }

}