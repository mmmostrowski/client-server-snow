<?php declare(strict_types=1);

namespace TechBit\Snow\Math;

use Perlin;


class PerlinNoise3D
{

    public function __construct(
        protected readonly Perlin $perlin)
    {
    }

    public function initialize(float $size): void
    {
        $this->perlin->_default_size = $size;
    }

    public function generate(float $x, float $y, float $z): float
    {
        return $this->perlin->noise($x, $y, $z);
    }

}