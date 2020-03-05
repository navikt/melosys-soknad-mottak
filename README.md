# melosys-soknad-mottak
Mottaksapplikasjon for søknader om trygdetilhørighet

## Oppsett

For å kjøre opp applikasjonen lokalt, eller kjøre integrasjonstester må det startes opp en lokal postgres-database.
Dette kan gjøres med kommandoen:
```
docker run -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=su -d --rm postgres
```
