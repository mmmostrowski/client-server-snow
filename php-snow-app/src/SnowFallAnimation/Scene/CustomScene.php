<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Scene;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationVisibleObject;
use TechBit\Snow\SnowFallAnimation\Snow\SnowBasis;
use TechBit\Snow\Console\ConsoleColor;


final class CustomScene implements IAnimationVisibleObject
{

    private readonly string $sceneTxt;

    private readonly SnowBasis $basis;

    private readonly StartupConfig $config;


    public function __construct(string $sceneTxt)
    {
        $this->sceneTxt = $sceneTxt;
    }

    public function initialize(AnimationContext $context): void
    {
        $this->basis = $context->snowBasis();
        $this->config = $context->config();
    }

    public function renderFirstFrame(): void
    {
        if (!$this->config->showScene()) {
            return;
        }

        $this->basis->drawGround();

        $this->basis->drawCharsInCenter(
            $this->sceneTxt,
            0, 0,
            ConsoleColor::BLUE
        );
    }

    public function renderLoopFrame(): void
    {
    }

}