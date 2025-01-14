<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Object;


interface IAnimationVisibleObject extends IAnimationObject
{

    public function renderFirstFrame(): void;

    public function renderLoopFrame(): void;

}