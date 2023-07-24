<?php declare(strict_types=1);

namespace TechBit\Snow;

use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Wind\IWind;
use TechBit\Snow\SnowFallAnimation\Wind\Type\BlowWind;
use TechBit\Snow\SnowFallAnimation\Wind\Type\FieldWind;
use TechBit\Snow\SnowFallAnimation\Wind\Type\Friction;
use TechBit\Snow\SnowFallAnimation\Wind\Type\MicroWavingWind;
use TechBit\Snow\SnowFallAnimation\Wind\Type\NoWind;
use TechBit\Snow\SnowFallAnimation\Wind\Type\StaticWind;
use TechBit\Snow\SnowFallAnimation\Config\Preset\CalmPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\ClassicalPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\MassiveSnowPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\NoGravityPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\NoSnowPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\NoWindPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\SnowyPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\TestPerformancePreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\TestWindPreset;
use TechBit\Snow\SnowFallAnimation\Config\Preset\WindyPreset;

final class ObjectsPool
{
    public function __construct(
        private readonly array $windForces = [
            MicroWavingWind::class,
            StaticWind::class,
            FieldWind::class,
            BlowWind::class,
            NoWind::class,
            Friction::class,
        ],
        private readonly array $configPresets = [
            ClassicalPreset::class,
            WindyPreset::class,
            SnowyPreset::class,
            MassiveSnowPreset::class,
            CalmPreset::class,
            NoSnowPreset::class,
            NoWindPreset::class,
            NoGravityPreset::class,
            TestPerformancePreset::class,
            TestWindPreset::class,
        ],
        private readonly array $defaultConfigPresets = [
            ClassicalPreset::class => 40,
            CalmPreset::class => 40,
            WindyPreset::class => 30,
            SnowyPreset::class => 30,
            MassiveSnowPreset::class => 20,
        ]
    )
    {
    }

    /**
     * @return class-string<IWind>[]
     */
    public function allWindForces(): array
    {
        return $this->windForces;
    }

    /**
     * @return class-string<Config>[]
     */
    public function allConfigPresets(): array
    {
        return $this->configPresets;
    }

    /**
     * @return array<class-string<Config>,int>
     */
    public function defaultConfigPresets(): array
    {
        return $this->defaultConfigPresets;
    }

}
