<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Snow;

use TechBit\Snow\Console\ConsoleColor;
use TechBit\Snow\Console\IConsole;
use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationAliveObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationVisibleObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationConfigurableObject;
use TechBit\Snow\SnowFallAnimation\Config\Config;


final class SnowBasis implements IAnimationAliveObject, IAnimationVisibleObject, IAnimationConfigurableObject
{
    const SHAPE = 0;
    const COUNTER = 1;

    private readonly IConsole $console;

    private readonly IFramePainter $renderer;

    private int $pressingSpeedMin;

    private int $pressingSpeedMax;

    private int $howManyParticlesNeedsToFallToBecomeGround;

    private readonly string $pressedSnowSymbol;

    private array $staticParticles = [];

    private array $particlesToBePressed = [];

    private int $frameCounter = 0;


    public function initialize(AnimationContext $context): void
    {
        $this->console = $context->console();
        $this->renderer = $context->painter();

        $this->pressedSnowSymbol = $context->snowFlakeShape()->pressedSnowSymbol();
        $this->frameCounter = 0;
    }

	public function onConfigChange(Config $config): void {
        $this->howManyParticlesNeedsToFallToBecomeGround = $config->snowHowManyFlakesNeedsToFallToFormAHill();
        $this->pressingSpeedMin = $config->snowIsPressedAfterFramesNumMin();
        $this->pressingSpeedMax = $config->snowIsPressedAfterFramesNumMax();
	}

    public function update(): void
    {
        $this->processSnowFlakesPressing();
    }

    private function processSnowFlakesPressing(): void
    {
        foreach ($this->particlesToBePressed as $x => &$list) {
            foreach ($list as $y => &$counter) {
                if (--$counter >= 0) {
                    continue;
                }

                $this->mergePoint($x, $y, $this->pressedSnowSymbol);
                unset($this->particlesToBePressed[$x][$y]);
            }
        }
    }

    private function mergePoint(float $x, float $y, string $shape): void
    {
        $x = (int)$x;
        $y = (int)$y;

        if ($this->staticParticles[$x][$y][self::SHAPE] != $this->pressedSnowSymbol) {
            $this->staticParticles[$x][$y][self::SHAPE] = $shape;
        }

        if (!isset($this->staticParticles[$x][$y][self::COUNTER])) {
            $this->staticParticles[$x][$y][self::COUNTER] = 0;
        }
    }

    public function renderFirstFrame(): void
    {
    }

    public function renderLoopFrame(): void
    {
        if (++$this->frameCounter % 3 != 0) {
            return;
        }

        foreach ($this->staticParticles as $x => $list) {
            foreach ($list as $y => $particle) {
                if ($particle[self::SHAPE] == '') {
                    continue;
                }
                $this->renderer->renderBasisParticle($x, $y, $particle[self::SHAPE]);
            }
        }
    }

    public function drawGround(): void
    {
        $this->drawHLine($this->console->minX(), $this->console->maxX(), $this->console->maxY());
    }

    private function drawHLine(float $minX, float $maxX, float $y): void
    {
        for ($x = $minX; $x <= $maxX; ++$x) {
            $this->addPoint($x, $y, '');
        }
    }

    private function addPoint(float $x, float $y, string $shape): void
    {
        $x = (int)$x;
        $y = (int)$y;

        $this->staticParticles[$x][$y] = [
            self::SHAPE => $shape,
            self::COUNTER => 0,
        ];
    }

    public function mergeParticle(float $x, float $y, string $shape): void
    {
        $x = (int)$x;
        $y = (int)$y;

        if (++$this->staticParticles[$x][$y][self::COUNTER] >= $this->howManyParticlesNeedsToFallToBecomeGround) {
            if (($this->isHitAt($x - 1, $y) || ($x == $this->console->minX() + 1))
                && ($this->isHitAt($x + 1, $y) || ($x == $this->console->maxX() - 1))
            ) {
                $this->addPoint($x, $y - 1, $shape);
            }
        } else {
            $this->mergePoint($x, $y, $shape);
            $this->addPointForPressing($x, $y);
        }
    }

    public function isHitAt(float $x, float $y): bool
    {
        $x = (int)$x;
        $y = (int)$y;

        return isset($this->staticParticles[$x][$y]);
    }

    private function addPointForPressing(float $x, float $y): void
    {
        $x = (int)$x;
        $y = (int)$y;

        $this->particlesToBePressed[$x][$y] = rand($this->pressingSpeedMin, $this->pressingSpeedMax);
    }

    public function drawCharsInCenter(string $chars, float $offsetX, float $offsetY, ConsoleColor $color): void
    {
        $this->drawChars(
            $chars,
            $this->console->centerX() + $offsetX,
            $this->console->centerY() + $offsetY,
            $color
        );
    }

    public function drawChars(string $chars, float $posX, float $posY, ConsoleColor $color): void
    {
        $lines = explode(PHP_EOL, $chars);

        $height = count($lines);
        $width = 0;
        foreach ($lines as $line) {
            $width = max($width, strlen($line));
        }

        for ($y = 0; $y < $height; ++$y) {
            $pY = $y + $posY - $height / 2;
            for ($x = 0; $x < $width; ++$x) {
                $pX = $x + $posX - $width / 2;
                $c = @$lines[$y][$x];

                if ($c === ' ' || $c === null || $c === '') {
                    continue;
                }

                if (!$this->console->isIn($pX, $pY)) {
                    continue;
                }

                $this->addPoint($pX, $pY, '');
                $this->renderer->renderBackgroundPixel($pX, $pY, $c, $color);
            }
            // echo PHP_EOL;
        }
    }

}