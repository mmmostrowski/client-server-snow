<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;

interface IConfigFactory
{

    /**
     * @param string|class-string<Config> $classOrName
     */
    public function create(string $mode, array $args = []): Config;
}