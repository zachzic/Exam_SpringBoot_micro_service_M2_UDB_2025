# Architecture de l'Application - Microservices & Kafka

## 📐 Architecture Événementielle - Flux Complet

### Étape 1 : Création d'une personne (MS-A)

**Fichier** : `ms-a/src/main/java/com/example/msa/controller/PersonController.java`

Le client envoie une requête POST vers MS-A :
```bash
POST http://localhost:8081/persons
{
  "nom": "Durand",
  "prenom": "Marie",
  "dateNaissance": "1990-05-15",
  "adresse": "123 rue de la Paix",
  "telephone": "0612345678"
}
```

**Fichier** : `ms-a/src/main/java/com/example/msa/service/PersonService.java`

1. MS-A sauvegarde la personne dans `persons_db` avec `statut = EN_ATTENTE`
2. MS-A publie un `PersonCreatedEvent` sur le topic Kafka `person-created-topic`

**DTO** : `common-dtos/src/main/java/com/example/dtos/PersonCreatedEvent.java`
```java
{
  "personId": "550e8400-e29b-41d4-a716-446655440000",
  "dateDeNaissance": "1990-05-15"
}
```

---

### Étape 2 : MS-B calcule l'âge

**Fichier** : `ms-b/src/main/java/com/example/msb/kafka/KafkaConsumer.java`

MS-B écoute le topic `person-created-topic` et reçoit l'événement.

**Fichier** : `ms-b/src/main/java/com/example/msb/service/AgeCalculatorService.java`

1. **Calcule l'âge** (ligne 27) :
   ```java
   int age = Period.between(dateNaissance, LocalDate.now()).getYears();
   ```

2. **Sauvegarde dans sa propre base** `ages_db` (lignes 29-35) :
   ```java
   PersonAge personAge = new PersonAge();
   personAge.setPersonId(event.getPersonId());
   personAge.setAge(age);
   personAge.setCalculatedAt(LocalDate.now());
   personAgeRepository.save(personAge);
   ```

3. **Publie un AgeEvent vers Kafka** (ligne 38-40) :
   - Topic : `age-calculated-topic`
   - Contenu : `AgeEvent(personId, age, "TERMINE")`

4. **En cas d'erreur** (ligne 42-46) :
   - Publie un `AgeEvent(personId, null, "ECHEC")`

**DTO** : `common-dtos/src/main/java/com/example/dtos/AgeEvent.java`
```java
{
  "personId": "550e8400-e29b-41d4-a716-446655440000",
  "age": 35,
  "status": "TERMINE"
}
```

---

### Étape 3 : MS-A met à jour le statut

**Fichier** : `ms-a/src/main/java/com/example/msa/kafka/KafkaConsumer.java`

1. **Écoute le topic** `age-calculated-topic`

2. **Récupère la personne** depuis `persons_db` (ligne 22)

3. **Met à jour l'âge et le statut** (lignes 23-25) :
   ```java
   person.setAge(event.getAge());
   person.setStatut("TERMINE".equals(event.getStatus()) ? Status.TERMINE : Status.ECHEC);
   personRepository.save(person);
   ```

---

## 🔄 Flux Complet Illustré

```
CLIENT
  │
  │ POST /persons {"nom": "Durand", "prenom": "Marie", "dateNaissance": "1990-05-15"}
  ↓
┌─────────────────────────────────────────┐
│          MS-A (Port 8081)               │
│  PersonController → PersonService       │
│    ├─ Save Person (statut: EN_ATTENTE) │
│    └─ Publish PersonCreatedEvent        │
└─────────────────┬───────────────────────┘
                  │
                  ↓ PersonCreatedEvent
            ┌──────────┐
            │  KAFKA   │  Topic: person-created-topic
            └──────────┘
                  │
                  ↓ PersonCreatedEvent
┌─────────────────────────────────────────┐
│          MS-B (Port 8082)               │
│  KafkaConsumer → AgeCalculatorService   │
│    ├─ Calculate Age (35 ans)            │
│    ├─ Save PersonAge in ages_db         │
│    └─ Publish AgeEvent(35, "TERMINE")   │
└─────────────────┬───────────────────────┘
                  │
                  ↓ AgeEvent
            ┌──────────┐
            │  KAFKA   │  Topic: age-calculated-topic
            └──────────┘
                  │
                  ↓ AgeEvent
┌─────────────────────────────────────────┐
│          MS-A (Port 8081)               │
│  KafkaConsumer                          │
│    ├─ Find Person by ID                 │
│    ├─ Update age = 35                   │
│    └─ Update statut = TERMINE           │
└─────────────────────────────────────────┘
```

---

## 💾 Bases de données séparées

### persons_db (MS-A)
- **Table** : `persons`
- **Colonnes** :
  - `id` (UUID)
  - `nom` (String)
  - `prenom` (String)
  - `dateNaissance` (LocalDate)
  - `adresse` (String)
  - `telephone` (String)
  - `statut` (ENUM: EN_ATTENTE, TERMINE, ECHEC)
  - `age` (Integer, nullable)

### ages_db (MS-B)
- **Table** : `person_ages`
- **Colonnes** :
  - `personId` (UUID, PK)
  - `age` (Integer)
  - `dateNaissance` (LocalDate)
  - `calculatedAt` (LocalDate)

---

## 🎯 Points clés de l'architecture

### 1. **Communication asynchrone**
Les services ne s'appellent jamais directement. Toute communication passe par Kafka.

### 2. **Database per service**
Chaque microservice a sa propre base de données indépendante :
- MS-A gère `persons_db`
- MS-B gère `ages_db`

### 3. **Event-driven**
Tout le workflow repose sur des événements Kafka :
- `PersonCreatedEvent` : publié par MS-A, consommé par MS-B
- `AgeEvent` : publié par MS-B, consommé par MS-A

### 4. **Résilience**
Si MS-B échoue lors du calcul, il publie un `AgeEvent` avec :
```java
{
  "personId": "...",
  "age": null,
  "status": "ECHEC"
}
```
MS-A mettra alors le statut de la personne à `ECHEC`.

### 5. **Traçabilité**
Tous les événements sont loggés dans les deux services :
```
[MS-A] Sent PersonCreatedEvent for person: 550e8400-...
[MS-B] Received PersonCreatedEvent for person: 550e8400-...
[MS-B] Sent AgeEvent for person: 550e8400-...
[MS-A] Received AgeEvent for person: 550e8400-... with age: 35
```

---

## 📊 Topics Kafka

| Topic | Producteur | Consommateur | Message |
|-------|-----------|--------------|---------|
| `person-created-topic` | MS-A | MS-B | `PersonCreatedEvent` |
| `age-calculated-topic` | MS-B | MS-A | `AgeEvent` |

---

## 🔍 Statuts possibles d'une personne

| Statut | Description |
|--------|-------------|
| `EN_ATTENTE` | Personne créée, en attente du calcul d'âge |
| `TERMINE` | Âge calculé avec succès |
| `ECHEC` | Erreur lors du calcul d'âge |

---

## 🛠️ Technologies utilisées

- **Spring Boot** : Framework microservices
- **Spring Data JPA** : Persistance avec Hibernate
- **MySQL 8.0** : Base de données relationnelle
- **Apache Kafka** : Bus d'événements
- **Lombok** : Réduction du boilerplate Java
- **Maven** : Gestion de dépendances

---

## 📝 Exemple de cycle complet

### 1. Création
```bash
POST http://localhost:8081/persons
{
  "nom": "Durand",
  "prenom": "Marie",
  "dateNaissance": "1990-05-15"
}
```

**Réponse** :
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nom": "Durand",
  "prenom": "Marie",
  "dateNaissance": "1990-05-15",
  "statut": "EN_ATTENTE",
  "age": null
}
```

### 2. Vérification (après quelques secondes)
```bash
GET http://localhost:8081/persons/550e8400-e29b-41d4-a716-446655440000
```

**Réponse** :
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nom": "Durand",
  "prenom": "Marie",
  "dateNaissance": "1990-05-15",
  "statut": "TERMINE",
  "age": 35
}
```

### 3. Consulter l'âge dans MS-B
```bash
GET http://localhost:8082/ages/550e8400-e29b-41d4-a716-446655440000
```

**Réponse** :
```json
{
  "personId": "550e8400-e29b-41d4-a716-446655440000",
  "age": 35,
  "dateNaissance": "1990-05-15",
  "calculatedAt": "2025-10-02"
}
```

---

C'est une architecture microservices classique avec bus d'événements !
