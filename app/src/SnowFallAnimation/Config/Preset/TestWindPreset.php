<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\Preset;

use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;


final class TestWindPreset extends DefaultConfig
{

    public function showScene(): bool
    {
        return false;
    }


    public function windBlowsMaxStrength(): int
    {
        return parent::windBlowsMaxStrength() * 3;
    }

    public function windBlowsMinStrength(): int
    {
        return parent::windBlowsMinStrength() * 3;
    }

//    public function fps()
//    {
//        return 100000;
//    }

    // public function snowProbabilityOfProducingFromTop(): int
    // {
    //     return 0;
    // }

//    public function gravity(): float
//    {
//        return 0;
//    }

    // public function windGlobalStrengthMin(): float
    // {
    //     return 0.0;
    // }

    // public function windGlobalStrengthMax(): float
    // {
    //     return 0.0;

    // }
//
//    public function windFieldVariation()
//    {
//        return 4.0;
//    }
//

//    public function windFieldPowerMin()
//    {
//        return 0;
//    }
//
//    public function windFieldPowerMax()
//    {
//        return 0;
//    }

    // public function windBlowsFrequency(): int
    // {
    //     return 100;
    // }

    public function snowProducingTempo(): int
    {
        return parent::snowProducingTempo() * 26;
    }

    public function snowMaxNumOfFlakesAtOnce(): int
    {
        return parent::snowMaxNumOfFlakesAtOnce() * 20;
    }

    public function windBlowsMinAnimationDuration(): int
    {
        return parent::windBlowsMinAnimationDuration() * 10;
    }

    public function windBlowsMaxAnimationDuration(): int
    {
        return parent::windBlowsMaxAnimationDuration() * 10;
    }



//    public function microMovementFrequency()
//    {
//        return 0;
//    }
//
    public function microMovementPower(): float
    {
        return 0.0;
    }

    public function showFps(): bool
    {
        return true;
    }
    
}