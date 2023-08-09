<?php declare(strict_types=1);

namespace TechBit\Snow\Console;

interface IConsole
{

    public function minX(): float;

    public function minY(): float;

    public function maxX(): float;

    public function maxY(): float;

    public function width(): float;

    public function height(): float;

    public function isIn(float $x, float $y): bool;

    public function centerX(): float;

    public function centerY(): float;

    public function printAt(float $x, float $y, string $txt): void;

    public function clear(): void;

    public function switchToColor(ConsoleColor $color): void;

}