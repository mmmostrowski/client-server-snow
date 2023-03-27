#!/usr/bin/env bash
set -eu

function main() {
    installNodeModulesToHost

    if [[ "${1:-}" == 'bash' ]] || [[ "${1:-}" == 'dev' ]]; then
        echo ''
        echo '--'
        echo ''
        echo 'To start developing: npm run start'
        echo ''
        echo 'To start serve production: serve -s build -n'
        echo ''
        echo ''
        export PHP_SNOW_APP_MODE=develop
        bash -l
        return 0
    fi

    if [[ "${PHP_SNOW_APP_MODE:-}" == "develop" ]]; then
        npm run start
        return 0
    fi

    "${@}"
}

function installNodeModulesToHost()
{
    if [[ -e /snow/reactjs-client/node_modules/ ]] \
        && cmp /snow/reactjs-client/node_modules/.package-lock.json /snow-node-modules/.package-lock.json; then
        return 0
    fi

    rm -rf /snow/reactjs-client/node_modules/
    cp -rf /snow-node-modules/ /snow/reactjs-client/node_modules/
}

main "${@}"