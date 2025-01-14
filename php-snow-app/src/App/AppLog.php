<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use Exception;

final class AppLog
{
    private static bool $firstTime = true;

    public static function debug(mixed ...$mixed): void
    {
        if (self::$firstTime) {
            self::$firstTime = false;        
            file_put_contents("app.log", '');
        }

        try{
            throw new Exception("Give me stack trace");
        } catch (Exception $e) {
            $class = $e->getTrace()[1]['class'];
            $method = $e->getTrace()[1]['function'];

            $where = substr($class . '::' . $method, strlen('Techbit\\Snow\\'));
        }

        file_put_contents("app.log", $where . "\n" . var_export($mixed, true) . "\n\n", FILE_APPEND);
    }
	
}
