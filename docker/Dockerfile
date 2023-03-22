FROM php:8.2.3-cli-alpine3.17

WORKDIR /app/

ARG UID=1000
ARG GID=1000
ARG PHP_SNOW_APP_MODE=production

RUN addgroup -S devbox -g ${GID} \
    && adduser -S devbox -G devbox -u ${UID} \
    && apk update \
    && apk add ncurses \
    && apk add --update nodejs npm \
    && apk upgrade  \
    && apk add bash \
;


# Install X-Debug for develop mode
RUN if [[ ${PHP_SNOW_APP_MODE} == develop ]]; then \
        apk update \
        && apk add busybox-extras \
        && apk --no-cache add pcre-dev \
        && apk add --update linux-headers \
        && pecl install xdebug-3.2.0 \
        && docker-php-ext-enable xdebug \
        && apk del pcre-dev \
        && ( \
            echo '[xdebug]' \
            && echo 'xdebug.mode=off' \
            && echo 'xdebug.client_host=host.docker.internal' \
            && echo 'xdebug.start_with_request=trigger' \
        ) > /usr/local/etc/php/conf.d/my-xdebug.ini \
        && chmod 666 /usr/local/etc/php/conf.d/my-xdebug.ini; \
    fi \
;


COPY --from=composer:2.2.7 /usr/bin/composer /usr/local/bin/composer
COPY /app/composer.json /app/
COPY /app/lib/ /app/lib/
COPY /docker/entrypoint.sh /
COPY /app/snow.php /app/snow.php
COPY /app/src/ /app/src/

RUN ( \
    echo "export PS1='\u@\h:\w\$ '"; \
    echo 'alias ll="ls -lah"'; \
) >> /etc/profile

RUN composer install \
    && cp -rf /app/vendor/ /app-vendor/

USER devbox

ENTRYPOINT [ "bash", "-l", "/entrypoint.sh"  ]

