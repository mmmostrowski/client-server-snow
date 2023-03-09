<?php declare(strict_types=1);

namespace TechBit\Snow\Server;
use TechBit\Snow\Console\Console;
use TechBit\Snow\Console\ConsoleColor;


final class MockConsole extends Console
{

    public function __construct(int $cols, int $rows)
    {
        $this->cols = $cols;
        $this->rows = $rows;
    }

    public function ensureConsoleValidSize(int $minWidth, int $minHeight): void
    {
    }

    public function refreshConsoleSize(): void
    {
    }

    public function switchToColor(ConsoleColor $color): void
    {
    }

    public function resetColor(): void
    {
    }

    public function clear(): void
    {
    }

    public function printAt(float $x, float $y, string $txt): void
    {
    }

}