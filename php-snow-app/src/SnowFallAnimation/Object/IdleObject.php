<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Object;

use TechBit\Snow\SnowFallAnimation\AnimationContext;

class IdleObject implements IAnimationObject, IAnimationAliveObject, IAnimationVisibleObject
{

    private static IdleObject $instance;

    private function __construct()
    {
    }

    public static function instance(): IdleObject
    {
        if (!isset(self::$instance)) {
            self::$instance = new IdleObject();
        }
        return self::$instance;
    }

    public function update(): void
    {
    }

    public function initialize(AnimationContext $context): void
    {
    }

    public function renderFirstFrame(): void
    {
    }

    public function renderLoopFrame(): void
    {
    }

}