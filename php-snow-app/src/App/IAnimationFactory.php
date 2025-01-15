<?php declare(strict_types=1);

namespace TechBit\Snow\App;


interface IAnimationFactory
{

    public function create(AppArguments $arguments): IAnimation;

}