# MS-B (Age API)

Microservice de calcul et consultation des ages.

## Role

- Consomme `PersonCreatedEvent` depuis Kafka
- Calcule l'age a partir de `dateDeNaissance`
- Stocke le resultat dans `ages_db` (`person_ages`)
- Publie `AgeEvent` vers Kafka
- Expose l'API REST `/ages`

## Port et dependances

- Port HTTP: `8082`
- Base MySQL: `ages_db`
- Kafka bootstrap server: `localhost:9092`

## Lancement

Depuis la racine du repo:

```powershell
.\mvnw -f .\ms-b\pom.xml spring-boot:run
```

## Configuration DB

Fichier: `ms-b/src/main/resources/application.properties`

Selon ton Docker mapping:

- `spring.datasource.url=jdbc:mysql://localhost:3306/ages_db`
- ou `spring.datasource.url=jdbc:mysql://localhost:3307/ages_db`

## Swagger

- UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`

## Endpoint

### `GET /ages/{personId}`

Retourne les informations d'age calculees pour une personne.

Exemple response `200`:

```json
{
  "personId": "550e8400-e29b-41d4-a716-446655440000",
  "dateNaissance": "1990-05-15",
  "age": 35,
  "calculatedAt": "2026-02-16"
}
```

Exemple response `404`:

```json
{
  "timestamp": "2026-02-16T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Age introuvable",
  "path": "/ages/550e8400-e29b-41d4-a716-446655440001"
}
```

## Auth

Swagger affiche un schema `Bearer JWT` dans `Authorize`.
Actuellement, le service ne bloque pas encore les endpoints avec Spring Security.
