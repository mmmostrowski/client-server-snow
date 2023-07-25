<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Config\PresetSlider;

use TechBit\Snow\Math\Interpolation\Interpolation;
use TechBit\Snow\Math\WeightedRandom;
use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Config\IPresetFactory;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationAliveObject;


final class ConfigPresetSlider implements Config, IAnimationAliveObject
{

	private Config $currentPreset;

	private ?Config $targetPreset;

	private float $progress = 0;

	private float $progressDelta = 0;

	private int $currentPresetCounter = 0;


	public function __construct(
		private readonly array $presets,
		private readonly StartupConfig $config,
        private readonly IPresetFactory $presetFactory,
		private readonly Interpolation $interpolation,
	) {
		if (empty($this->presets)) {
			throw new \InvalidArgumentException("Slider must have at least one preset provided!");
		}
		$this->targetPreset = $this->randomNextPreset(false);
		$this->startNextPreset();
	}

	public function initialize(AnimationContext $context): void {
	}

    public function update(): void
	{
		if (count($this->presets) == 1) {
			$this->currentPreset = $this->presetFactory->create(array_keys($this->presets)[0]);
			$this->targetPreset = null;
			return;
		}

		if (--$this->currentPresetCounter < 0) {
			$this->progress += $this->progressDelta;
			if ( $this->progress >= 1) {
				$this->startNextPreset();
			}
		}
	}

    private function startNextPreset(): void
	{
		$this->currentPreset = $this->targetPreset;

		if (count($this->presets) == 1) {
			return;
		}

		$this->targetPreset = $this->randomNextPreset(true);
		$this->progress = 0;

		$durationMin = $this->config->sliderMinDurationSec();
		$durationMax = $this->config->sliderMaxDurationSec();
		$fadeTimeMin = $this->config->sliderMinFadeTimeSec();
		$fadeTimeMax = $this->config->sliderMaxFadeTimeSec();

		$this->currentPresetCounter = rand($durationMin, $durationMax) * $this->config->targetFps();
		$this->progressDelta = 1.0 / ( rand($fadeTimeMin, $fadeTimeMax) * $this->config->targetFps() );
	}

    private function randomNextPreset(bool $excludeCurrent): Config
	{
		$presets = $this->presets;
		if ($excludeCurrent) {
			unset($presets[get_class($this->currentPreset)]);
		}
		$random = new WeightedRandom($presets);
		$presetClass = $random->next();
		return $this->presetFactory->create($presetClass);
	}

    public function gravity(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->gravity());
	}

    public function friction(): float
    {
        return $this->interpolateFloat(fn(Config $config) => $config->friction());
    }

    public function microMovementPower(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->microMovementPower());
	}

    public function microMovementFrequency(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->microMovementFrequency());
	}

    public function windGlobalVariation(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->windGlobalVariation());
	}

    public function windGlobalStrengthMin(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->windGlobalStrengthMin());
	}

    public function windGlobalStrengthMax(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->windGlobalStrengthMax());
	}

    public function windFieldVariation(): float
	{
		return $this->interpolateFloat(fn(Config $config) => $config->windFieldVariation());
	}

    public function windFieldStrengthMin(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->windFieldStrengthMin());
	}

    public function windFieldStrengthMax(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->windFieldStrengthMax());
	}

    public function windFieldGridUpdateEveryNthFrame(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->windFieldGridUpdateEveryNthFrame());
	}

    public function windFieldGridSize(): int {
		return $this->interpolateInt(fn(Config $config) => $config->windFieldGridSize());
	}

    public function snowProducingTempo(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->snowProducingTempo());
	}

    public function snowMaxNumOfFlakesAtOnce(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->snowMaxNumOfFlakesAtOnce());
	}

    public function snowProbabilityOfProducingFromTop(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->snowProbabilityOfProducingFromTop());
	}

    public function snowProducingGridUpdateEveryNthFrame(): int
    {
        return $this->interpolateInt(fn(Config $config) => $config->snowProducingGridUpdateEveryNthFrame());
    }

    public function snowProducingVariation(): float
    {
        return $this->interpolateFloat(fn(Config $config) => $config->snowProducingVariation());
    }

    public function snowProducingGridSize(): int
    {
        return $this->interpolateInt(fn(Config $config) => $config->snowProducingGridSize());
    }

	public function snowHowManyFlakesNeedsToFallToFormAHill(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->snowHowManyFlakesNeedsToFallToFormAHill());
	}
	
	public function snowIsPressedAfterFramesNumMin(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->snowIsPressedAfterFramesNumMin());
	}
	
	public function snowIsPressedAfterFramesNumMax(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->snowIsPressedAfterFramesNumMax());
	}
	
	public function windBlowsFrequency(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->windBlowsFrequency());
	}
	
	public function windBlowsMaxNumAtSameTime(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->windBlowsMaxNumAtSameTime());
	}
	
	public function windBlowsMinStrength(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->windBlowsMinStrength());
	}
	
	public function windBlowsMaxStrength(): int 
	{
		return $this->interpolateInt(fn(Config $config) => $config->windBlowsMaxStrength());
	}
	
	public function windBlowsMinAnimationDuration(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->windBlowsMinAnimationDuration());
	}
	
	public function windBlowsMaxAnimationDuration(): int
	{
		return $this->interpolateInt(fn(Config $config) => $config->windBlowsMaxAnimationDuration());
	}

	public function hasWind(): bool 
	{
		return true;
	}

	public function showFps(): bool 
	{
		return $this->closestPreset()->showFps();
	}

	public function extendWorkingAreaFactor(): float {
		return $this->interpolateFloat(fn(Config $config) => $config->extendWorkingAreaFactor());
	}

	private function interpolateFloat(callable $callback): float
	{
		if (empty($this->targetPreset) || $this->progress == 0.0) {
			return $callback($this->currentPreset);
		}

		return $this->interpolation->findFloat($this->progress, $callback($this->currentPreset), $callback($this->targetPreset));
	}
	
	private function interpolateInt(callable $callback): int
	{
		if (empty($this->targetPreset) || $this->progress == 0.0) {
			return $callback($this->currentPreset);
		}

		return $this->interpolation->findInt($this->progress, $callback($this->currentPreset), $callback($this->targetPreset));
	}

	private function closestPreset(): Config
	{
		return $this->progress <= 0.5 
			? $this->currentPreset
			: $this->targetPreset;
	}

    public function showScene(): bool
    {
        return $this->currentPreset->showScene();
    }
}
