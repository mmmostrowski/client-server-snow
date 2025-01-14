<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use TechBit\Snow\App\Exception\AppException;

interface IAnimation
{

    /**
     * @throws AppException
     */
    public function initialize(): void;

    public function play(): void;

}