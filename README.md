# melosys-soknad-mottak
Mottaksapplikasjon for søknader om trygdetilhørighet av utsendte arbeidstakere.

- Leser fra en kø hos Altinn
- Lagrer XML-søknad og vedlegg
- Bestiller PDF som representerer søknaden
- Sender en Kafka-melding når søknad er klar

## Oppsett

For å kjøre opp applikasjonen lokalt, eller kjøre integrasjonstester må det startes opp en lokal postgres-database.
Dette kan gjøres med kommandoen:
```
docker run -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=su -d --rm postgres
```
