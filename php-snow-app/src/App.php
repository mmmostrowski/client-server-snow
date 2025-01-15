<?php declare(strict_types=1);

namespace TechBit\Snow;

use TechBit\Snow\SnowFallAnimation\AnimationFactory;
use TechBit\Snow\App\AppArguments;
use TechBit\Snow\App\Exception\AppException;
use TechBit\Snow\App\IAnimationFactory;
use TechBit\Snow\App\IApp;


final class App implements IApp
{

    public function __construct(
        private readonly IAnimationFactory $factory = new AnimationFactory())
    {
    }

    /**
     * @throws AppException
     */
    public function run(AppArguments $arguments): void
    {
        $animation = $this->factory->create($arguments);

        $animation->initialize();

        $animation->play();
    }

}