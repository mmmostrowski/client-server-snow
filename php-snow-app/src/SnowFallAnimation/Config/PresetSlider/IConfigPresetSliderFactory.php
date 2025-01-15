<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\PresetSlider;


interface IConfigPresetSliderFactory
{

    public function create(array $presets): ConfigPresetSlider;

}
