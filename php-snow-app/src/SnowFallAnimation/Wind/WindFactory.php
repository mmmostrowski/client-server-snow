<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind;

use TechBit\Snow\SnowFallAnimation\Wind\Type\NoWind;
use TechBit\Snow\App\NamedClass;

final class WindFactory implements IWindFactory
{

    public function __construct(
        private readonly NamedClass $namedClass = new NamedClass("SnowFallAnimation\\Wind\\Type\\", ""),
    )
    {
    }

    /**
     * @param string[]|class-string<IWind>[] $windForces
     */
    public function create(bool $windEnabled, array $windForces): IWind
    {
        if (!$windEnabled || empty($windForces)) {
            return new NoWind();
        }

        $windObjects = [];
        foreach ($windForces as $classOrName) {
            $windObjects[] = $this->createWindObject($classOrName);
        }

        if (count($windForces) > 1) {
            return new WindComposition($windObjects);
        }
        return reset($windObjects);
    }

    /**
     * @param string|class-string<IWind> $classOrName
     */
    private function createWindObject(mixed $classOrName): IWind
    {
        $classname = $this->namedClass->toClassName($classOrName);
        return new $classname();
    }


}