FROM eclipse-temurin:21-jre

USER root

ARG BURP_DOWNLOAD_URL="https://portswigger.net/burp/releases/download?product=community&type=Jar"

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl fluxbox x11vnc xvfb \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /opt/burp /data \
    && curl -L "$BURP_DOWNLOAD_URL" -o /opt/burp/burpsuite_community.jar

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 5900
VOLUME ["/data"]

ENTRYPOINT ["/entrypoint.sh"]
