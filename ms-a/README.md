# MS-A - Service de Gestion des Personnes

## 📋 Description

Microservice responsable de la gestion complète des personnes (CRUD) et de l'orchestration du workflow de calcul d'âge via événements Kafka.

## 🎯 Responsabilités

- Création et stockage des personnes
- Publication d'événements `PersonCreatedEvent` lors de la création
- Écoute des événements `AgeEvent` pour mise à jour du statut
- Exposition d'API REST pour la gestion des personnes

## 🏗️ Architecture Technique

### Stack Technologique
- **Spring Boot** 3.5.6
- **Spring Data JPA** pour la persistance
- **Spring Kafka** pour la messagerie
- **H2/PostgreSQL** pour la base de données
- **Lombok** pour réduire le boilerplate

### Structure du Code

```
ms-a/
├── src/main/java/com/example/msa/
│   ├── MsAApplication.java           # Point d'entrée
│   ├── controller/
│   │   └── PersonController.java     # Endpoints REST
│   ├── model/
│   │   ├── Person.java              # Entité JPA
│   │   └── Status.java              # Enum des statuts
│   ├── repository/
│   │   └── PersonRepository.java     # Interface JPA
│   ├── service/
│   │   └── PersonService.java        # Logique métier
│   └── kafka/
│       ├── KafkaProducer.java       # Producteur Kafka
│       └── KafkaConsumer.java       # Consommateur Kafka
└── src/main/resources/
    ├── application.properties         # Config par défaut
    └── application-h2.properties     # Config H2
```

## 🔌 API Endpoints

### POST /persons
Créer une nouvelle personne

**Request:**
```http
POST http://localhost:8081/persons
Content-Type: application/json

{
  "nom": "Martin",
  "prenom": "Sophie",
  "adresse": "456 avenue Test",
  "dateNaissance": "1985-03-20",
  "telephone": "0987654321"
}
```

**Response (201 Created):**
```json
{
  "id": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "nom": "Martin",
  "prenom": "Sophie",
  "adresse": "456 avenue Test",
  "dateNaissance": "1985-03-20",
  "telephone": "0987654321",
  "statut": "EN_ATTENTE",
  "age": null
}
```

### GET /persons/{id}
Récupérer une personne par son ID

**Request:**
```http
GET http://localhost:8081/persons/d87fd4e2-9524-4513-b43f-9e35f2fb54e2
```

**Response (200 OK):**
```json
{
  "id": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "nom": "Martin",
  "prenom": "Sophie",
  "adresse": "456 avenue Test",
  "dateNaissance": "1985-03-20",
  "telephone": "0987654321",
  "statut": "TERMINE",
  "age": 40
}
```

**Response (404 Not Found):**
```json
{
  "error": "Person not found"
}
```

## 📨 Événements Kafka

### Événements Produits

**PersonCreatedEvent** (Topic: `person-created-topic`)
```json
{
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "dateDeNaissance": "1985-03-20"
}
```

### Événements Consommés

**AgeEvent** (Topic: `age-calculated-topic`)
```json
{
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "age": 40,
  "success": true
}
```

## 💾 Base de Données

### Schéma de la Table `persons`

| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Identifiant unique |
| nom | VARCHAR(255) | NOT NULL | Nom de famille |
| prenom | VARCHAR(255) | NOT NULL | Prénom |
| adresse | VARCHAR(255) | | Adresse postale |
| date_naissance | DATE | NOT NULL | Date de naissance |
| telephone | VARCHAR(255) | | Numéro de téléphone |
| statut | VARCHAR(20) | NOT NULL | EN_ATTENTE, TERMINE, ECHEC |
| age | INTEGER | | Âge calculé par MS-B |

## ⚙️ Configuration

### application.properties
```properties
spring.application.name=ms-a
server.port=8081

# Database PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/persons_db
spring.datasource.username=postgres
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=ms-a-group
```

### application-h2.properties
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
```

## 🚀 Démarrage

### Avec H2 (Recommandé pour le développement)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Avec PostgreSQL
```bash
mvn spring-boot:run
```

## 🧪 Tests

### Test de création avec cURL
```bash
curl -X POST http://localhost:8081/persons \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "User",
    "adresse": "123 Test Street",
    "dateNaissance": "2000-01-01",
    "telephone": "0600000000"
  }'
```

### Test avec Postman

1. Créez une nouvelle requête POST
2. URL: `http://localhost:8081/persons`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "adresse": "10 rue de la République",
  "dateNaissance": "1988-06-15",
  "telephone": "0612345678"
}
```

## 📊 Monitoring

### Console H2
- **URL** : http://localhost:8081/h2-console
- **JDBC URL** : `jdbc:h2:mem:testdb`
  - ⚠️ **IMPORTANT** : Remplacez l'URL par défaut (`jdbc:h2:~/test`) par `jdbc:h2:mem:testdb`
- **Username** : `sa`
- **Password** : (laisser le champ vide)

### Logs
Les logs sont configurés en niveau DEBUG pour le package `com.example.msa`

## 🔄 Workflow Complet

1. **Réception requête POST** → PersonController
2. **Sauvegarde en base** → PersonService → PersonRepository
3. **Publication événement** → KafkaProducer → `person-created-topic`
4. **MS-B traite l'événement** et publie sur `age-calculated-topic`
5. **Réception AgeEvent** → KafkaConsumer
6. **Mise à jour du statut** → PersonService → TERMINE/ECHEC

## 🐛 Dépannage

### Le statut reste EN_ATTENTE
- Vérifiez que MS-B est démarré
- Vérifiez la connexion Kafka
- Consultez les logs pour les erreurs

### Erreur de connexion Kafka
- Vérifiez que Kafka est démarré : `docker ps`
- Vérifiez l'entrée hosts : `127.0.0.1 kafka`

## 📚 Dépendances Principales

- `spring-boot-starter-web` : API REST
- `spring-boot-starter-data-jpa` : Persistance
- `spring-kafka` : Messaging
- `postgresql` : Driver PostgreSQL
- `h2` : Base de données en mémoire
- `lombok` : Réduction du boilerplate