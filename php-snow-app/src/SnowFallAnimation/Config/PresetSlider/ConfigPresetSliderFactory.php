<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\PresetSlider;
use TechBit\Snow\Math\Interpolation\Interpolation;
use TechBit\Snow\SnowFallAnimation\Config\DefaultConfig;
use TechBit\Snow\SnowFallAnimation\Config\IPresetFactory;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;


final class ConfigPresetSliderFactory implements IConfigPresetSliderFactory
{

	public function __construct(
		private readonly StartupConfig $config,
        private readonly IPresetFactory $presetFactory,
		private readonly Interpolation $interpolation,
	) {
	}
	
	public function create(array $presets): ConfigPresetSlider {
		if (empty($presets)) {
			$presets = [ new DefaultConfig() ];
		}

        return new ConfigPresetSlider($presets, 
            $this->config,
            $this->presetFactory,
            $this->interpolation,
        );
	}
}
