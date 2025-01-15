<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Frame;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationAliveObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationConfigurableObject;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationVisibleObject;
use TechBit\Snow\Console\IConsole;

final class FrameStabilizer implements IAnimationVisibleObject, IAnimationAliveObject, IAnimationConfigurableObject
{

    private readonly IConsole $console;

    private readonly int $intervalMs;

    private int $lastSecond;

    private int $currentFps = 0;

    private float $lastFrame = 0.0;

    private int $frameCounter = 0;

    private bool $showFps = false;

    public function initialize(AnimationContext $context): void
    {
        $this->console = $context->console();

        $this->intervalMs = (int)(1000 / $context->config()->targetFps());
        $this->lastSecond = (int)microtime(true);
    }
	public function onConfigChange(Config $config): void 
    {
        $this->showFps = $config->showFps();
	}

    public function update(): void
    {
        if (!$this->lastFrame) {
            $this->lastFrame = microtime(true);
            return;
        }

        $nowMicro = microtime(true);
        $nowSec = (int)$nowMicro;

        ++$this->frameCounter;
        if ($this->lastSecond < $nowSec) {
            $this->lastSecond = $nowSec;
            $this->currentFps = $this->frameCounter;
            $this->frameCounter = 0;
        }

        $deltaSec = $nowMicro - $this->lastFrame;

        $waitMs = $this->intervalMs - $deltaSec * 1000;
        if ($waitMs > 0) {
            usleep((int)($waitMs * 1000));
        }

        $this->lastFrame = microtime(true);
    }


    public function renderFirstFrame(): void
    {

    }

    public function renderLoopFrame(): void
    {
        if ($this->showFps) {
            $this->console->printAt(0, 0, 'fps: ' . $this->currentFps . '     ');
        }
    }
}