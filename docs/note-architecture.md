\# Note d'architecture



\## 1. Découpage en modules



L'application est un seul déployable (un JAR) découpé en quatre modules métier et un module technique.



| Module | Responsabilité |

|---|---|

| `catalogue` | spécialités, médecins, créneaux ; recherche multicritère |

| `acteurs` | patients, points de pénalité |

| `transactions` | rendez-vous, quota, conflits, liste d'attente, rappels |

| `notifications` | enregistrement des notifications |

| `shared` | exceptions, gestion centralisée des erreurs, configuration (aucune logique métier) |



Pourquoi ce découpage : chaque module correspond à un vocabulaire métier distinct. `transactions`

est le seul à orchestrer les autres ; `catalogue` et `acteurs` ne connaissent personne.



\## 2. Règles de dépendance



1\. Un package racine par module, avec `api` (interfaces, DTO, événements) et `internal`

&#x20;  (entités, repositories, services, contrôleurs).

2\. Un module n'importe jamais le `internal` d'un autre. `transactions` n'utilise que

&#x20;  `CreneauApi` (catalogue) et `PatientApi` (acteurs).

3\. Aucune relation JPA entre modules : on référence l'autre module par identifiant

&#x20;  (`patientId`, `creneauId`), jamais par `@ManyToOne`. Les tables n'ont pas non plus de clé

&#x20;  étrangère entre modules.

4\. Pas de dépendance circulaire : `catalogue` et `acteurs` ne dépendent que de `shared`.

5\. `notifications` ne dépend que des événements publiés dans l'`api` de `transactions`.

&#x20;  `transactions` ignore l'existence de `notifications`.



Ces règles sont déclarées dans les `package-info.java` (`@ApplicationModule`,

`@NamedInterface`) et vérifiées par `ArchitectureTest` (Spring Modulith, `verify()`). Si une

règle est violée, le test échoue.



\*\*Communication par événement\*\* : `transactions` publie `RendezVousPris`, `RendezVousAnnule` et

`RappelRendezVous` (`ApplicationEventPublisher`). `notifications` les écoute avec

`@ApplicationModuleListener` : exécution après le commit, dans un autre thread, dans une nouvelle

transaction. Une réservation refusée n'émet aucune notification, puisque la transaction échoue.



\*\*Gestion de la concurrence\*\* : la colonne `version` de `creneau` (verrouillage optimiste) et un

index unique partiel (`un seul rendez-vous CONFIRME par créneau`) empêchent un double rendez-vous.



\## 3. Pourquoi Flyway



\- Le schéma est décrit en \*\*SQL natif\*\*, lisible et directement exécutable dans `psql`.

&#x20; Liquibase impose un format intermédiaire (XML, YAML) dont l'intérêt est la portabilité entre

&#x20; SGBD ; ce projet est sur PostgreSQL seulement.

\- Intégration directe à Spring Boot : les migrations s'appliquent au démarrage, y compris dans

&#x20; `docker compose up` et dans les tests Testcontainers.

\- Nommage `V<version>\_\_<module>\_<description>.sql`, un dossier par module. Les numéros sont

&#x20; globaux : une nouvelle migration reçoit toujours un numéro supérieur au plus grand existant,

&#x20; quel que soit le module.

\- `ddl-auto: validate` : Hibernate vérifie le schéma sans jamais le modifier.

\- Une migration appliquée n'est jamais modifiée (Flyway en garde une empreinte) : toute

&#x20; correction est une nouvelle version (exemple : `V6\_1` ajoute `fin` à `rendez\_vous`).



\## 4. Limites connues



\- \*\*Intégrité entre modules\*\* : sans clé étrangère entre modules, la base n'empêche pas de

&#x20; supprimer un patient qui a des rendez-vous, ni un créneau qui a une liste d'attente. C'est le

&#x20; compromis du monolithe modulaire.

\- \*\*Événements non persistés\*\* : si un écouteur échoue, l'événement est perdu. L'Event

&#x20; Publication Registry de Spring Modulith (événements persistés et rejoués) corrigerait cela.

\- \*\*Pas d'authentification\*\* : toute personne peut lire les notifications ou les rendez-vous de

&#x20; n'importe quel patient. Spring Security avec JWT serait la suite logique.

\- \*\*Quota sans verrou global\*\* : deux demandes simultanées du même patient pourraient dépasser

&#x20; le quota de 3 ; seul le créneau est protégé contre les accès concurrents.

\- \*\*Dates sans fuseau horaire\*\* : `LocalDateTime`, interprété dans le fuseau du serveur (UTC dans

&#x20; le conteneur).

\- \*\*Planificateur\*\* : une seule instance est supposée ; avec plusieurs instances, les rappels

&#x20; seraient exécutés plusieurs fois.

\- \*\*Mot de passe de démonstration\*\* : `docker-compose.yml` fournit une valeur par défaut pour que

&#x20; le projet démarre sans configuration ; elle est surchargée par `.env` pour tout usage réel.

