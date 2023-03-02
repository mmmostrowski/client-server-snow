<?php declare(strict_types=1);

namespace TechBit\Snow\Server;
use TechBit\Snow\Console\Console;


final class MockConsole extends Console
{

    public function ensureConsoleValidSize(int $minWidth, int $minHeight): void
    {
    }

}