# Gestion des Personnes & Calcul d'âge - Architecture Microservices

## 🏗️ Architecture

Architecture de microservices événementielle pour la gestion de personnes avec calcul automatique d'âge via Apache Kafka.

```
┌─────────────┐     PersonCreatedEvent     ┌─────────────┐
│    MS-A     │ ─────────────────────────> │    MS-B     │
│  (Port 8081)│                             │  (Port 8082)│
│   Persons   │ <───────────────────────── │     Age     │
└─────────────┘         AgeEvent           └─────────────┘
      │                                            │
      └────────────────┬───────────────────────────┘
                       │
                  Apache Kafka
                  (Port 9092)
```

### Services
- **MS-A** : Service de gestion des personnes (Port 8081)
- **MS-B** : Service de calcul d'âge (Port 8082)
- **Kafka** : Bus d'événements asynchrone (Port 9092)
- **Kafka UI** : Interface de monitoring Kafka (Port 8090)
- **H2 Database** : Base de données en mémoire pour chaque service

## 🚀 Quick Start

### Prérequis
- Java 17+
- Maven 3.8+
- Docker Desktop
- Windows : PowerShell en mode administrateur

### 1. Démarrer l'infrastructure Docker

```bash
cd gestion-personnes-age
docker-compose up -d
```

### 2. Configuration du système (Windows)

Ajouter l'entrée hosts pour Kafka (PowerShell Admin) :
```powershell
Add-Content -Path C:\Windows\System32\drivers\etc\hosts -Value "127.0.0.1 kafka"
```

### 3. Compiler le projet

```bash
mvn clean install
```

### 4. Démarrer les microservices

Terminal 1 - MS-A :
```bash
cd ms-a
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Terminal 2 - MS-B :
```bash
cd ms-b
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## 📊 Workflow Événementiel

1. **Création d'une personne** dans MS-A
2. MS-A publie `PersonCreatedEvent` sur le topic `person-created-topic`
3. MS-B consomme l'événement et calcule l'âge
4. MS-B publie `AgeEvent` sur le topic `age-calculated-topic`
5. MS-A consomme l'événement et met à jour le statut

### Statuts possibles
- `EN_ATTENTE` : Statut initial après création
- `TERMINE` : Calcul d'âge réussi
- `ECHEC` : Erreur lors du calcul

## 🔌 Endpoints REST

### MS-A - Gestion des Personnes (http://localhost:8081)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/persons` | Créer une nouvelle personne |
| GET | `/persons/{id}` | Récupérer une personne par ID |

### MS-B - Service d'Âge (http://localhost:8082)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/ages/{personId}` | Récupérer l'âge calculé d'une personne |

## 📝 Exemples d'utilisation

### Créer une personne

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

**Réponse** :
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

### Vérifier le statut (après quelques secondes)

```bash
curl http://localhost:8081/persons/550e8400-e29b-41d4-a716-446655440000
```

**Réponse après traitement** :
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

### Consulter l'âge dans MS-B

```bash
curl http://localhost:8082/ages/550e8400-e29b-41d4-a716-446655440000
```

## 🔍 Monitoring

### Kafka UI
Accédez à http://localhost:8090 pour visualiser :
- Topics créés (`person-created-topic`, `age-calculated-topic`)
- Messages échangés
- Consumer groups
- Lag et performances

### Console H2
- MS-A : http://localhost:8081/h2-console
- MS-B : http://localhost:8082/h2-console
- **JDBC URL** : `jdbc:h2:mem:testdb` ⚠️ **IMPORTANT : Ne pas utiliser l'URL par défaut !**
- **Username** : `sa`
- **Password** : (laisser vide)

## 🛠️ Configuration

### Profils Spring
- `default` : Utilise PostgreSQL
- `h2` : Utilise H2 en mémoire (recommandé pour le développement)

### Variables d'environnement Docker
Les configurations sont dans `docker-compose.yml`

## 📦 Structure du Projet

```
gestion-personnes-age/
├── common-dtos/          # DTOs partagés entre services
│   ├── PersonCreatedEvent.java
│   └── AgeEvent.java
├── ms-a/                 # Microservice A - Gestion Personnes
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── kafka/
├── ms-b/                 # Microservice B - Calcul d'âge
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── kafka/
├── docker-compose.yml    # Infrastructure
└── pom.xml              # Parent POM
```

## 🧪 Tests avec Postman

1. Importez la collection Postman (si disponible)
2. Configurez l'environnement avec :
   - `base_url_ms_a`: `http://localhost:8081`
   - `base_url_ms_b`: `http://localhost:8082`

## 🐛 Troubleshooting

### Erreur de connexion Kafka
- Vérifiez que l'entrée hosts est bien ajoutée
- Redémarrez les services après l'ajout

### Ports déjà utilisés
```bash
# Windows - Trouver le processus
netstat -ano | findstr :8081

# Tuer le processus
taskkill /F /PID <PID>
```

### Problèmes d'encodage (Windows)
Utilisez le profil H2 au lieu de PostgreSQL

## 📄 Licence

Projet d'exemple à des fins éducatives.