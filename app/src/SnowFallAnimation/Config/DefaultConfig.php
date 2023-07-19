<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;


class DefaultConfig implements Config
{

    public function showFps(): bool
    {
        return true;
    }

    public function hasWind(): bool
    {
        return true;
    }

    public function gravity(): float
    {
        return 0.35;
    }

    public function microMovementPower(): float
    {
        return 0.4;
    }

    public function microMovementFrequency(): float
    {
        return 0.6;
    }

    public function windGlobalVariation(): float
    {
        return 1.0;
    }

    public function windGlobalStrengthMin(): float
    {
        return 1.0;
    }

    public function windGlobalStrengthMax(): float
    {
        return 5.0;
    }

    public function windFieldVariation(): float
    {
        return 3.0;
    }

    public function windFieldStrengthMin(): int
    {
        return -10;
    }

    public function windFieldStrengthMax(): int
    {
        return 40;
    }

    public function windFieldGridUpdateEveryNthFrame(): int
    {
        return 3;
    }

    public function windFieldGridSize(): int
    {
        return 4;
    }

    public function snowProducingTempo(): int
    {
        return 100;
    }

    public function snowMaxNumOfFlakesAtOnce(): int
    {
        return 4000;
    }

    public function snowProbabilityOfProducingFromTop(): int
    {
        return 70;
    }

    public function snowHowManyFlakesNeedsToFallToFormAHill(): int
    {
        return 2;
    }

    public function snowIsPressedAfterFramesNumMin(): int
    {
        return 55;
    }

    public function snowIsPressedAfterFramesNumMax(): int
    {
        return 888;
    }

    public function windBlowsFrequency(): int
    {
        return 20;
    }

    public function windBlowsMaxNumAtSameTime(): int
    {
        return 1;
    }

    public function windBlowsMinStrength(): int
    {
        return 1;
    }

    public function windBlowsMaxStrength(): int
    {
        return 3;
    }

    public function windBlowsMinAnimationDuration(): int
    {
        return 60;
    }

    public function windBlowsMaxAnimationDuration(): int
    {
        return 200;
    }

    public function extendWorkingAreaFactor(): float
    {
        return 1.5;
    }

}