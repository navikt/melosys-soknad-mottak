FROM gcr.io/distroless/java21-debian12:nonroot

ENV TZ="Europe/Oslo"
ENV LANG="nb_NO.UTF-8"

WORKDIR /app

COPY mottak/target/melosys-soknad-mottak.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
