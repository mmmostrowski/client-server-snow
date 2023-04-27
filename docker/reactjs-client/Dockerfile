FROM node:19-alpine3.16

WORKDIR /snow/reactjs-client/

RUN apk update \
    && apk upgrade  \
    && apk add bash curl nano vim \
    && echo 'ls -lah "${@}"' > /usr/local/bin/ll \
    && chmod a+x /usr/local/bin/ll \
    && echo "export PS1='\u@\h:\w\$ '" >> /etc/profile \
;

COPY /reactjs-client/.env /snow/reactjs-client/.env
COPY /reactjs-client/app.js /snow/reactjs-client/app.js
COPY /reactjs-client/src/ /snow/reactjs-client/src/
COPY /reactjs-client/public/ /snow/reactjs-client/public/
COPY /reactjs-client/package.json /snow/reactjs-client/package.json
COPY /reactjs-client/package-lock.json /snow/reactjs-client/package-lock.json
COPY /reactjs-client/tsconfig.json /snow/reactjs-client/tsconfig.json
COPY /reactjs-client/webpack.config.js /snow/reactjs-client/webpack.config.js

ARG PHP_SNOW_APP_MODE=production

RUN mkdir -p /data/ \
    && chown node:node -R /data/ \
    && chown node:node -R /snow/ \
    && npm install \
    && cp -rf /snow/reactjs-client/node_modules/ /data/snow-node-modules/ \
    && if [[ ${PHP_SNOW_APP_MODE} == production ]]; then \
          npm run pack \
          && npm run build \
          && cp -rf /snow/reactjs-client/build/ /data/snow-node-build/ \
          && chown node:node -R /data/; \
       fi \
    && chown node:node -R /snow/ \
;


USER node
EXPOSE 8181
EXPOSE 3000

COPY /docker/reactjs-client/entrypoint.sh /
ENTRYPOINT [ "/entrypoint.sh" ]
CMD [ "node", "app.js" ]