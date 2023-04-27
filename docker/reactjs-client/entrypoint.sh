#!/usr/bin/env bash
set -eu

function main() {
    installFreshCopyOfNodeModules
    installFreshCopyOfNodeBuild

    if [[ "${1:-}" == 'bash' ]] || [[ "${1:-}" == 'dev' ]]; then
        echo ''
        echo '--'
        echo ''
        echo 'To start reactjs-client server: npm run pack && npm run build && node app.js'
        echo 'To start reactjs-client development: npm run start'
        echo ''
        echo ''
        bash -l
        return 0
    fi

    if [[ "${PHP_SNOW_APP_MODE:-}" == "develop" ]]; then
        npm run start
        return 0
    fi

    "${@}"
}

function installFreshCopyOfNodeModules() {
    if [[ -e /snow/reactjs-client/node_modules/ ]] \
        && cmp /snow/reactjs-client/node_modules/.package-lock.json /data/snow-node-modules/.package-lock.json; then
        return 0
    fi

    rm -rf /snow/reactjs-client/node_modules/
    cp -rf /data/snow-node-modules/ /snow/reactjs-client/node_modules/
}

function installFreshCopyOfNodeBuild() {
    if [[ ! -e /data/snow-node-build/ ]]; then
        return;
    fi

    rm -rf /snow/reactjs-client/build/
    cp -rf /data/snow-node-build/ /snow/reactjs-client/build/
}

main "${@}"