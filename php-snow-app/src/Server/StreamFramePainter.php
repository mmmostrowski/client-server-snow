<?php declare(strict_types=1);

namespace TechBit\Snow\Server;

use Exception;
use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use TechBit\Snow\SnowFallAnimation\Frame\IFramePainter;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationObject;
use TechBit\Snow\SnowFallAnimation\Snow\ISnowFlakeShape;
use TechBit\Snow\SnowFallAnimation\Snow\SnowParticles;
use TechBit\Snow\Console\ConsoleColor;


final class StreamFramePainter implements IFramePainter, IAnimationObject
{

    private readonly SnowParticles $particles;

    /**
     * @var resource
     */
    private $pipe;

    private int $frameCounter = 0;

    private array $particlesBuffer = [];

    private array $backgroundPixels = [];

    private array $basisPixels = [];

    private int $basisPixelsCount = 0;

    private bool $debugToScreen = false;

    public function __construct(
        private readonly string $sessionId,
        private readonly string $pipesDir,
        private readonly StartupConfig $startupConfig,
        private readonly int $canvasWidth,
        private readonly int $canvasHeight,
        private readonly ISnowFlakeShape $flakes,
    ) {
        if (getenv("DEBUG_TO_SCREEN")) {
            $this->debugToScreen = true;
        }
    }

    /**
     * @throws Exception
     */
    public function initialize(AnimationContext $context): void
    {
        $this->particles = $context->snowParticles();

        $pipeFile = $this->pipesDir . "/" . $this->sessionId;

        if (file_exists($pipeFile) && !unlink($pipeFile)) {
            throw new Exception("Cannot delete: $pipeFile");
        }

        if (!posix_mkfifo($pipeFile, 0777)) {
            throw new Exception("Cannot create a named pipe: $pipeFile");
        }

        if (!$this->debugToScreen) {
            $this->pipe = fopen($pipeFile, "w");

            stream_set_blocking($this->pipe, true);

            fwrite($this->pipe, "hello-php-snow");
        }
    }

    public function startAnimation(): void
    {
        $this->writeData('NNN',
            $this->canvasWidth,
            $this->canvasHeight,
            $this->startupConfig->targetFps(),
        );
    }

    public function startFirstFrame(): void
    {
        $this->backgroundPixels = [];
    }

    public function endFirstFrame(): void
    {
        // background
        if (!$this->backgroundPixels) {
            $this->writeData('C', 0);
            return;
        }
        $this->writeData('C', 1);
        $this->writeData('N', $this->canvasWidth);
        $this->writeData('N', $this->canvasHeight);
        for ($y = 0; $y < $this->canvasHeight; ++$y) {
            for ($x = 0; $x < $this->canvasWidth; ++$x) {
                $this->writeData('C', $this->backgroundPixels[$x][$y] ?? 0);
            }
        }
        $this->backgroundPixels = [];
    }

    public function startFrame(): void
    {
        $this->particlesBuffer = [];
        $this->basisPixels = [];
        $this->basisPixelsCount = 0;
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
            $x, $y, $this->flakes->shapeIdx($this->particles->shape($idx)),
        ];
    }

    public function renderBasisParticle(float $x, float $y, string $shape): void
    {
        ++$this->basisPixelsCount;
        $this->basisPixels[(int)$x][(int)$y] = $this->flakes->shapeIdx($shape);
    }

    public function renderBackgroundPixel(float $x, float $y, string $char, ConsoleColor $color): void
    {
        $this->backgroundPixels[(int)$x][(int)$y] = ord($char);
    }

    public function eraseParticle(int $idx): void
    {
    }

    public function endFrame(): void
    {
        // frame num
        $this->writeData('NN',
            ++$this->frameCounter,
            count($this->particlesBuffer),
        );

        // particles
        foreach($this->particlesBuffer as $particle) {
            $this->writeData('GGC',
                $particle[0], // X
                $particle[1], // Y
                $particle[2]  // c
            );
        }

        // basis
        $this->writeData('N', $this->basisPixelsCount);
        foreach($this->basisPixels as $x => $colum) {
            foreach($colum as $y => $pixel) {
                $this->writeData('NNC', $x, $y, $pixel);
            }
        }
    }

    public function stopAnimation(): void
    {
        $this->writeData('N', 0xffffffff);
    }

    private function writeData(string $code, mixed... $args): void
    {
        if ($this->debugToScreen) {
            var_dump($args);
            return;
        }
        fwrite($this->pipe, pack($code, ...$args));
    }

}