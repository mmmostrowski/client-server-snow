<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation;

use TechBit\Snow\SnowFallAnimation\Config\Config;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Object\AnimationObjects;
use TechBit\Snow\App\IAnimation;
use TechBit\Snow\Console\IConsole;
use TechBit\Snow\Console\InvalidConsoleSizeException;


final class SnowFallAnimation implements IAnimation
{

    private IConsole $console;
    private IFramePainter $painter;

    public function __construct(
        private readonly AnimationContext $context,
        private readonly AnimationObjects $objects,
        private readonly Config $config,
        private readonly StartupConfig $startupConfig,
    )
    {
        $this->console = $this->context->console();
        $this->painter = $this->context->painter();
    }
    
    /**
     * @throws InvalidConsoleSizeException
     */
    public function initialize(): void
    {
        srand();

        $this->console->ensureConsoleValidSize(
            $this->startupConfig->minRequiredConsoleWidth(),
            $this->startupConfig->minRequiredConsoleHeight(),
        );

        foreach ($this->objects->allConfigurableObjects() as $object) {
            $object->onConfigChange($this->config);
        }        

        foreach ($this->objects->allObjects() as $object) {
            $object->initialize($this->context);
        }
    }

    public function play(): void
    {
        $this->painter->startAnimation();

        $visibleObjects = $this->objects->allVisibleObjects();
        $aliveObjects = $this->objects->allAliveObjects();
        $configurableObjects = $this->objects->allConfigurableObjects();

        foreach ($visibleObjects as $object) {
            $object->renderFirstFrame();
        }

        $maxFrames = $this->context->config()->animationLengthInFrames();
        while (--$maxFrames) {
            $this->painter->startFrame();

            foreach ($configurableObjects as $object) {
                $object->onConfigChange($this->config);
            }        

            foreach ($aliveObjects as $object) {
                $object->update();
            }

            foreach ($visibleObjects as $object) {
                $object->renderLoopFrame();
            }

            $this->painter->endFrame();
        }

        echo "\n";
        echo "                            \n";
        echo "  Thank you for watching!   \n";
        echo "                            \n";
    }

}