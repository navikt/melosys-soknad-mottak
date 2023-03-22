FROM ghcr.io/navikt/baseimages/temurin:17
COPY mottak/target/melosys-soknad-mottak.jar app.jar
