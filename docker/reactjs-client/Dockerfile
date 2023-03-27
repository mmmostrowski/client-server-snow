FROM node:19-alpine3.16

WORKDIR /snow/reactjs-client/

RUN apk update \
    && apk upgrade  \
    && apk add bash curl nano vim \
    && ( \
        echo "export PS1='\u@\h:\w\$ '"; \
        echo 'alias ll="ls -lah"'; \
    ) >> /etc/profile \
;

COPY /docker/reactjs-client/entrypoint.sh /
COPY /reactjs-client/src/ /snow/reactjs-client/src/
COPY /reactjs-client/public/ /snow/reactjs-client/public/
COPY /reactjs-client/package.json /snow/reactjs-client/package.json
COPY /reactjs-client/package-lock.json /snow/reactjs-client/package-lock.json
COPY /reactjs-client/tsconfig.json /snow/reactjs-client/tsconfig.json
COPY /reactjs-client/webpack.config.js /snow/reactjs-client/webpack.config.js



RUN chown node:node /snow/ -R \
    && npm install \
    && npm install -g serve \
    && npm run pack \
    && npm run build \
    && cp -rf /snow/reactjs-client/node_modules/ /snow-node-modules/ \
;


USER node
EXPOSE 8181
EXPOSE 3000

ARG PHP_SNOW_APP_MODE=production

ENTRYPOINT [ "/entrypoint.sh" ]
CMD [ "serve", "-s", "build", "--no-clipboard" ]