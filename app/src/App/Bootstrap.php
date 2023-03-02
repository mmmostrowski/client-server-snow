<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use TechBit\Snow\App;
use TechBit\Snow\App\Exception\AppUserException;
use TechBit\Snow\Server\MockConsole;
use TechBit\Snow\Server\StreamFramePainter;
use TechBit\Snow\SnowFallAnimation\AnimationFactory;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfig;
use Throwable;

final class Bootstrap
{

    public static function createArguments(array $argv, string $projectRootDir, bool $isDeveloperMode): AppArguments
    {
        return (new AppArgumentsFactory())->create($argv, $projectRootDir, $isDeveloperMode);
    }

    public static function createApp(AppArguments $appArguments): IApp
    {
        if ($appArguments->isServer()) {
            $startupConfig = new StartupConfig();
            return new App(new AnimationFactory(
                console: new MockConsole(),
                startupConfig : $startupConfig,
                renderer : new StreamFramePainter(
                    $appArguments->serverSessionId(),
                    $appArguments->projectRootDir() . "/.pipes",
                    $startupConfig,
                    $appArguments->serverCanvasWidth(),
                    $appArguments->serverCanvasHeight(),
                ))
            );
        }
        return new App();
    }

    public static function run(IApp $app, AppArguments $appArguments): int
    {
        try {
            error_reporting($appArguments->isDeveloperMode() ? E_ALL : E_ERROR | E_PARSE);
            ini_set('display_errors', $appArguments->isDeveloperMode() ? 'On' : 'Off');

            srand(time());

            $app->run($appArguments);

            return 0;
        } catch (AppUserException $e) {
            echo PHP_EOL;
            echo $e->getMessage();
            echo PHP_EOL;
            return 1;
        } catch (Throwable $e) {
            echo $appArguments->isDeveloperMode() ? $e : 'Unknown error';
            echo PHP_EOL;
            return 1;
        }
    }

}
