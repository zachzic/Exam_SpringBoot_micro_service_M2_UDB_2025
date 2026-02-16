# MS-A (Persons API)

Microservice de gestion des personnes.

## Role

- Expose l'API REST `/persons`
- Sauvegarde les personnes dans `persons_db`
- Publie `PersonCreatedEvent` vers Kafka
- Consomme `AgeEvent` pour mettre a jour `age` et `statut`

## Port et dependances

- Port HTTP: `8081`
- Base MySQL: `persons_db`
- Kafka bootstrap server: `localhost:9092`

## Lancement

Depuis la racine du repo:

```powershell
.\mvnw -f .\ms-a\pom.xml spring-boot:run
```

## Configuration DB

Fichier: `ms-a/src/main/resources/application.properties`

Selon ton Docker mapping:
- `spring.datasource.url=jdbc:mysql://localhost:3306/persons_db`
- ou `spring.datasource.url=jdbc:mysql://localhost:3307/persons_db`

## Swagger

- UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Endpoints

### `POST /persons`

Cree une personne et initialise le statut a `EN_ATTENTE`.

Exemple request:

```json
{
  "nom": "Durand",
  "prenom": "Marie",
  "adresse": "123 rue de la Paix",
  "dateNaissance": "1990-05-15",
  "telephone": "0612345678"
}
```

Exemple response `201`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nom": "Durand",
  "prenom": "Marie",
  "adresse": "123 rue de la Paix",
  "dateNaissance": "1990-05-15",
  "telephone": "0612345678",
  "statut": "EN_ATTENTE",
  "age": null
}
```

### `GET /persons/{id}`

Retourne la personne avec son statut courant.

Exemple response `200`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nom": "Durand",
  "prenom": "Marie",
  "adresse": "123 rue de la Paix",
  "dateNaissance": "1990-05-15",
  "telephone": "0612345678",
  "statut": "TERMINE",
  "age": 35
}
```

Exemple response `404`:

```json
{
  "timestamp": "2026-02-16T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Personne introuvable",
  "path": "/persons/550e8400-e29b-41d4-a716-446655440001"
}
```

## Auth

Swagger affiche un schema `Bearer JWT` dans `Authorize`.
Actuellement, le service ne bloque pas encore les endpoints avec Spring Security.
