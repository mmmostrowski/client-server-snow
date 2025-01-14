<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use TechBit\Snow\App\Exception\AppException;

interface IApp
{

    /**
     * @throws AppException
     */
    public function run(AppArguments $arguments): void;

}