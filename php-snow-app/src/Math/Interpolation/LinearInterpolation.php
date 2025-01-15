<?php declare(strict_types=1);

namespace TechBit\Snow\Math\Interpolation;


final class LinearInterpolation implements Interpolation
{

    public function findInt(float $t, int $from, int $to): int
    {
        return (int)( $from + $this->clampT($t) * ( $to - $from ) );
    }

    public function findFloat(float $t, float $from, float $to): float
    {
        return $from + $this->clampT($t) * ( $to - $from );
    }

    private function clampT(float $t): float
    {
        if ($t <= 0) {
            return 0;
        }
        if ($t >= 1) {
            return 1;
        }
        return $t;
    }

}