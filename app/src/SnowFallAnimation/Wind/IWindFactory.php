<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Wind;

interface IWindFactory
{


    /**
     * @param string[]|class-string<IWind>[] $windForces
     */
    public function create(bool $windEnabled, array $windForces): IWind;


}