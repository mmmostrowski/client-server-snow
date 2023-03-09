<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;
use TechBit\Snow\App\AppArguments;


interface IStartupConfigFactory
{
    
    public function create(AppArguments $args): StartupConfig;

}