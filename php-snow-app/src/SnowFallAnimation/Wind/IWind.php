<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind;


use TechBit\Snow\SnowFallAnimation\Object\IAnimationAliveObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationConfigurableObject;

interface IWind extends IAnimationAliveObject, IAnimationConfigurableObject
{

    public function moveParticle(int $idx): void;

}