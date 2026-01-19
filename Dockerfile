FROM eclipse-temurin:21

# Fang opp nåværende bruker-ID og gruppe-ID (apprunner?)
RUN echo "$(id -u):$(id -g)" > /tmp/original_user

# Bytt til root for å oppdatere og fjerne pakker
USER root

# Fjern avhengigheter som ikke er nødvendige for å kjøre mottak, og som skaper CVEs.
# Oppdater openssl og dpkg til nyeste versjoner for å fikse CVE-ene
RUN apt-get update && \
    apt-get upgrade -y openssl dpkg && \
    apt-get remove -y wget && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# Bytt tilbake til den opprinnelige brukeren ved hjelp av fanget ID
RUN USER_INFO=$(cat /tmp/original_user) && chown $USER_INFO /tmp/original_user

COPY mottak/target/melosys-soknad-mottak.jar app.jar

COPY scripts/init-scripts/ /init-scripts
COPY scripts/entrypoint.sh /entrypoint.sh
COPY scripts/run-java.sh /run-java.sh
RUN chmod +x /entrypoint.sh /run-java.sh

ENV APP_JAR=/app.jar
USER $(cat /tmp/original_user | cut -d: -f1)
ENTRYPOINT ["/entrypoint.sh"]
