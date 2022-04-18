FROM openjdk:13
COPY . /usr/src/hq-api
WORKDIR /usr/src/hq-api
ARG TOKEN
ENV TOKEN=$TOKEN
RUN bash -c "`curl -sL https://raw.githubusercontent.com/buildkite/agent/main/install.sh`"
RUN cp /root/.buildkite-agent/bin/buildkite-agent /usr/local/bin
CMD /bin/sh