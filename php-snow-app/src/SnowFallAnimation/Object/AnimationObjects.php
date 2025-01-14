<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Object;

final class AnimationObjects
{

    /**
     * @var IAnimationObject[]
     */
    private readonly array $objects;

    /**
     * @var IAnimationVisibleObject[]
     */
    private readonly array $visibleObjects;

    /**
     * @var IAnimationAliveObject[]
     */
    private readonly array $aliveObjects;

    /**
     * @return IAnimationConfigurableObject[]
     */
    private readonly array $configurableObjects;


    /**
     * @param IAnimationObject[] $objects
     */
    public function __construct(array $objects)
    {
        $aliveObjects = [];
        $visibleObjects = [];
        $configurableObjects = [];

        foreach ($objects as $i => $object) {
            $class = get_class($object);

            if (!$object instanceof IAnimationObject) {
                unset($objects[$i]);
            }

            if ($object instanceof IAnimationAliveObject) {
                $aliveObjects[$class] = $object;
            }
            if ($object instanceof IAnimationVisibleObject) {
                $visibleObjects[$class] = $object;
            }
            if ($object instanceof IAnimationConfigurableObject) {
                $configurableObjects[$class] = $object;
            }
        }

        $this->objects = array_values($objects);
        $this->aliveObjects = $aliveObjects;
        $this->visibleObjects = $visibleObjects;
        $this->configurableObjects = $configurableObjects;
    }


    /**
     * @return IAnimationObject[]
     */
    public function allObjects(): array
    {
        return $this->objects;
    }

    /**
     * @return IAnimationVisibleObject[]
     */
    public function allVisibleObjects(): array
    {
        return $this->visibleObjects;
    }

    /**
     * @return IAnimationAliveObject[]
     */
    public function allAliveObjects(): array
    {
        return $this->aliveObjects;
    }

    /**
     * @return IAnimationConfigurableObject[]
     */
    public function allConfigurableObjects(): array
    {
        return $this->configurableObjects;
    }

}