-- Créer les bases de données
CREATE DATABASE persons_db;
CREATE DATABASE ages_db;

-- Donner tous les privilèges à l'utilisateur postgres
GRANT ALL PRIVILEGES ON DATABASE persons_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ages_db TO postgres;