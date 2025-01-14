<?php declare(strict_types=1);

namespace TechBit\Snow\Math\Interpolation;



interface Interpolation
{

    public function findInt(float $t, int $from, int $to): int;

    public function findFloat(float $t, float $from, float $to): float;

}