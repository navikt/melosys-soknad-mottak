FROM eclipse-temurin:21-jre

RUN apt-get update && \
    apt-get install -y --no-install-recommends dumb-init locales && \
    apt-get upgrade -y openssl dpkg && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

RUN sed -i -e 's/# nb_NO.UTF-8 UTF-8/nb_NO.UTF-8 UTF-8/' /etc/locale.gen && locale-gen
ENV LC_ALL="nb_NO.UTF-8"
ENV LANG="nb_NO.UTF-8"
ENV TZ="Europe/Oslo"
ENV APP_JAR=app.jar

RUN groupadd -r --gid 1069 apprunner && useradd -r --uid 1069 -g apprunner apprunner

WORKDIR /app

COPY --chown=apprunner:root docker/init-scripts/ /init-scripts/
COPY --chown=apprunner:root docker/entrypoint.sh /entrypoint.sh
COPY --chown=apprunner:root docker/run-java.sh /run-java.sh
RUN chmod +x /entrypoint.sh /run-java.sh /init-scripts/*.sh

RUN chown -R apprunner /app
USER apprunner

EXPOSE 8080

COPY mottak/target/melosys-soknad-mottak.jar app.jar

ENTRYPOINT ["/usr/bin/dumb-init", "--", "/entrypoint.sh"]
