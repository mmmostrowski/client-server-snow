#!/usr/bin/env bash
set -eu

function main() {
    installFreshCopyOfVendorFolder
    installFreshCopyOfGradleFolders

    if isAskingForDev "${1:-}" || ( [[ "${1:-}" == "snow-server" ]] && isAskingForDev "${2:-}" ); then
        cd /snow/java-proxy
        echo ''
        echo '--'
        echo ''
        echo "To run php snow app: cd /snow/app; php snow.php [ args ... ]"
        echo "To enable XDebug: echo 'xdebug.mode=debug' >> /usr/local/etc/php/conf.d/my-xdebug.ini"
        echo "To stream data to output instead of named pipe: export DEBUG_TO_SCREEN=1"
        echo ''
        echo "To start java-proxy server: java -jar /snow/java-proxy/build/libs/proxy-0.0.1-SNAPSHOT.jar"
        echo "To start java-proxy development: gradle --project-dir /snow/java-proxy bootRun"
        echo "To test java-proxy: gradle --project-dir /snow/java-proxy test"
        echo ''
        bash -l
        return 0
    fi

    if [[ "${1:-}" == "snow-server" ]]; then
        (
            mkdir -p "/snow/.pipes/";
            cd /snow/java-proxy/
            if [[ ! -e /snow/java-proxy/build/libs/proxy-0.0.1-SNAPSHOT.jar ]] || [[ ! -e  /data/app-gradle-build/ ]]; then
                gradle --no-daemon  bootJar --project-dir /snow/java-proxy/
            fi
            java -jar /snow/java-proxy/build/libs/proxy-0.0.1-SNAPSHOT.jar
        )
        return 0
    fi

    if [[ "${1:-}" == "test" ]]; then
        (
            mkdir -p "/snow/.pipes/";
            cd /snow/java-proxy/
            gradle "${@}"
        )
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
    purgeDirectory /snow/app/vendor/
    cp -rf /data/app-vendor/. /snow/app/vendor/
}

function installFreshCopyOfGradleFolders() {
    if [[ -e /data/app-gradle/ ]]; then
        purgeDirectory /snow/java-proxy/.gradle/
        cp -rf /data/app-gradle/. /snow/java-proxy/.gradle/
    fi

    if [[ -e /data/app-gradle-build/ ]]; then
        purgeDirectory /snow/java-proxy/build/
        cp -rf /data/app-gradle-build/. /snow/java-proxy/build/
    fi
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

function purgeDirectory() {
    local dir="${1}"
    shopt -s dotglob
    rm -rf "${dir}/"*
    shopt -u dotglob
}

main "${@}"
