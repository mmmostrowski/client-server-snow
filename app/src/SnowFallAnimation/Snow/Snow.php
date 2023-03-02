<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Snow;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationConfigurableObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationVisibleObject;
use TechBit\Snow\Console\IConsole;


final class Snow implements IAnimationVisibleObject, IAnimationConfigurableObject
{

    private readonly SnowBasis $basis;

    private readonly SnowParticles $particles;

    private readonly IFramePainter $renderer;

    private readonly IConsole $console;

    private readonly SnowFlakeShape $shapes;

    private int $howMuchSnowIsGeneratedAtTop;

    private int $snowMaxNumOfFlakesAtOnce;

    private int $snowProducingTempo;

    private float $extendWorkingAreaFacor;

    public function initialize(AnimationContext $context): void
    {
        $this->shapes = $context->snowFlakeShape();
        $this->console = $context->console();

        $this->particles = $context->snowParticles();
        $this->renderer = $context->painter();
        $this->basis = $context->snowBasis();
    }

	public function onConfigChange(Config $config): void 
    {
        $this->howMuchSnowIsGeneratedAtTop = $config->snowProbabilityOfProducingFromTop();
        $this->snowMaxNumOfFlakesAtOnce = $config->snowMaxNumOfFlakesAtOnce();
        $this->snowProducingTempo = $config->snowProducingTempo();
        $this->extendWorkingAreaFacor = $config->extendWorkingAreaFacor();
	}

    public function renderFirstFrame(): void
    {
    }

    public function renderLoopFrame(): void
    {
        for ($i = 0; $i < $this->numOfSnowflakesToGenerate(); ++$i) {
            $newParticle = $this->generateFlakeParticle();
            if ($this->basis->isHitAt($newParticle[SnowParticles::X], $newParticle[SnowParticles::Y])) {
                continue;
            }

            $this->particles->addNew($newParticle);
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

        $extendByX = (int)($this->console->width() * $this->extendWorkingAreaFacor);
        $extendByY = (int)($this->console->height() * $this->extendWorkingAreaFacor);

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

}