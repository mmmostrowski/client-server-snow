<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Snow;

use TechBit\Snow\Math\PerlinNoise3D;
use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationAliveObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationConfigurableObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationVisibleObject;
use TechBit\Snow\Console\IConsole;


final class Snow implements IAnimationVisibleObject, IAnimationConfigurableObject, IAnimationAliveObject
{
    const INV_E_MINUS_1 = 1.0 / (M_E - 1.0);

    private readonly SnowBasis $basis;

    private readonly SnowParticles $particles;

    private readonly IConsole $console;

    private readonly SnowFlakeShape $shapes;

    private int $snowMaxNumOfFlakesAtOnce;

    private int $snowProducingTempo;

    private float $extendWorkingAreaFactor;
    private int $updateGridEveryNthFrame;
    private int $updateGridFrameCounter;
    private float $snowProducingVariation;
    private float $gridSize = -1;
    private float $time = 0.0;
    private array $grid = [];


    public function __construct(
        private readonly PerlinNoise3D $vectorProbabilityNoise = new PerlinNoise3D())
    {
    }

    public function onConfigChange(Config $config): void
    {
        $this->snowMaxNumOfFlakesAtOnce = $config->snowMaxNumOfFlakesAtOnce();
        $this->snowProducingTempo = $config->snowProducingTempo();
        $this->extendWorkingAreaFactor = $config->extendWorkingAreaFactor();
        $this->updateGridEveryNthFrame = $config->snowProducingGridUpdateEveryNthFrame();
        $this->snowProducingVariation = $config->snowProducingVariation();

        $previousGridSize = $this->gridSize;
        $this->gridSize = $config->snowProducingGridSize();
        if ($this->gridSize != $previousGridSize) {
            $this->updateGrid();
        }
    }

    public function initialize(AnimationContext $context): void
    {
        $this->time = (float)time();

        $this->shapes = $context->snowFlakeShape();
        $this->console = $context->console();

        $this->particles = $context->snowParticles();
        $this->basis = $context->snowBasis();

        $this->vectorProbabilityNoise->initialize(777);

        $this->updateGridFrameCounter = $this->updateGridEveryNthFrame;
    }

    public function update(): void
    {
        $this->time += 0.01 * $this->snowProducingVariation;
        if (--$this->updateGridFrameCounter <= 0) {
            $this->updateGridFrameCounter = $this->updateGridEveryNthFrame;
            $this->updateGrid();
        }
    }

    public function renderFirstFrame(): void
    {
    }

    public function renderLoopFrame(): void
    {
        $numOfTies = 2;
        for ($i = 0; $i < $this->numOfSnowflakesToGenerate(); ++$i) {
            for ($numOfTry = 0; $numOfTry < $numOfTies; ++$numOfTry) {
                $newParticle = $this->generateFlakeParticle();
                if ($this->basis->isHitAt($newParticle[SnowParticles::X], $newParticle[SnowParticles::Y])) {
                    continue;
                }
                if (!$this->isActiveArea($newParticle[SnowParticles::X], $newParticle[SnowParticles::Y])) {
                    continue;
                }
                $this->particles->addNew($newParticle);
                break;
            }
        }
    }

    private function numOfSnowflakesToGenerate(): int
    {
        if ($this->particles->count() >= $this->snowMaxNumOfFlakesAtOnce) {
            return 0;
        }

        if ($this->snowProducingTempo < 100) {
            return rand(0, 100) < $this->snowProducingTempo ? 1 : 0;
        }

        return min((int)($this->snowProducingTempo / 100), $this->snowMaxNumOfFlakesAtOnce);
    }

    private function generateFlakeParticle(): array
    {
        $shape = $this->shapes->randomShape();

        $extendByX = (int)($this->console->width() * $this->extendWorkingAreaFactor);
        $extendByY = (int)($this->console->height() * $this->extendWorkingAreaFactor);

        $consoleYMin = (int)$this->console->minY() - $extendByY;
        $consoleYMax = (int)$this->console->maxY() + $extendByY;

        $newPlaceX = rand((int)$this->console->minX() - $extendByX, (int)$this->console->maxX() + $extendByX);
        $newPlaceY = $consoleYMax - $consoleYMin - log(log(log(rand(17, 5000000)))) * ( $consoleYMax - $consoleYMin );

        return $this->particles->makeNew(
            $newPlaceX,
            $newPlaceY,
            $shape,
        );
    }

    private function updateGrid(): void
    {
        for ($gy = 0; $gy < $this->gridSize; ++$gy) {
            for ($gx = 0; $gx < $this->gridSize; ++$gx) {
                $noise = $this->vectorProbabilityNoise->generate($gx, $gy, $this->time);
                $this->grid[$gx][$gy] = $noise / 2.0 + 0.5;
            }
        }
        $this->normalizeGrid();
    }

    private function normalizeGrid(): void
    {
        $minValue = 1000.0;
        $maxValue = -1000.0;
        for ($gy = 0; $gy < $this->gridSize; ++$gy) {
            for ($gx = 0; $gx < $this->gridSize; ++$gx) {
                if ($minValue > $this->grid[$gx][$gy]) {
                    $minValue = $this->grid[$gx][$gy];
                }
                if ($maxValue < $this->grid[$gx][$gy]) {
                    $maxValue = $this->grid[$gx][$gy];
                }
            }
        }

        $range = $maxValue - $minValue;
        for ($gy = 0; $gy < $this->gridSize; ++$gy) {
            for ($gx = 0; $gx < $this->gridSize; ++$gx) {
                $this->grid[$gx][$gy] = ($this->grid[$gx][$gy] - $minValue) / $range;
            }
        }
    }

    private function isActiveArea(float $x, float $y): bool
    {
        $particleX = (int)(($this->gridSize) * ($x - $this->console->minX()) / ($this->console->width() * $this->extendWorkingAreaFactor));
        if ($particleX >= $this->gridSize || $particleX < 0) {
            return false;
        }

        $particleY = (int)(($this->gridSize) * ($y - $this->console->minY()) / ($this->console->height() * $this->extendWorkingAreaFactor));
        if ($particleY >= $this->gridSize || $particleY < 0) {
            return false;
        }

        $probability = $this->grid[$particleX][$particleY];
        return $probability > (exp(rand(0, 1000) / 1000.0) - 1.0) * self::INV_E_MINUS_1;
    }
}