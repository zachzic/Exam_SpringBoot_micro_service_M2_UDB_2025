# Gestion Personnes & Age

Architecture microservices evenementielle avec Kafka:
- `ms-a` (port `8081`): creation/consultation des personnes
- `ms-b` (port `8082`): calcul et consultation des ages
- `common-dtos`: DTO partages entre les services

Le flux metier est:
1. `POST /persons` sur `ms-a`
2. publication `PersonCreatedEvent` sur Kafka
3. consommation par `ms-b` et calcul de l'age
4. publication `AgeEvent`
5. consommation par `ms-a` et mise a jour du statut (`EN_ATTENTE`, `TERMINE`, `ECHEC`)

## Prerequis

- Java 17
- Maven (ou `mvnw`)
- Docker Desktop

## Demarrage rapide

1. Aller a la racine:

```powershell
cd "C:\Users\USER\Documents\gestion-personnes&age\gestion-personnes-age"
```

2. Lancer l'infra:

```powershell
docker compose up -d
docker compose ps
```

3. Adapter les URLs MySQL selon ton mapping Docker:

- Si `docker compose ps` affiche `3306->3306`, utiliser:
  - `jdbc:mysql://localhost:3306/persons_db` (ms-a)
  - `jdbc:mysql://localhost:3306/ages_db` (ms-b)
- Si `docker compose ps` affiche `3307->3306`, utiliser:
  - `jdbc:mysql://localhost:3307/persons_db` (ms-a)
  - `jdbc:mysql://localhost:3307/ages_db` (ms-b)

Fichiers a verifier:
- `ms-a/src/main/resources/application.properties`
- `ms-b/src/main/resources/application.properties`

4. Compiler:

```powershell
.\mvnw clean install -DskipTests
```

5. Lancer les services (2 terminaux):

Terminal 1:
```powershell
.\mvnw -f .\ms-a\pom.xml spring-boot:run
```

Terminal 2:
```powershell
.\mvnw -f .\ms-b\pom.xml spring-boot:run
```

Note: utiliser `-f <module>/pom.xml` evite l'erreur "Unable to find a suitable main class" sur le parent.

## Swagger / OpenAPI

- MS-A Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- MS-B Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- MS-A OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- MS-B OpenAPI JSON: `http://localhost:8082/v3/api-docs`

La section `Authorize` (Bearer JWT) est documentee dans Swagger. L'auth n'est pas encore enforcee par Spring Security dans le code actuel.

## Endpoints

### MS-A (`http://localhost:8081`)

- `POST /persons`
- `GET /persons/{id}`

Exemple creation:

```bash
curl -X POST http://localhost:8081/persons \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Durand",
    "prenom": "Marie",
    "adresse": "123 rue de la Paix",
    "dateNaissance": "1990-05-15",
    "telephone": "0612345678"
  }'
```

### MS-B (`http://localhost:8082`)

- `GET /ages/{personId}`

## Outils utiles

- Kafka UI: `http://localhost:8090`
- Adminer: `http://localhost:8080`
  - Server: `mysql`
  - User: `root`
  - Password: `root`

## Troubleshooting

### Port 3306 deja utilise

Erreur type:
`bind: Only one usage of each socket address is normally permitted`

Solutions:
1. Arreter MySQL local (ex: `mysqld.exe`) puis relancer Docker
2. Garder MySQL local, mapper Docker en `3307:3306`, puis mettre `localhost:3307` dans les `application.properties`

### Warning docker compose sur `version`

Message:
`the attribute 'version' is obsolete`

Ce n'est pas bloquant. Tu peux supprimer la ligne `version: '3.8'` de `docker-compose.yml`.

## Arret

```powershell
docker compose down
```

Arret complet + suppression volumes:

```powershell
docker compose down -v
```
