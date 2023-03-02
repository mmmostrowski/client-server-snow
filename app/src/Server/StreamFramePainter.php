<?php declare(strict_types=1);

namespace TechBit\Snow\Server;

use TechBit\Snow\Console\IConsole;
use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationObject;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\Console\ConsoleColor;


final class StreamFramePainter implements IFramePainter, IAnimationObject
{

    private readonly SnowParticles $particles;

    private int $frameCounter = 0;

    private $pipe;

    public function __construct(
        private readonly string $sessionId,
        private readonly string $pipesDir,        
        private readonly StartupConfig $startupConfig,
        private readonly int $canvasWidth,
        private readonly int $canvasHeight,
    ) {        
    }

    public function initialize(AnimationContext $context): void
    {
        $this->particles = $context->snowParticles();

        $pipeFile = $this->pipesDir . "/" . $this->sessionId;        

        if (file_exists($pipeFile) && !unlink($pipeFile)) {
            throw new \Exception("Cannot delete a named pipe file: {$pipeFile}");
        }

        if (!posix_mkfifo($pipeFile, 0777)) {
            throw new \Exception("Cannot create a named pipe: {$pipeFile}");
        }
        
        $this->pipe = fopen($pipeFile, "w+");
    }

    public function startFirstFrame(): void
    {
        fwrite($this->pipe, pack('LLL', 
            $this->canvasWidth,
            $this->canvasHeight,
            $this->startupConfig->targetFps(),
        ));
    }

	public function startNewFrame(): void 
    {
        fwrite($this->pipe, pack('LL', 
            ++$this->frameCounter, 
            $this->particles->count(),
        ));
	}

    public function renderParticle(int $idx): void
    {
        $x = $this->particles->x($idx);
        if ($x < 0 || $x >= $this->canvasWidth) {
            return;            
        }

        $y = $this->particles->y($idx);
        if ($y < 0  || $y >= $this->canvasHeight) {
            return;            
        }

        fwrite($this->pipe, pack('ffC', 
            $x, 
            $y, 
            $this->particles->shape($idx),
        ));
    }

    public function renderBasisParticle(float $x, float $y, string $shape): void
    {
    }

    public function renderBackgroundPixel(float $x, float $y, string $char, ConsoleColor $color): void
    {
    }

	public function eraseParticle(int $idx): void 
    {
	}
}