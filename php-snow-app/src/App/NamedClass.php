<?php declare(strict_types=1);

namespace TechBit\Snow\App;

final class NamedClass
{

    public function __construct(
        private readonly string $namespace,
        private readonly string $classSuffix = '',
    )
    {
    }


    /**
     * @return class-string
     */
    public function toClassName(string $name): string
    {
        if ($name === '') {
            return '';
        }

        if (class_exists($name)) {
            return $name;
        }

        $fullClassname = "TechBit\\Snow\\" . $this->namespace . $name . $this->classSuffix;
        if (class_exists($fullClassname)) {
            return $fullClassname;
        }

        $className = ucwords($name, '_');
        $className = str_replace('_', '', $className);
        return "TechBit\\Snow\\" . $this->namespace . $className . $this->classSuffix;
    }

}