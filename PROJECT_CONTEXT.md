# Babankassouwa — Plateforme B2B Marchands / Fournisseurs

> Document de contexte pour reprendre le projet sans perdre le fil. Écrit le 2026-07-14.
> À mettre à jour à chaque évolution majeure (nouvelle entité, changement d'archi, faille corrigée...).

## 1. Vision du projet

Plateforme de commerce **B2B** mettant en relation :
- des **Marchands** (grandes surfaces, commerçants, boutiques) qui achètent,
- des **Fournisseurs** qui vendent des produits via un catalogue (Catégories / Sous-catégories / Produits),
- des **Services de livraison** qui gèrent une flotte de **Livreurs** pour acheminer les commandes,
- un rôle **Administrateur** pour la gestion globale.

Projet démarré en licence, actuellement un **backend Spring Boot seul** (pas de frontend dans ce dépôt — un frontend Angular consommant l'API sur `http://localhost:4200` semble avoir existé ou être prévu, cf. CORS et un ancien commit `pfebase+angulartocontinue`).

## 2. Stack technique

- **Java 19**, **Spring Boot 3.2.3**, Maven, module unique `com.pfe:code`
- **Spring Data JPA** + **MySQL** (`mysql-connector-j`), base `babakfinal`, `ddl-auto=update` (pas de Flyway/Liquibase)
- **Spring Security** stateless + **JWT** maison (lib `com.auth0:java-jwt:3.4.1`, assez ancienne)
- **Spring Mail** (SMTP Gmail) pour l'envoi d'emails (vérification de compte marchand, notifications)
- **springdoc-openapi** (Swagger UI) pour la doc API
- Lombok partout dans les entités
- Dépendances **mortes/à nettoyer** : toute la suite `spring-integration-*` (7 artefacts, aucun usage constaté), imports `@PreAuthorize`/`@PostAuthorize` non fonctionnels (voir §5)
- Thymeleaf vient d'être retiré du pom (cohérent avec un backend 100% REST)
- Actuator vient d'être ajouté

`server.servlet.context-path=/baba` — **toutes les routes ci-dessous sont donc préfixées `/baba/...` en runtime**, mais Spring Security matche les routes **sans** ce préfixe.

## 3. Modèle de données (JPA)

Hiérarchie d'héritage `Utilisateur` → `Marchand` / `Fournisseur` / `Livreur` / `Administrateur` / `ServiceLivraison`, toutes `@Entity` concrètes **sans `@Inheritance` explicite** → Hibernate applique SINGLE_TABLE par défaut (une seule table, plein de colonnes NULL selon le type). À clarifier/documenter consciemment (SINGLE_TABLE, JOINED ou TABLE_PER_CLASS ?).

Entités principales :
- **Utilisateur** : nom, prenom, telephone, password (BCrypt), email (unique), adresse (`@ManyToOne` vers `Adresse` — bizarre pour une relation 1-1), role (enum `Role`)
- **Marchand** : `isactive` (champ **public**, à corriger), commandes
- **Fournisseur** : description, produits (`EAGER` + `cascade=ALL` — à revoir, risque perf/suppression en cascade)
- **Livreur** : matricule, serviceLivraison, commandesLivreur
- **Administrateur** : matricule
- **ServiceLivraison** : description, livreurs (`EAGER`), commandes
- **Commande** : reference, dateCommande, marchand, adresseLivraison (String libre, dupliqué avec `Adresse`), lignesCommande, serviceLivraison, livreur, etat (enum `Etat`)
- **LigneCommande** : commande, produit, quantité, prixligne (`Long` — incohérent avec `Produit.prixProd` en `Double`)
- **Produit** : nomProd, prixProd (Double), quantite (défaut codé en dur `400L`), fournisseur, categorie, sousCategorie, images
- **Categorie** / **SousCategorie** : catalogue hiérarchique classique
- **Image** : `byte[]` stocké en BLOB en base (limite 4 Mo) — à migrer vers un stockage objet (S3-like) si le projet grandit
- **Adresse** : pays, ville, emplacement
- **VerificationToken** : mal rangée dans `services/request` alors que c'est une `@Entity` persistée — à déplacer dans `entities/`
- Enums : `Role` (ADMIN, FOURNISSEUR, ACHETEUR, SERVICE_LIVRAISON, LIVREUR), `Etat` (EN_ATTENTE, EN_COURS, LIVREE, ANNULEE)

## 4. Sécurité — état actuel (chantier en cours, non commité)

Un refactor de `security/` est en cours dans le working tree (fichiers modifiés non commités). Ce qui a déjà été corrigé dans ce chantier :
- `JWTAuthenticationFilter` : `authenticationManager` maintenant correctement lié via `super.setAuthenticationManager()` (avant : jamais réellement configuré), header `Authorization` avec le préfixe `"Bearer "` (manquant avant), gestion propre des erreurs de parsing/nullité email-password
- `JWTAuthorizationFilter` : requête sans token laisse maintenant filer la chaîne (avant : bloquait silencieusement sans réponse), token invalide/expiré → 401 JSON propre (avant : 500 non géré)
- `SecurityBeansConfiguration` : suppression du bean `AuthenticationManager` dupliqué
- `SecurityConfig` : construction interne de l'`AuthenticationManager` via `AuthenticationManagerBuilder`, ajout d'un `CorsConfigurationSource` dédié, gestion JSON centralisée des 401/403
- `application.properties` : identifiants Gmail externalisés en variables d'env `SPRING_MAIL_USERNAME`/`SPRING_MAIL_PASSWORD`

### ⚠️ Problèmes de sécurité restants à traiter en priorité

1. **Secret JWT en dur** dans `SecParams.java` (`"salif667@gmail/com"`) — n'importe qui avec accès au dépôt peut forger un token ADMIN. **À faire immédiatement** : déplacer dans une variable d'environnement.
2. **Un mot de passe d'application Gmail a été committé en clair** dans l'historique git (commit `8e8d379` ou antérieur) avant d'être externalisé. Même supprimé du code actuel, il reste dans `git log`. **À faire** : révoquer ce mot de passe d'application Gmail dès que possible, et envisager une réécriture d'historique si le dépôt devient public.
3. **IDOR** sur plusieurs endpoints (`/users/changepassword/{id}`, `/users/updateinfosuser`, `/marchands/updateinfos`, `/fournisseurs/updateinfos`, `/livreurs/updateL`) : aucun contrôle que l'utilisateur authentifié correspond à l'`{id}` ciblé — un utilisateur connecté peut modifier/supprimer le compte de n'importe qui.
4. `/categories/**` et `/souscategories/**` sont **entièrement publiques**, y compris les écritures (POST/PUT/DELETE) — n'importe qui peut modifier le catalogue sans être connecté.
5. `Utilisateur.password` n'a pas de `@JsonIgnore` → le hash BCrypt est renvoyé dans toutes les réponses JSON contenant un utilisateur.
6. **CORS incohérent** : `SecurityConfig` restreint à `localhost:4200` avec `allowCredentials=true`, mais presque tous les contrôleurs ont `@CrossOrigin(origins="*")` en plus — contradictoire et invalide côté spec CORS (wildcard + credentials interdits). Choisir une seule source de vérité.
7. La whitelist contient `"/baba/login"` alors que Spring Security matche normalement le chemin **sans** le context-path → risque que le login lui-même soit mal routé/bloqué. **À tester en priorité.**
8. `@EnableMethodSecurity` a été retiré de `SecurityConfig`, ce qui rend **inertes** tous les `@PreAuthorize`/`@PostAuthorize` encore présents dans les contrôleurs (imports morts trompeurs) — soit les supprimer, soit réactiver `@EnableMethodSecurity` et les rendre fonctionnels.
9. Endpoints sans règle de rôle explicite (ex. `POST /serviceslivraison/addSl`, tout `CommandeRESTController`) retombent sur `anyRequest().authenticated()` → accessibles à **tout** utilisateur connecté, pas seulement ADMIN. Pas de contrôle de propriété sur les commandes (un Fournisseur/Livreur peut voir/modifier les commandes d'un autre marchand).
10. `logging.level.org.springframework.security=TRACE` et `spring.jpa.show-sql=true` actifs sans profil dev/prod — à conditionner par profil Spring avant tout déploiement.

## 5. Bugs de code identifiés (hors sécurité)

- `CommandeServiceImpl.generateCommandeReference` : bug de copier-coller, utilise `nomMarchand.length()` au lieu de `nomSL.length()` pour tronquer `nomSL`.
- `MarchandServiceImpl.createMarchand` : appelle `save()` deux fois pour rien.
- Usage répété de `Optional.get()` sans vérification (`LivreurServiceImpl`, `ProduitServiceImpl.getProd`, `CommandeServiceImpl.setLivreurCommande`/`updateEtat`, `MarchandRESTCONTROLLER.getByMail`) → `NoSuchElementException` non gérée, 500 générique au lieu d'erreur métier claire.
- Imports morts/erronés dans `MarchandServiceImpl` (`org.antlr.v4.runtime.Token`, `org.apache.catalina.User` — probablement de l'autocomplétion IDE mal validée).
- `ProduitRESTController.filtre` et `.rechercherProduits` utilisent `@RequestBody` sur des requêtes **GET** — non conforme HTTP.
- Endpoints dupliqués dans `SousCategorieRESTController` (`getSousCat` vs `getbyid`).
- Types monétaires incohérents : `Produit.prixProd` (Double), `LigneCommande.prixligne` (Long), `Commande.prixtotal` (Double) — à unifier en `BigDecimal`.
- `Marchand.isactive` est un champ public — à encapsuler proprement.
- Nom de classe non conventionnel : `MarchandRESTCONTROLLER` (casse incohérente vs les autres `XxxRESTController`).
- Problèmes d'encodage détectés dans certains messages d'erreur (`"non trouvÃ©"`) — vérifier l'encodage UTF-8 des fichiers sources.
- `VerificationToken` mal rangée dans `services/request` alors que c'est une entité persistée.

## 6. Endpoints REST — vue d'ensemble

Toutes les routes ci-dessous sont sous le préfixe `/baba`.

| Contrôleur | Base path | Rôle requis (actuel) |
|---|---|---|
| UserRESTController | `/users` | ADMIN (gestion), COMMUN (self-service — **IDOR à corriger**) |
| MarchandRESTCONTROLLER | `/marchands` | register public, ADMIN (liste/delete), ACHETEUR (self-update) |
| FournisseurRESTController | `/fournisseurs` | lecture publique, écriture non clairement restreinte |
| LivreurRESTController | `/livreurs` | SERVICE / SERVICE_OR_LIVREUR |
| ServiceLivraisonRESTController | `/serviceslivraison` | **à restreindre à ADMIN** (actuellement ouvert à tout authentifié) |
| CategorieRESTController | `/categories` | **publique en écriture — faille à corriger** |
| SousCategorieRESTController | `/souscategories` | **publique en écriture — faille à corriger** |
| ProduitRESTController | `/produits` | FOURNISSEUR pour écriture, lecture publique |
| ImageRESTController | `/images` | authenticated (comportement changé récemment, à vérifier vs flux d'inscription) |
| CommandeRESTController | `/commandes` | authenticated, **sans contrôle de propriété** |
| EtatCommandeRESTController | `/etats` | lecture des valeurs de l'enum |

## 7. Prochaines étapes suggérées

1. Corriger le secret JWT en dur (variable d'env) et révoquer le mot de passe Gmail exposé dans l'historique.
2. Corriger le mapping `/baba/login` et vérifier que le login fonctionne réellement avec la nouvelle `SecurityConfig`.
3. Ajouter un contrôle de propriété (comparer l'id du token JWT à l'id ciblé) sur tous les endpoints self-service (`changepassword`, `updateinfos`, etc.).
4. Restreindre `/categories` et `/souscategories` en écriture à ADMIN/FOURNISSEUR selon le besoin métier.
5. Unifier CORS (soit `SecurityConfig`, soit `@CrossOrigin`, pas les deux) et retirer `origins="*"` combiné à des credentials.
6. Ajouter `@JsonIgnore` sur `Utilisateur.password`.
7. Décider consciemment de la stratégie d'héritage JPA (`@Inheritance(strategy = ...)`).
8. Nettoyer les dépendances `spring-integration-*` si vraiment inutilisées, et les imports `@PreAuthorize` morts (ou réactiver `@EnableMethodSecurity`).
9. Introduire des profils Spring (`application-dev.properties` / `application-prod.properties`) pour désactiver `show-sql`/`TRACE` en prod.
10. Envisager `BigDecimal` pour tous les montants.
11. Ajouter des tests (actuellement quasi inexistants au-delà du squelette par défaut).

## 8. Historique de ce document

- 2026-07-14 : création initiale après analyse complète du projet (structure, stack, modèle de données, sécurité, endpoints, bugs).