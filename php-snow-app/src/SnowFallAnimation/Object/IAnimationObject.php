<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Object;


use TechBit\Snow\SnowFallAnimation\AnimationContext;

interface IAnimationObject
{

    public function initialize(AnimationContext $context): void;

}

