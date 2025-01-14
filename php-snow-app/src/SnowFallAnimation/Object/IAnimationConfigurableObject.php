<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Object;

use TechBit\Snow\SnowFallAnimation\Config\Config;


interface IAnimationConfigurableObject 
{

    public function onConfigChange(Config $config): void;

}