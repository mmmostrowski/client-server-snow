<?php declare(strict_types=1);

namespace TechBit\Snow\Console;


class Console implements IConsole
{

    protected float $cols = 0.0;

    protected float $rows = 0.0;

    private int $refreshingCounter = 0;


    public function __construct()
    {
        $this->refreshConsoleSize();
        register_shutdown_function(function () {
            echo "\033[?25h";
        });
    }

    /**
     * @throws InvalidConsoleSizeException
     */
    public function ensureConsoleValidSize(int $minWidth, int $minHeight): void
    {
        $width = $this->width();
        $height = $this->height();

        // if ($width < $minWidth || $height < $minHeight) {
        //     throw new InvalidConsoleSizeException("Console size must be at least {$minWidth}x{$minHeight}!\n" .
        //         "Current console size is {$width}x{$height}.\n\nPlease make your terminal window larger!");
        // }
    }

    public function refreshConsoleSize(): void
    {
        $this->cols = (float)exec('tput cols');
        $this->rows = (float)exec('tput lines');
    }

    public function switchToColor(ConsoleColor $color): void
    {
        echo $color->terminalCode();
    }

    public function resetColor(): void
    {
        echo ConsoleColor::RESET->terminalCode();
    }

    public function width(): float
    {
        return $this->maxX() - $this->minX();
    }

    public function maxX(): float
    {
        return $this->cols;
    }

    public function minX(): float
    {
        return 0;
    }

    public function height(): float
    {
        return $this->maxY() - $this->minY();
    }

    public function maxY(): float
    {
        return $this->rows;
    }

    public function minY(): float
    {
        return 0;
    }

    public function isIn(float $x, float $y): bool
    {
        return $x >= $this->minX() && $x <= $this->maxX() && $y >= $this->minY() && $y <= $this->maxY();
    }

    public function centerX(): float
    {
        return ($this->maxX() - $this->minX()) / 2;
    }

    public function centerY(): float
    {
        return ($this->maxY() - $this->minY()) / 2;
    }

    public function clear(): void
    {
        for ($y = $this->minY(); $y <= $this->maxY(); ++$y) {
            for ($x = $this->minX(); $x <= $this->maxX(); ++$x) {
                $this->printAt($x, $y, ' ');
            }
        }
    }

    public function printAt(float $x, float $y, string $txt): void
    {
        if ($x < $this->minX() || $x > $this->maxX() || $y < $this->minY() || $y > $this->maxY()) {
            return;
        }

        $x = (int)$x;
        $y = (int)$y;

        # move cursor
        echo "\033[?25l";
        echo "\033[{$y};{$x}H";

        # print
        echo $txt;
    }

}