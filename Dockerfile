FROM openjdk:13
COPY . /usr/src/hq-api
WORKDIR /usr/src/hq-api
CMD /bin/sh