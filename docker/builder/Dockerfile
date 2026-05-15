# =====================================================================
# Fejlesztői build környezet (Java 21 + Gradle)
#
# Ezt a konténert a fejlesztő interaktívan használhatja:
#   docker compose exec builder bash
#   ./gradlew test
#   ./gradlew bootRun
# A Git repó a /workspace mappába van mountolva, így a builder
# és a Jenkins is ugyanazon a kódbázison dolgozik.
# =====================================================================
FROM eclipse-temurin:21-jdk

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Budapest
ENV GRADLE_USER_HOME=/home/dev/.gradle

RUN apt-get update && apt-get install -y --no-install-recommends \
        git \
        curl \
        unzip \
        bash \
        ca-certificates \
        wget \
        vim \
        procps \
        sudo \
    && rm -rf /var/lib/apt/lists/*

# Nem-root fejlesztői felhasználó.
# Az eclipse-temurin image (Ubuntu Noble alapú) tartalmaz egy beépített
# 'ubuntu' usert UID 1000-en; ezt először eltávolítjuk, hogy a megadott
# USER_ID / GROUP_ID szabad legyen, és a 'dev' user tisztán létrejöjjön.
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN if id -u ${USER_ID} >/dev/null 2>&1; then \
        EXISTING_USER=$(getent passwd ${USER_ID} | cut -d: -f1); \
        userdel -r "$EXISTING_USER" 2>/dev/null || true; \
    fi && \
    if getent group ${GROUP_ID} >/dev/null 2>&1; then \
        EXISTING_GROUP=$(getent group ${GROUP_ID} | cut -d: -f1); \
        groupdel "$EXISTING_GROUP" 2>/dev/null || true; \
    fi && \
    groupadd -g ${GROUP_ID} dev && \
    useradd -m -u ${USER_ID} -g ${GROUP_ID} -s /bin/bash dev && \
    mkdir -p /home/dev/.gradle && \
    chown -R dev:dev /home/dev

WORKDIR /workspace

USER dev

CMD ["tail", "-f", "/dev/null"]
