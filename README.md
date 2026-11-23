\# Gestion de rendez-vous d'une clinique



Projet d'examen Spring Boot (M1 Génie Logiciel), domaine C : \*\*Clinique (prise de RDV)\*\*.

API REST en \*\*monolithe modulaire\*\*, schéma géré par \*\*Flyway\*\*, documentée avec \*\*Swagger\*\*, conteneurisée avec \*\*Docker\*\*.



Auteur : Mame Aby Diop



\## Lancement en une commande



Prérequis : Docker Desktop (ou Docker + Compose) démarré.



```bash

git clone https://github.com/abibatouJoop-hub/gestion\_rdv\_clinique.git
cd gestion\_rdv\_clinique

docker compose up --build

```



Au premier démarrage, la construction de l'image prend quelques minutes. Les migrations Flyway

sont appliquées automatiquement. Quand le journal affiche `Started CliniqueApiApplication` :



| Ressource | URL |

|---|---|

| Swagger UI | http://localhost:8080/swagger-ui.html |

| Santé de l'application | http://localhost:8080/actuator/health |

| Spécification OpenAPI | http://localhost:8080/v3/api-docs |



Aucune configuration n'est nécessaire. Pour personnaliser (ports, mot de passe), copiez

`.env.example` en `.env` et modifiez-le : `.env` n'est jamais versionné.



Arrêt : `docker compose down` (conserve les données) ou `docker compose down -v` (repart d'une base vide).



\## Données de test



\- Les \*\*6 spécialités\*\* (médecine générale, cardiologie, pédiatrie, dermatologie, gynécologie,

&#x20; ophtalmologie) sont chargées par la migration `V1\_2`.

\- Le fichier \*\*`docs/requetes.http`\*\* déroule tous les scénarios métier : création d'un médecin,

&#x20; de patients et de créneaux, prise de rendez-vous, quota, conflit, liste d'attente, annulation,

&#x20; rappels et notifications. Il s'ouvre dans IntelliJ IDEA ou VS Code (extension REST Client).

\- Il n'y a pas de compte utilisateur : l'API n'est pas authentifiée (voir les limites dans la note

&#x20; d'architecture).



\## Architecture : 4 modules



```

&#x20;                   ┌──────────────┐

&#x20;                   │ transactions │  rendez-vous, liste d'attente, rappels

&#x20;                   └───┬──────┬───┘

&#x20;        appelle l'api  │      │  appelle l'api

&#x20;             ┌─────────▼┐    ┌▼─────────┐

&#x20;             │catalogue │    │ acteurs  │

&#x20;             │médecins, │    │ patients,│

&#x20;             │créneaux  │    │ pénalités│

&#x20;             └──────────┘    └──────────┘



&#x20;transactions ┄┄ événements (RendezVousPris, RendezVousAnnule, RappelRendezVous) ┄┄▶ notifications



&#x20;shared : erreurs, configuration (aucune logique métier)

```



Chaque module est un package racine (`sn.isi.clinique\_api.<module>`) avec deux sous-packages :

`api` (interfaces, DTO, événements : seule partie visible des autres modules) et `internal`

(entités, repositories, services, contrôleurs). Les règles sont vérifiées automatiquement par

`ArchitectureTest` (Spring Modulith). Détails dans `docs/note-architecture.md`.



\## Règles de gestion



| Règle | Valeur |

|---|---|

| Quota | 3 rendez-vous confirmés à venir par patient |

| Patient bloqué | 5 points de pénalité ou plus |

| Annulation à moins de 24 h | +1 point |

| Absence | +2 points |

| Créneau déjà pris | inscription possible en liste d'attente ; au retour du créneau, le premier patient éligible l'obtient |

| Conflit | un patient ne peut pas avoir deux rendez-vous qui se chevauchent ; un médecin non plus pour ses créneaux |

| Rappels | une notification la veille (planificateur horaire) |



Erreurs : un seul format JSON (`code`, `message`, `horodatage`, `details`) avec les codes 400, 404 et 409.

## Authentification et rôles

L'API est protégée par un jeton JWT. Toutes les routes exigent un jeton, sauf la
connexion, Swagger et `/actuator/health`.

| Rôle | Droits |
|---|---|
| ADMIN | Tout, y compris la création des comptes (`/api/utilisateurs`) |
| SECRETAIRE | Patients, médecins, spécialités, créneaux, rendez-vous, liste d'attente, notifications |
| MEDECIN | Lecture seule de **ses propres** rendez-vous et notifications, marquer un patient absent |

Les patients ne se connectent pas : ils reçoivent les notifications de la clinique.

### Variables d'environnement

| Variable | Rôle |
|---|---|
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Compte ADMIN créé au premier démarrage s'il n'en existe aucun |
| `JWT_SECRET` | Clé de signature des jetons (32 caractères minimum, l'application refuse de démarrer sinon) |
| `JWT_EXPIRATION_MINUTES` | Durée de validité d'un jeton (60 par défaut) |

Générer un secret (PowerShell) :

    $b = New-Object byte[] 48; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)

### Utilisation

1. `POST /api/auth/connexion` avec `{"email": "...", "motDePasse": "..."}` renvoie un `token`.
2. Envoyer ensuite `Authorization: Bearer <token>` à chaque requête.
3. Dans Swagger, le bouton **Authorize** permet de coller le jeton une fois pour toutes.
4. Flux d'un nouveau médecin : la secrétaire enregistre le médecin, puis l'ADMIN lui crée son
   compte (`POST /api/utilisateurs`, rôle `MEDECIN`, avec son `medecinId`).

### Codes d'erreur

| Code | Signification |
|---|---|
| 401 `NON_AUTHENTIFIE` | Jeton absent, invalide ou expiré |
| 401 `IDENTIFIANTS_INVALIDES` | E-mail ou mot de passe incorrect |
| 403 `ACCES_REFUSE` | Rôle insuffisant pour cette action |
| 404 | Ressource inexistante **ou appartenant à un autre médecin** |

\## Tests



```bash

cd clinique\_api

./mvnw test

```



\- Tests unitaires des règles de gestion (Mockito) : quota, pénalités, file d'attente.

\- Tests d'intégration, un jeu par module, sur un vrai PostgreSQL éphémère (\*\*Testcontainers\*\* :

&#x20; Docker doit être démarré). Ils appliquent les migrations Flyway sur une base vide.

\- Test d'architecture (Spring Modulith : `ApplicationModules.verify()`).



\## Migrations Flyway



Un dossier par module dans `clinique\_api/src/main/resources/db/migration/`. Hibernate est réglé

sur `ddl-auto: validate` : seul Flyway crée et fait évoluer le schéma. Une migration déjà

appliquée n'est jamais modifiée.



\## Frontend



Le dossier `clinique\_front` est réservé au futur client Angular ; il n'est pas évalué dans ce projet.



\## Déclaration des outils d'assistance



J'ai utilisé \*\*Claude (Anthropic)\*\* comme assistant pour : <

« la conception du découpage en modules, la rédaction d'une partie du code et des tests, et la

résolution d'erreurs de configuration Docker, Maven et Testcontainers »>.

Je me suis assuré de comprendre chaque partie du code livré et je peux l'expliquer et le modifier en direct.

