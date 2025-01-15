<?php declare(strict_types=1);

namespace TechBit\Snow\Math;

use lib\Perlin;


final class PerlinNoise2D
{

    public function __construct(
        private readonly Perlin $perlin = new Perlin())
    {
    }

    public function generate(float $x, float $y): float
    {
        return $this->perlin->random2D($x, $y);
    }

}