#!/usr/bin/env bash
set -eu

function main() {
    installVendorFolderToHost

    if [[ "${1:-}" == 'bash' ]] || [[ "${1:-}" == 'dev' ]]; then
        echo ''
        echo '--'
        echo ''
        echo "To run app please execute: php snow.php [ args ... ]"
        echo ''
        echo "To enable XDebug please run: echo 'xdebug.mode=debug' >> /usr/local/etc/php/conf.d/my-xdebug.ini"
        echo ''
        echo "To start java-proxy: gradle --project-dir /snow/java-proxy bootRun"
        echo ''
        echo ''
        export PHP_SNOW_APP_MODE=develop
        bash -l
        return 0
    fi

    if [[ "${1:-}" == "snow-server" ]]; then
        gradle --project-dir /snow/java-proxy bootRun
        return 0
    fi

    waitUntilTerminalSizeIsAvailable 3s

    if [[ "${PHP_SNOW_APP_MODE:-}" != "develop" ]]; then
        terminalCleanupOnExit true
    fi

    
    if ! "php" "snow.php" "${@}"; then
        terminalCleanupOnExit false
        return 1
    fi
}

function installVendorFolderToHost()
{
    if [[ -e /snow/app/vendor/ ]] \
        && cmp /snow/app/vendor/composer/installed.json /app-vendor/composer/installed.json; then
        return
    fi

    rm -rf /snow/app/vendor/
    cp -rf /app-vendor/ /snow/app/vendor/
}

function waitUntilTerminalSizeIsAvailable() {
    local waitSec="${1%s}"

    local iterations=$(( waitSec * 2 ))
    while (( --iterations >= 0 )); do
        local cols
        cols="$( tput cols )"

        if (( cols != 0 )) && (( cols != 80 )); then
            break
        fi

        sleep 0.5;
    done
}

function terminalCleanupOnExit() {
    local trapEnabled="${1:-true}"

    if $trapEnabled; then
        trap "reset; clear"  EXIT
    else
        trap ""  EXIT
    fi
}

main "${@}"
