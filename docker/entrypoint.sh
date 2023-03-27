#!/usr/bin/env bash
set -eu

function main() {
    installFreshCopyOfVendorFolder
    installFreshCopyOfGradleFolders

    if isAskingForDev "${1:-}" || ( [[ "${1:-}" == "snow-server" ]] && isAskingForDev "${2:-}" ); then
        echo ''
        echo '--'
        echo ''
        echo "To run php snow app please execute: php snow.php [ args ... ]"
        echo "To enable XDebug please run: echo 'xdebug.mode=debug' >> /usr/local/etc/php/conf.d/my-xdebug.ini"
        echo ''
        echo "To start java-proxy server: java -jar /snow/java-proxy/build/libs/proxy-0.0.1-SNAPSHOT.jar"
        echo "To start java-proxy development: gradle --project-dir /snow/java-proxy bootRun"
        echo ''
        echo 'To start reactjs-client server ( please run from host ): ./run snow-client '
        echo 'To start reactjs-client development ( please run from host ): ./run snow-client dev'
        echo ''
        export PHP_SNOW_APP_MODE=develop
        bash -l
        return 0
    fi

    if [[ "${1:-}" == "snow-server" ]]; then
        java -jar /snow/java-proxy/build/libs/proxy-0.0.1-SNAPSHOT.jar
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

function installFreshCopyOfVendorFolder() {
    rm -rf /snow/app/vendor/
    cp -rf /data/app-vendor/ /snow/app/vendor/
}

function installFreshCopyOfGradleFolders() {
    rm -rf /snow/java-proxy/.gradle/
    rm -rf /snow/java-proxy/build/
    cp -rf /data/app-gradle/ /snow/java-proxy/.gradle/
    cp -rf /data/app-gradle-build/ /snow/java-proxy/build/
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

function isAskingForDev() {
    local param="${1}"

    [[ "${param}" == 'bash' ]] || [[ "${param}" == 'dev' ]]
}

main "${@}"
