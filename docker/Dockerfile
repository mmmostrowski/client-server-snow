FROM php:8.2.3-cli-alpine3.17

WORKDIR /app/

ARG UID=1000
ARG GID=1000

RUN addgroup -S devbox -g ${GID} \
    && adduser -S devbox -G devbox -u ${UID} \
    && apk add ncurses \
    && apk add --update nodejs npm \
    && apk update  \
    && apk upgrade  \
    && apk add bash \
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

