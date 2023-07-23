<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Scene;

use TechBit\Snow\SnowFallAnimation\Object\IAnimationObject;

interface ISceneFactory
{

    public function create(bool $showScene, ?string $customSceneTxt): IAnimationObject;

}