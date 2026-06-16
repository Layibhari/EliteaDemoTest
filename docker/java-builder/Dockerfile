FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y \
    git \
    curl \
    wget \
    unzip && \
    rm -rf /var/lib/apt/lists/*

ARG MAVEN_VERSION=3.9.9

RUN wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

RUN java -version
RUN mvn -version

WORKDIR /workspace