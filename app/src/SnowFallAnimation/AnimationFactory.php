<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation;

use TechBit\Snow\Math\Interpolation\Interpolation;
use TechBit\Snow\Math\Interpolation\LinearInterpolation;
use TechBit\Snow\SnowFallAnimation\Config\ConfigFactory;
use TechBit\Snow\SnowFallAnimation\Config\IConfigFactory;
use TechBit\Snow\SnowFallAnimation\Config\IPresetFactory;
use TechBit\Snow\SnowFallAnimation\Config\IStartupConfigFactory;
use TechBit\Snow\SnowFallAnimation\Config\PresetFactory;
use TechBit\Snow\SnowFallAnimation\Config\PresetSlider\ConfigPresetSliderFactory;
use TechBit\Snow\SnowFallAnimation\Config\PresetSlider\IConfigPresetSliderFactory;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfigFactory;
use TechBit\Snow\SnowFallAnimation\Frame\FramePainter;
use TechBit\Snow\SnowFallAnimation\Frame\FrameStabilizer;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Object\AnimationObjects;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationObject;
use TechBit\Snow\SnowFallAnimation\Scene\ISceneFactory;
use TechBit\Snow\SnowFallAnimation\Scene\SceneFactory;
use TechBit\Snow\SnowFallAnimation\Snow\ISnowFlakeShape;
use TechBit\Snow\SnowFallAnimation\Snow\Snow;
use TechBit\Snow\SnowFallAnimation\Snow\SnowFall;
use TechBit\Snow\SnowFallAnimation\Snow\SnowBasis;
use TechBit\Snow\SnowFallAnimation\Snow\SnowFlakeShape;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\SnowFallAnimation\Wind\IWindFactory;
use TechBit\Snow\SnowFallAnimation\Wind\WindFactory;
use TechBit\Snow\App\AppArguments;
use TechBit\Snow\App\IAnimation;
use TechBit\Snow\App\IAnimationFactory;
use TechBit\Snow\Console\Console;
use TechBit\Snow\Console\IConsole;
use TechBit\Snow\ObjectsPool;


final class AnimationFactory implements IAnimationFactory
{

    public function __construct(
        private readonly string $defaultPreset = 'slideshow:random',
        private readonly IFramePainter $renderer = new FramePainter(),
        private readonly IStartupConfigFactory       $startupConfigFactory = new StartupConfigFactory(),
        private readonly ISnowFlakeShape             $flakeShapes = new SnowFlakeShape(),
        private readonly ISceneFactory               $sceneFactory = new SceneFactory(),
        private readonly SnowBasis                   $snowBasis = new SnowBasis(),
        private readonly IConsole                    $console = new Console(),
        private readonly IPresetFactory              $presetFactory = new PresetFactory(),
        private readonly Interpolation               $presetInterpolator = new LinearInterpolation(),
        private readonly IAnimationObject            $frameStabilizer = new FrameStabilizer(),
        private readonly ObjectsPool                 $objectsPool = new ObjectsPool(),
        private readonly IAnimationObject            $snowFall = new SnowFall(),
        private readonly IAnimationObject            $snow = new Snow(),
        private readonly ?IConfigPresetSliderFactory $configPresetSliderFactory = null,
        private readonly ?IConfigFactory             $configFactory = null,
        private readonly ?IWindFactory               $windFactory = null,
        private readonly ?StartupConfig              $startupConfig = null,
    ) {
    }

    public function create(AppArguments $arguments): IAnimation
    {
        $startupConfig = $this->startupConfig ?? $this->startupConfigFactory->create($arguments);

        $windFactory = $this->windFactory ?? new WindFactory();

        $configPresetSliderFactory = $this->configPresetSliderFactory ?? new ConfigPresetSliderFactory(
            $startupConfig,
            $this->presetFactory,
            $this->presetInterpolator,
        );

        $configFactory = $this->configFactory ?? new ConfigFactory(
            $configPresetSliderFactory,
            $this->presetFactory,
            $this->objectsPool->defaultConfigPresets(),
            $this->defaultPreset);


        $animationObjects = [
            $this->frameStabilizer, $this->renderer,
            $this->snowBasis, $this->snow, $this->snowFall,
        ];

        $config = $configFactory->create($arguments->presetName());

        $windForces = $arguments->useDefaultWindForces()
            ? $this->objectsPool->allWindForces()
            : $arguments->windForces();
        $wind = $windFactory->create($config->hasWind(), $windForces);

        $scene = $this->sceneFactory->create($config->showScene(), $arguments->customScene());

        return new SnowFallAnimation(
            new AnimationContext(
                $this->console, $this->renderer,
                $wind,
                $this->flakeShapes,
                $config,
                $startupConfig,
                $this->snowBasis,
                new SnowParticles(),
            ),
            new AnimationObjects([
                $wind,
                $scene,
                ...$animationObjects,
                $config,
            ]),
            $config,
            $startupConfig,
        );
    }

}