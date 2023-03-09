<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Frame;

use TechBit\Snow\Console\ConsoleColor;

interface IFramePainter
{

    public function startAnimation(): void;

    public function startFrame(): void;

    public function endFrame(): void;

    public function renderParticle(int $idx): void;

    public function renderBasisParticle(float $x, float $y, string $shape): void;

    public function renderBackgroundPixel(float $x, float $y, string $char, ConsoleColor $color): void;

    public function eraseParticle(int $idx): void;

    public function stopAnimation(): void;    

}