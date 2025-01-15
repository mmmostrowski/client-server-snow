<?php declare(strict_types=1);

namespace TechBit\Snow\Math;

use lib\Perlin;


final class PerlinNoise1D
{

    public function __construct(
        private readonly Perlin $perlin = new Perlin())
    {
    }

    public function generateInRange(float $x, float $from, float $to): float
    {
        return $from + ($this->generate($x) / 2 + 0.5) * ($to - $from);
    }

    public function generate(float $x): float
    {
        return (float)$this->perlin->random1D($x);
    }

}