<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;

use TechBit\Snow\App\NamedClass;


final class PresetFactory implements IPresetFactory
{

    public function __construct(
        private readonly NamedClass $namedClass = new NamedClass("SnowFallAnimation\\Config\\Preset\\", "Preset")
    ) {
    }

    /**
     * @param class-string<Config> $classOrName
     */
    public function create(string $classOrName, array $args = []): Config
    {
        $classname = $this->className($classOrName);
        return new $classname(...$args);
    }

    public function className(string $presetName): string
    {
        return $this->namedClass->toClassName($presetName);
    }

}