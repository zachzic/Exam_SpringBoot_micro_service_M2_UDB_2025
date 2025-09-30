# MS-B - Service de Calcul d'Âge

## 📋 Description

Microservice dédié au calcul d'âge des personnes. Il écoute les événements de création de personnes, calcule leur âge, stocke le résultat et publie un événement de confirmation.

## 🎯 Responsabilités

- Écoute des événements `PersonCreatedEvent` depuis Kafka
- Calcul de l'âge basé sur la date de naissance
- Stockage de la relation PersonId → Age
- Publication d'événements `AgeEvent` avec le résultat
- Exposition d'API REST pour consulter les âges calculés

## 🏗️ Architecture Technique

### Stack Technologique
- **Spring Boot** 3.5.6
- **Spring Data JPA** pour la persistance
- **Spring Kafka** pour la messagerie événementielle
- **H2/PostgreSQL** pour la base de données
- **Lombok** pour réduire le boilerplate

### Structure du Code

```
ms-b/
├── src/main/java/com/example/msb/
│   ├── MsBApplication.java              # Point d'entrée
│   ├── controller/
│   │   └── AgeController.java          # Endpoints REST
│   ├── model/
│   │   └── PersonAge.java              # Entité JPA
│   ├── repository/
│   │   └── PersonAgeRepository.java    # Interface JPA
│   ├── service/
│   │   └── AgeCalculatorService.java   # Logique de calcul
│   └── kafka/
│       ├── KafkaConsumer.java          # Consommateur Kafka
│       └── KafkaProducer.java          # Producteur Kafka
└── src/main/resources/
    ├── application.properties           # Config par défaut
    └── application-h2.properties       # Config H2
```

## 🔌 API Endpoints

### GET /ages/{personId}
Récupérer l'âge calculé d'une personne

**Request:**
```http
GET http://localhost:8082/ages/d87fd4e2-9524-4513-b43f-9e35f2fb54e2
```

**Response (200 OK):**
```json
{
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "age": 40,
  "calculatedAt": "2025-09-30T21:01:45.123Z"
}
```

**Response (404 Not Found):**
```json
{
  "error": "Age not found for person",
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2"
}
```

## 📨 Événements Kafka

### Événements Consommés

**PersonCreatedEvent** (Topic: `person-created-topic`)
```json
{
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "dateDeNaissance": "1985-03-20"
}
```

### Événements Produits

**AgeEvent** (Topic: `age-calculated-topic`)
```json
{
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "age": 40,
  "success": true
}
```

En cas d'erreur :
```json
{
  "personId": "d87fd4e2-9524-4513-b43f-9e35f2fb54e2",
  "age": null,
  "success": false
}
```

## 💾 Base de Données

### Schéma de la Table `person_ages`

| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Identifiant unique |
| person_id | UUID | UNIQUE, NOT NULL | ID de la personne |
| age | INTEGER | NOT NULL | Âge calculé |
| calculated_at | TIMESTAMP | NOT NULL | Date/heure du calcul |

## 🧮 Logique de Calcul d'Âge

Le calcul d'âge prend en compte :
- L'année actuelle vs l'année de naissance
- Le mois et jour pour ajuster si l'anniversaire n'est pas encore passé
- Utilise `LocalDate.now()` pour obtenir la date actuelle
- Retourne l'âge en années complètes

### Exemple de calcul
```java
public int calculateAge(LocalDate birthDate) {
    LocalDate currentDate = LocalDate.now();
    return Period.between(birthDate, currentDate).getYears();
}
```

## ⚙️ Configuration

### application.properties
```properties
spring.application.name=ms-b
server.port=8082

# Database PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/ages_db
spring.datasource.username=postgres
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=ms-b-group
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

### Test de consultation d'âge avec cURL
```bash
# Remplacez l'UUID par un ID valide
curl http://localhost:8082/ages/d87fd4e2-9524-4513-b43f-9e35f2fb54e2
```

### Test avec Postman

1. Créez d'abord une personne via MS-A
2. Attendez quelques secondes pour le traitement
3. Faites une requête GET :
   - URL: `http://localhost:8082/ages/{personId}`
   - Method: GET

## 📊 Monitoring

### Console H2
- **URL** : http://localhost:8082/h2-console
- **JDBC URL** : `jdbc:h2:mem:testdb`
  - ⚠️ **IMPORTANT** : Remplacez l'URL par défaut (`jdbc:h2:~/test`) par `jdbc:h2:mem:testdb`
- **Username** : `sa`
- **Password** : (laisser le champ vide)

### Requêtes SQL utiles
```sql
-- Voir tous les âges calculés
SELECT * FROM person_ages;

-- Statistiques
SELECT
    COUNT(*) as total,
    AVG(age) as age_moyen,
    MIN(age) as age_min,
    MAX(age) as age_max
FROM person_ages;
```

### Logs
Les logs sont configurés en niveau DEBUG pour le package `com.example.msb`

## 🔄 Workflow de Traitement

1. **Réception PersonCreatedEvent** → KafkaConsumer
2. **Extraction date de naissance** → Parsing de la date
3. **Calcul de l'âge** → AgeCalculatorService
4. **Sauvegarde en base** → PersonAgeRepository
5. **Publication AgeEvent** → KafkaProducer
6. **MS-A met à jour le statut** → TERMINE ou ECHEC

## 🚨 Gestion des Erreurs

### Cas d'erreur gérés
- Date de naissance invalide → AgeEvent avec `success: false`
- Date de naissance future → AgeEvent avec `success: false`
- Erreur de base de données → Retry automatique
- Kafka indisponible → Messages en attente

### Comportement en cas d'erreur
```java
   try {
       int age = calculateAge(birthDate);
       // Sauvegarde et publication succès
   } catch (Exception e) {
       // Log de l'erreur
       // Publication AgeEvent avec success = false
   }
```

## 🐛 Dépannage

### L'âge n'est pas calculé
- Vérifiez que MS-B reçoit les événements
- Consultez les logs pour les erreurs
- Vérifiez la connexion Kafka

### Erreur de connexion Kafka
```bash
# Vérifier que Kafka est démarré
docker ps | grep kafka

# Vérifier les logs du conteneur
docker logs kafka
```

### Base de données vide
- Vérifiez que des personnes ont été créées dans MS-A
- Attendez quelques secondes pour le traitement asynchrone

## 🔍 Métriques

Le service expose automatiquement des métriques :
- Nombre d'événements traités
- Temps de calcul moyen
- Taux de succès/échec
- Latence Kafka

## 📚 Dépendances Principales

- `spring-boot-starter-web` : API REST
- `spring-boot-starter-data-jpa` : Persistance
- `spring-kafka` : Messaging événementiel
- `postgresql` : Driver PostgreSQL
- `h2` : Base de données en mémoire
- `lombok` : Réduction du boilerplate
- `common-dtos` : DTOs partagés (module interne)