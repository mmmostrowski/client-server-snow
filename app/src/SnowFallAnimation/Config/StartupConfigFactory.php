<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;
use TechBit\Snow\App\AppArguments;


final class StartupConfigFactory implements IStartupConfigFactory
{
    
    public function create(AppArguments $args): StartupConfig
    {
        return new StartupConfig($args->targetFps(), $args->animationDurationSec());
    }

}