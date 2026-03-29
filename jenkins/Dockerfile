# Jenkins + Docker CLI + Compose v2 (plugin dans cli-plugins)
FROM jenkins/jenkins:lts-jdk21
USER root
ARG DOCKER_COMPOSE_VERSION=2.29.7
RUN apt-get update \
    && apt-get install -y --no-install-recommends docker.io ca-certificates curl \
    && mkdir -p /usr/local/lib/docker/cli-plugins \
    && curl -fsSL "https://github.com/docker/compose/releases/download/v${DOCKER_COMPOSE_VERSION}/docker-compose-linux-x86_64" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose \
    && chmod +x /usr/local/lib/docker/cli-plugins/docker-compose \
    && rm -rf /var/lib/apt/lists/*
USER jenkins
