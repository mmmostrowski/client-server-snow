<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Object;


interface IAnimationAliveObject extends IAnimationObject
{

    public function update(): void;

}