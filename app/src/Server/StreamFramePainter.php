<?php declare(strict_types=1);

namespace TechBit\Snow\Server;

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

    private array $particlesBuffer = [];

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
            throw new \Exception("Cannot delete: {$pipeFile}");
        }

        if (!posix_mkfifo($pipeFile, 0777)) {
            throw new \Exception("Cannot create a named pipe: {$pipeFile}");
        }

        $this->pipe = fopen($pipeFile, "w");

        stream_set_blocking($this->pipe, true); 

        fwrite($this->pipe, "hello-php-snow");
    }

    public function startAnimation(): void
    {
        $this->fwriteData('NNN', 
            $this->canvasWidth,
            $this->canvasHeight,
            $this->startupConfig->targetFps(),
        );
    }

	public function startFrame(): void 
    {
        $this->particlesBuffer = [];
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

        $this->particlesBuffer[] = [
            $x, $y, $this->particles->shape($idx),
        ];
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

    public function endFrame(): void
    {        
        $this->fwriteData('NN', 
            ++$this->frameCounter, 
            count($this->particlesBuffer),
        );

        foreach($this->particlesBuffer as $particle) {
            $this->fwriteData('GGC', 
                $particle[0],
                $particle[1],
                $particle[2],
            );
        }
    }

	public function stopAnimation(): void 
    {
        $this->fwriteData('NN', 0xffffffff, 0);
        // sleep(3); # give client time to consume and shutdown gracefuly 
    }

    private function fwriteData(string $code, mixed... $args) 
    {
        fwrite($this->pipe, pack($code, ...$args));
    }

}