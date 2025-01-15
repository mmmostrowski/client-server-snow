<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;

interface IPresetFactory
{

    /**
     * @param string|class-string<Config> $classOrName
     */
    public function create(string $classOrName, array $args = []): Config;

    public function className(string $presetName): string;

}