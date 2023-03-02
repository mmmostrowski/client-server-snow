<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation;

use TechBit\Snow\Math\Interpolation\Interpolation;
use TechBit\Snow\Math\Interpolation\LinearInterpolation;
use TechBit\Snow\SnowFallAnimation\Config\ConfigFactory;
use TechBit\Snow\SnowFallAnimation\Config\IConfigFactory;
use TechBit\Snow\SnowFallAnimation\Config\IPresetFactory;
use TechBit\Snow\SnowFallAnimation\Config\PresetFactory;
use TechBit\Snow\SnowFallAnimation\Config\PresetSlider\ConfigPresetSliderFactory;
use TechBit\Snow\SnowFallAnimation\Config\PresetSlider\IConfigPresetSliderFactory;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
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

    /**
     * @var IAnimationObject[]
     */
    private readonly array $animationObjects;

    private readonly IWindFactory $windFactory;

    private readonly IConfigFactory $configFactory;


    public function __construct(
        string           $defaultPreset = 'slideshow:random',
        private          readonly IFramePainter $renderer = new FramePainter(),
        private          readonly StartupConfig $startupConfig = new StartupConfig(),
        private          readonly ISnowFlakeShape $flakeShapes = new SnowFlakeShape(),
        private          readonly ISceneFactory $sceneFactory = new SceneFactory(),
        private          readonly SnowBasis $snowBasis = new SnowBasis(),
        private          readonly IConsole $console = new Console(),
        private          readonly IPresetFactory $presetFactory = new PresetFactory(),
        Interpolation               $presetInterpolator = new LinearInterpolation(),
        IAnimationObject            $frameStabilizer = new FrameStabilizer(),
        ObjectsPool                 $objectsPool = new ObjectsPool(),
        IAnimationObject            $snowFall = new SnowFall(),
        IAnimationObject            $snow = new Snow(),
        ?IConfigPresetSliderFactory $configPresetSliderFactory = null,
        ?IConfigFactory             $configFactory = null,
        ?IWindFactory               $windFactory = null,
    )
    {
        $this->windFactory = $windFactory ?? new WindFactory(
                $objectsPool->allWindForces());

        $configPresetSliderFactory = $configPresetSliderFactory ?? new ConfigPresetSliderFactory(
            $this->startupConfig,
            $presetFactory,
            $presetInterpolator,
        );

        $this->configFactory = $configFactory ?? new ConfigFactory(
            $configPresetSliderFactory,
            $presetFactory,
            $objectsPool->defaultConfigPresets(),
            $defaultPreset);

        $this->animationObjects = [
            $frameStabilizer, $renderer,
            $snowBasis, $snow, $snowFall,
        ];
    }

    public function create(AppArguments $arguments): IAnimation
    {
        $config = $this->configFactory->create($arguments->presetName());

        $wind = $this->windFactory->create($config->hasWind(), $arguments->windForces());

        $scene = $this->sceneFactory->create($arguments->customScene());

        return new SnowFallAnimation(
            new AnimationContext(
                $this->console, $this->renderer,
                $wind,
                $this->flakeShapes,
                $config,
                $this->startupConfig,
                $this->snowBasis,
                new SnowParticles(),
            ),
            new AnimationObjects([
                $wind,
                $scene,
                ...$this->animationObjects,
                $config,
            ]),
            $config,
            $this->startupConfig,
        );
    }

}