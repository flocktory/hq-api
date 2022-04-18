FROM node:17
ARG TOKEN
ENV TOKEN=$TOKEN
RUN bash -c "`curl -sL https://raw.githubusercontent.com/buildkite/agent/main/install.sh`"
RUN cp /root/.buildkite-agent/bin/buildkite-agent /usr/local/bin
RUN npm install -g npm-cli-login
CMD /bin/sh