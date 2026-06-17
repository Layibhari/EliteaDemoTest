FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y curl git wget

ARG MAVEN_VERSION=3.9.9

RUN wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

COPY settings.xml /root/.m2/settings.xml

WORKDIR /workspace

# Add this line to keep the container running
ENTRYPOINT ["tail", "-f", "/dev/null"]