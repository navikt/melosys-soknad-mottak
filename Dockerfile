FROM ghcr.io/navikt/baseimages/temurin:17

# Fjern avhengigheter som ikke er nødvendige for å kjøre mottak, og som skaper CVEs.
RUN apt-get update && apt-get remove -y wget && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

COPY mottak/target/melosys-soknad-mottak.jar app.jar
