<?php declare(strict_types=1);

namespace TechBit\Snow\SnowFallAnimation\Scene;

use TechBit\Snow\SnowFallAnimation\AnimationContext;
use TechBit\Snow\SnowFallAnimation\Object\IAnimationVisibleObject;
use TechBit\Snow\SnowFallAnimation\Snow\SnowBasis;
use TechBit\Snow\Console\ConsoleColor;
use TechBit\Snow\Console\IConsole;


final class Scene implements IAnimationVisibleObject
{

    private readonly string $credentialsText;

    private readonly IConsole $console;

    private readonly SnowBasis $basis;

    private readonly bool $isShowingScene;


    public function __construct()
    {
        $this->credentialsText = "[ 2023 (C) Maciej Ostrowski | https://github.com/mmmostrowski ]";
    }

    public function initialize(AnimationContext $context): void
    {
        $this->console = $context->console();
        $this->basis = $context->snowBasis();
        $this->isShowingScene = $context->config()->showScene();
    }

    public function renderFirstFrame(): void
    {
        if (!$this->isShowingScene) {
            return;
        }

        $this->basis->drawGround();

        $this->basis->drawCharsInCenter($this->drawPHP(),
            0,
            -7,
            ConsoleColor::LIGHT_BLUE);

        $this->basis->drawCharsInCenter($this->drawIsAwesome(),
            38,
            10,
            ConsoleColor::BLUE
        );

        $this->basis->drawChars($this->credentialsText,
            (int)($this->console->maxX() - strlen($this->credentialsText) / 2),
            (int)($this->console->maxY()),
            ConsoleColor::BLUE
        );
    }

    private function drawPHP(): string
    {
        return <<<EOL
PPPPPPPPPPPPPPPPP        HHHHHHHHH     HHHHHHHHH     PPPPPPPPPPPPPPPPP   
P::::::::::::::::P       H:::::::H     H:::::::H     P::::::::::::::::P  
P::::::PPPPPP:::::P      H:::::::H     H:::::::H     P::::::PPPPPP:::::P 
PP:::::P     P:::::P     HH::::::H     H::::::HH     PP:::::P     P:::::P
  P::::P     P:::::P       H:::::H     H:::::H         P::::P     P:::::P
  P::::P     P:::::P       H:::::H     H:::::H         P::::P     P:::::P
  P::::PPPPPP:::::P        H::::::HHHHH::::::H         P::::PPPPPP:::::P 
  P:::::::::::::PP         H:::::::::::::::::H         P:::::::::::::PP  
  P::::PPPPPPPPP           H:::::::::::::::::H         P::::PPPPPPPPP    
  P::::P                   H::::::HHHHH::::::H         P::::P            
  P::::P                   H:::::H     H:::::H         P::::P            
  P::::P                   H:::::H     H:::::H         P::::P            
PP::::::PP               HH::::::H     H::::::HH     PP::::::PP          
P::::::::P               H:::::::H     H:::::::H     P::::::::P          
P::::::::P               H:::::::H     H:::::::H     P::::::::P          
PPPPPPPPPP               HHHHHHHHH     HHHHHHHHH     PPPPPPPPPP
EOL;
    }

    private function drawIsAwesome(): string
    {
        return <<<EOL
88                  ,ad8888ba,                                                88
""                 d8"'    `"8b                                        ,d     88
                  d8'                                                  88     88
88  ,adPPYba,     88             8b,dPPYba,   ,adPPYba,  ,adPPYYba,  MM88MMM  88
88  I8[    ""     88      88888  88P'   "Y8  a8P_____88  ""     `Y8    88     88
88   `"Y8ba,      Y8,        88  88          8PP"""""""  ,adPPPPP88    88     ""
88  aa    ]8I      Y8a.    .a88  88          "8b,   ,aa  88,    ,88    88,    aa
88  `"YbbdP"'       `"Y88888P"   88           `"Ybbd8"'  `"8bbdP"Y8    "Y888  88                                                                                
EOL;
    }

    public function renderLoopFrame(): void
    {
    }

}