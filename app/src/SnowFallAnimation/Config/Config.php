<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;


interface Config
{

    public function hasWind(): bool;

    public function showFps(): bool;

    public function gravity(): float;

    public function microMovementPower(): float;

    public function microMovementFrequency(): float;

    public function windGlobalVariation(): float;

    public function windGlobalStrengthMin(): float;

    public function windGlobalStrengthMax(): float;

    public function windFieldVariation(): float;

    public function windFieldStrengthMin(): int;

    public function windFieldStrengthMax(): int;

    public function windFieldGridUpdateEveryNthFrame(): int;

    public function windFieldGridSize(): int;

    public function snowProducingTempo(): int;

    public function snowMaxNumOfFlakesAtOnce(): int;

    public function snowProbabilityOfProducingFromTop(): int;

    public function snowHowManyFlakesNeedsToFallToFormAHill(): int;

    public function snowIsPressedAfterFramesNumMin(): int;

    public function snowIsPressedAfterFramesNumMax(): int;

    public function windBlowsFrequency(): int;

    public function windBlowsMaxNumAtSameTime(): int;

    public function windBlowsMinStrength(): int;

    public function windBlowsMaxStrength(): int;

    public function windBlowsMinAnimationDuration(): int;

    public function windBlowsMaxAnimationDuration(): int;

    public function extendWorkingAreaFactor(): float;

    public function showScene(): bool;

    public function friction(): float;

}