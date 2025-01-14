<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Scene;

use TechBit\Snow\SnowFallAnimation\Object\IAnimationObject;
use TechBit\Snow\SnowFallAnimation\Object\IdleObject;

final class SceneFactory implements ISceneFactory
{

    public function create(bool $showScene, ?string $customSceneTxt): IAnimationObject
    {
        if (!$showScene) {
            return IdleObject::instance();
        }
        return $customSceneTxt === null
            ? new Scene()
            : new CustomScene($customSceneTxt);
    }

}