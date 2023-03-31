<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Snow;

final class SnowParticles
{
    const SHAPE = 0;
    const X = 1;
    const Y = 2;
    const MOMENTUM_X = 3;
    const MOMENTUM_Y = 4;

    private array $particles = [];

    private int $counter = 0;

    private readonly array $nullParticle;

    public static function perParticleFactor(int $idx, float $minFactor): float 
    {
        return $minFactor + (1.0 - $minFactor) * ( ( $idx % 101 ) ) / 100.0;
    }   

    public function __construct()
    {
        $this->nullParticle = [
            self::X => -1,
            self::Y => -1,
            self::SHAPE => '',
            self::MOMENTUM_X => 0.0,
            self::MOMENTUM_Y => 0.0,
        ];
    }

    public function makeNew(float $x, float $y, string $shape): array
    {
        return [
                self::X => $x,
                self::Y => $y,
                self::SHAPE => $shape,
            ] + $this->nullParticle;
    }

    public function addNew(array $data): int
    {
        $this->particles[$this->counter] = $data;
        return $this->counter++;
    }

    public function all(): array
    {
        return $this->particles;
    }

    public function count(): int
    {
        return count($this->particles);
    }

    public function x(int $idx): float
    {
        return $this->particles[$idx][self::X];
    }

    public function y(int $idx): float
    {
        return $this->particles[$idx][self::Y];
    }

    public function shape(int $idx): string
    {
        return $this->particles[$idx][self::SHAPE];
    }
    public function shapeIdx(int $idx): string
    {
        return $this->particles[$idx][self::SHAPE];
    }

    public function moveByX(int $idx, float $dx): void
    {
        $this->particles[$idx][self::X] += $dx;
    }

    public function moveByY(int $idx, float $dy): void
    {
        $this->particles[$idx][self::Y] += $dy;
    }

    public function remove(int $idx): void
    {
        unset($this->particles[$idx]);
    }

    public function updateMomentum(int $idx, float $dx, float $dy): void
    {
        $this->particles[$idx][self::MOMENTUM_X] += $dx;
        $this->particles[$idx][self::MOMENTUM_Y] += $dy;
    }

    public function updateMomentumArr(int $idx, array $data): void
    {
        $this->particles[$idx][self::MOMENTUM_X] += $data[0];
        $this->particles[$idx][self::MOMENTUM_Y] += $data[1];
    }

    public function moveByMomentum(int $idx): void
    {
        $this->particles[$idx][self::X] += $this->particles[$idx][self::MOMENTUM_X];
        $this->particles[$idx][self::Y] += $this->particles[$idx][self::MOMENTUM_Y];
    }

}