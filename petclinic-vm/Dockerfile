FROM ubuntu:22.04


RUN apt-get update && \
    apt-get install -y openssh-server sudo openjdk-21-jre-headless curl && \
    rm -rf /var/lib/apt/lists/*


RUN useradd -m -s /bin/bash deploy && \
    echo "deploy:deploy" | chpasswd && \
    echo "deploy ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/deploy


RUN mkdir /var/run/sshd && \
    mkdir -p /home/deploy/.ssh && \
    chown deploy:deploy /home/deploy/.ssh && \
    chmod 700 /home/deploy/.ssh


RUN sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config && \
    sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin no/' /etc/ssh/sshd_config

EXPOSE 22 8081

CMD ["/usr/sbin/sshd", "-D"]
