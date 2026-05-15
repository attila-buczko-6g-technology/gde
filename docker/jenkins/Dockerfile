# =====================================================================
# Jenkins LTS Java 21 támogatással + Docker CLI a host socket eléréshez
# =====================================================================
FROM jenkins/jenkins:lts-jdk21

USER root

# Setup wizard kikapcsolása – konfigurációt Configuration-as-Code-ból veszünk
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc/jenkins.yaml


# Docker CLI telepítése – a Jenkins a host docker.sock-on keresztül indít konténereket.
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
    && chmod a+r /etc/apt/keyrings/docker.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/debian $(lsb_release -cs) stable" \
    > /etc/apt/sources.list.d/docker.list \
    && apt-get update && apt-get install -y --no-install-recommends docker-ce-cli \
    && rm -rf /var/lib/apt/lists/*

# Előtelepített pluginek
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY casc/ /var/jenkins_home/casc/
RUN chown -R jenkins:jenkins /var/jenkins_home/casc

USER jenkins
