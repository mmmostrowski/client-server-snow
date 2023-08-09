<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config;

use InvalidArgumentException;
use TechBit\Snow\Math\WeightedRandom;
use TechBit\Snow\SnowFallAnimation\Config\PresetSlider\IConfigPresetSliderFactory;


final class ConfigFactory implements IConfigFactory
{

    private readonly WeightedRandom $randomPreset;

    /**
     * @param array<class-string<Config>, int> $presetsToRandom
     */
    public function __construct(
        private readonly IConfigPresetSliderFactory $configPresetSliderFactory,
        private readonly IPresetFactory $presetFactory,
        private readonly array $presetsToRandom,
        private readonly string $defaultPreset,
    )
    {
        $this->randomPreset = new WeightedRandom($this->presetsToRandom);
    }

    /**
     * @param class-string<Config> $mode
     */
    public function create(string $mode, array $args = []): Config
    {
        $mode = str_replace(' ', '', $mode);

        if ($mode == '') {
            $mode = $this->defaultPreset;
        }

        if ($mode == 'random') {
            return $this->create($this->randomPreset->next());
        }

        if ($mode == 'slideshow:random') {
            return $this->configPresetSliderFactory->create($this->presetsToRandom);
        }

        if (str_starts_with($mode, 'slideshow:')) {
            $slideshowItems = substr($mode, strlen('slideshow:'));
            return $this->createSlideshowFor($slideshowItems);
        }

        return $this->presetFactory->create($mode);
    }

    private function createSlideshowFor(string $presetNames): Config
    {
        $presetNames = array_filter(explode(',', $presetNames));

        $presetClasses = array_map(fn($name) => $this->presetFactory->className($name), $presetNames);

        $unknownPresets = array_filter($presetClasses, fn($class) => !class_exists($class));
        if ($unknownPresets) {
            throw new InvalidArgumentException("Unknown presets:\n" . implode("\n - ", [null, ...$unknownPresets] ) . "\n\n");
        }

        return $this->configPresetSliderFactory->create(array_map(fn() => 10, array_flip($presetClasses)));
    }

}