<?php declare(strict_types=1);

namespace TechBit\Snow\App;

use TechBit\Snow\App;
use TechBit\Snow\App\Exception\AppUserException;
use TechBit\Snow\Server\MockConsole;
use TechBit\Snow\Server\StreamFramePainter;
use TechBit\Snow\SnowFallAnimation\AnimationFactory;
use TechBit\Snow\SnowFallAnimation\Config\StartupConfigFactory;
use TechBit\Snow\SnowFallAnimation\Snow\SnowFlakeShape;
use Throwable;

final class Bootstrap
{

    public static function createArguments(array $argv, bool $isDeveloperMode, array $additional): AppArguments
    {
        return (new AppArgumentsFactory())->create($argv, $isDeveloperMode, $additional);
    }

    public static function createApp(AppArguments $appArguments): IApp
    {
        if ($appArguments->isServer()) {
            $startupConfig = (new StartupConfigFactory())->create($appArguments);
            $flakeShapes = new SnowFlakeShape();
            return new App(new AnimationFactory(
                renderer : new StreamFramePainter(
                    $appArguments->serverSessionId(),
                    $appArguments->serverPipesDir(),
                    $startupConfig,
                    $appArguments->serverCanvasWidth(),
                    $appArguments->serverCanvasHeight(),
                    $flakeShapes
                ),
                flakeShapes : $flakeShapes,
                console: new MockConsole(
                    $appArguments->serverCanvasWidth(),
                    $appArguments->serverCanvasHeight(),
                ),
                startupConfig : $startupConfig,
            ));
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
            echo $appArguments->isDeveloperMode() ? $e : "Error: {$e->getMessage()}";
            echo PHP_EOL;
            return 1;
        }
    }

}
