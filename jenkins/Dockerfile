FROM jenkins/jenkins:lts

USER root

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV CASC_JENKINS_CONFIG=/var/jenkins_conf/casc.yaml

RUN apt-get update \
    && apt-get install -y --no-install-recommends ansible curl openssh-client python3 sshpass \
    && rm -rf /var/lib/apt/lists/*

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

RUN mkdir -p /var/jenkins_conf
COPY casc.yaml /var/jenkins_conf/casc.yaml
RUN chown -R jenkins:jenkins /var/jenkins_conf

USER jenkins
