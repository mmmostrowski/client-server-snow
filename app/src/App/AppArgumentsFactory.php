<?php declare(strict_types=1);

namespace TechBit\Snow\App;

final class AppArgumentsFactory
{

    public function create(array $argv, string $projectRootDir, bool $isDeveloperMode): AppArguments
    {
        array_shift($argv);

        $serverSessionId = null;
        $serverCanvasWidth = 0;
        $serverCanvasHeight = 0;
        if ($this->isServer($argv)) {
            $this->read($argv);
            $serverSessionId = $this->read($argv);
            $serverCanvasWidth = (int)$this->read($argv);
            $serverCanvasHeight = (int)$this->read($argv);
        }
        $customScene = $this->isResource($argv) ? $this->readResource($argv) : null;
        $presetName = $this->read($argv);

        return new AppArguments($projectRootDir, $isDeveloperMode, 
            [], $presetName, $customScene, 
            $serverSessionId, $serverCanvasWidth, $serverCanvasHeight);
    }

    private function isResource(array $argv): bool
    {
        $value = $argv[0] ?? '';

        if (empty($value)) {
            return false;
        }

        return str_starts_with($value, 'base64:') || @file_exists($value) || @file_get_contents($value);
    }

    private function isServer(array $argv): bool
    {
        $value = $argv[0] ?? '';
        return $value == 'server';
    }

    private function readResource(array &$argv): string
    {
        $value = $this->read($argv);

        if (str_starts_with($value, 'base64:')) {
            return (string)@base64_decode(preg_replace('/^base64:/', '', $value));
        }

        return (string)@file_get_contents($value);
    }

    private function read(array &$argv): string
    {
        if (empty($argv)) {
            return '';
        }
        return array_shift($argv);
    }
}