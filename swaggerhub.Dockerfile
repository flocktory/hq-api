FROM node:17
COPY . /usr/src/hq-api
WORKDIR /usr/src/hq-api
RUN npm i -g swaggerhub-cli
CMD /bin/sh