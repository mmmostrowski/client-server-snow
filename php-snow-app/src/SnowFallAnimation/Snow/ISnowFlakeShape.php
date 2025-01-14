<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Snow;

interface ISnowFlakeShape
{

    public function randomShape(): string;

    public function pressedSnowSymbol(): string;

    public function shapeIdx($shape): int;

}