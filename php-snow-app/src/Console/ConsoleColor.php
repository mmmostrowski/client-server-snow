<?php declare(strict_types=1);

namespace TechBit\Snow\Console;


enum ConsoleColor
{
    case RESET;

    case BLACK;
    case WHITE;
    case BLUE;
    case LIGHT_BLUE;

    public function terminalCode(): string
    {
        return match ($this) {
            self::RESET => "\e[0m",

            self::BLACK => "\e[48;5;232m",
            self::WHITE => "\e[38;5;195m",
            self::BLUE => "\e[38;5;25m",
            self::LIGHT_BLUE => "\e[38;5;39m",
        };
    }

}
