# Roadmap — De "projet de licence" à marketplace B2B fonctionnelle

> Complète [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md) (état des lieux). Ce document propose le plan pour rendre le projet réellement utilisable, dans l'esprit d'un "mini Alibaba B2B" (marchands ↔ fournisseurs, sans viser toutes les fonctionnalités d'Alibaba).

## Principe de travail

1. **Phase 0 — Stabilisation** : corriger ce qui casse ou est dangereux dans l'existant (sécurité, bugs bloquants) sans ajouter de fonctionnalité.
2. **Phase 1 — Fondations manquantes** : ce qu'une marketplace B2B ne peut pas fonctionner sans (panier/commande cohérente, paiement au moins simulé, gestion de stock correcte).
3. **Phase 2 — Fonctionnalités de valeur** : ce qui différencie une vraie marketplace d'un simple CRUD (recherche, avis, messagerie, tableaux de bord).
4. **Phase 3 — Industrialisation** : tests, profils d'environnement, CI, observabilité.

Chaque correction/fonctionnalité = un commit dédié et cohérent, poussé sur `origin/master` (ou une branche si le changement est risqué) après validation. **Les commits sont faits en ton nom uniquement (`cybercoder0101`), sans co-auteur Claude.**

---

## Phase 0 — Stabilisation (corrige l'existant, ne casse rien)

Reprend les points de [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md) §4-5, dans l'ordre de priorité :

1. Secret JWT externalisé en variable d'environnement (plus de secret en dur dans `SecParams`)
2. Vérifier/corriger le mapping `/baba/login` vs context-path — s'assurer que le login fonctionne réellement
3. Contrôle de propriété (IDOR) sur les endpoints self-service (changepassword, updateinfos...)
4. Restreindre l'écriture sur `/categories` et `/souscategories` (ADMIN/FOURNISSEUR selon le cas)
5. `@JsonIgnore` sur `Utilisateur.password`
6. Unifier la politique CORS (un seul endroit, pas de `origins="*"` + credentials)
7. Nettoyer les `@PreAuthorize` morts ou réactiver `@EnableMethodSecurity`
8. Restreindre les endpoints de création/suppression sans règle explicite (ex. `ServiceLivraison`) à ADMIN
9. Corriger les bugs identifiés : `Optional.get()` non sécurisés, bug de troncature dans `generateCommandeReference`, double `save()`, imports morts
10. Révoquer le mot de passe Gmail exposé dans l'historique git (action manuelle de ta part côté compte Google, je ne peux pas le faire)

Objectif de fin de phase : l'API est utilisable en toute sécurité par un futur frontend, sans faille béante ni bug silencieux.

---

## Phase 1 — Fondations manquantes d'une marketplace B2B

Ce qui manque structurellement pour qu'un marchand puisse réellement "commander" chez un fournisseur :

- **Panier** : actuellement `Commande` semble se créer directement en une fois (`newcommande`). Il manque un état "panier en cours" avant validation. À vérifier/introduire un état `Etat.PANIER` ou une entité `Panier` séparée.
- **Multi-fournisseurs par commande** : une commande B2B panier peut contenir des lignes de plusieurs fournisseurs différents (`LigneCommande.produit.fournisseur`) — il faut clarifier si une `Commande` = un fournisseur ou peut être scindée automatiquement en sous-commandes par fournisseur à la validation (comme le fait Alibaba/Amazon).
- **Gestion de stock réelle** : `Produit.quantite` existe mais rien ne semble décrémenter le stock à la commande, ni vérifier la disponibilité avant validation. À ajouter avec gestion de la concurrence (verrouillage optimiste `@Version`).
- **Paiement** : aucune notion de paiement dans le modèle actuel (`Commande` n'a pas de statut de paiement). Pour un premier jalon fonctionnel : un statut `PaymentStatus` (EN_ATTENTE/PAYEE/ANNULEE) même sans intégrer un vrai PSP, avec possibilité d'intégrer Stripe/CinetPay/etc. plus tard.
- **Devis / négociation de prix** : typique du B2B (contrairement au B2C) — un marchand peut demander un prix pour une grande quantité avant de commander. Fonctionnalité à évaluer selon tes priorités (peut être Phase 2).
- **Adresses multiples par utilisateur** : le modèle actuel a `Utilisateur.adresse` en `@ManyToOne` unique — un marchand avec plusieurs points de vente ne peut pas avoir plusieurs adresses de livraison. À revoir en `@OneToMany`.

---

## Phase 2 — Fonctionnalités de valeur (différenciantes)

- **Recherche et filtres avancés** : recherche full-text sur produits (nom, description, catégorie, fourchette de prix, fournisseur, pays) — actuellement dispersée en une multitude d'endpoints GET séparés (`/nomasc`, `/prixasc`, `/prix/{prix}`...). À terme, un seul endpoint de recherche paramétrable serait plus propre (déjà amorcé avec `ProduitFilterRequest`, à finaliser correctement en `@RequestParam`/`@RequestBody` sur POST).
- **Avis et notation** des fournisseurs (et éventuellement des marchands) — confiance clé en B2B.
- **Messagerie / demandes de contact** marchand ↔ fournisseur avant commande (négociation).
- **Tableaux de bord** par rôle : ADMIN (vue globale, KPIs), FOURNISSEUR (ses ventes, ses produits, ses commandes en attente), MARCHAND (historique de commandes, suivi de livraison), SERVICE_LIVRAISON (gestion de flotte de livreurs, commandes assignées).
- **Notifications** : au-delà de l'email, notifications in-app / webhook pour changement d'état de commande.
- **Suivi de commande en temps réel / historique d'états** : actuellement `Commande.etat` est un simple champ écrasé à chaque changement — pas d'historique. Ajouter une table `HistoriqueEtatCommande` pour la traçabilité (utile en B2B pour litiges).
- **Facturation** : génération de factures PDF pour les commandes (utile en B2B où la compta est plus formelle qu'en B2C).
- **Frontend** : il n'y a aucun frontend dans ce dépôt actuellement. À décider ensemble : Angular (cohérent avec les traces trouvées dans l'historique), React, ou autre.

---

## Phase 3 — Industrialisation

- Tests unitaires (services) et d'intégration (contrôleurs avec `@SpringBootTest` + base H2/Testcontainers) — quasi absents actuellement.
- Profils Spring `dev`/`prod` (désactiver `show-sql`, `TRACE`, ajuster `ddl-auto`).
- Migration de schéma versionnée (Flyway ou Liquibase) au lieu de `ddl-auto=update`.
- CI (GitHub Actions) : build + tests à chaque push/PR.
- Observabilité : exploiter Actuator (déjà ajouté) avec des endpoints de health/metrics correctement exposés (pas publiquement en prod).
- Stockage des images hors base (S3/MinIO/disque) au lieu de BLOB MySQL, si le volume de produits grandit.

---

## Ordre de travail proposé

On avance phase par phase, un point à la fois, avec commit + push après chaque correction/fonctionnalité validée. On commence par la **Phase 0** (sécurité et bugs), car construire de nouvelles fonctionnalités sur une base avec IDOR et secret JWT en dur serait contre-productif.

## Historique

- 2026-07-14 : création de la roadmap après l'audit initial du projet.
- 2026-07-14 : Phase 0 (stabilisation) terminée et poussée sur `origin/master` :
  secret JWT externalisé (`.env` + spring-dotenv), mapping `/login` corrigé,
  IDOR corrigés sur les endpoints self-service et sur `/commandes`, écriture
  du catalogue restreinte à ADMIN, mot de passe masqué en sortie JSON, CORS
  unifié, annotations `@PreAuthorize` mortes supprimées, endpoints sans règle
  de rôle explicite restreints, bugs de code corrigés (`Optional.get()` non
  gardés, bug de troncature `generateCommandeReference`, double `save()`,
  imports morts). Limites connues restantes : `/commandes/getbyref/{ref}`
  reste accessible à tout utilisateur authentifié sans vérification de
  propriété (référence potentiellement devinable) ; `/fournisseurs/**` reste
  entièrement whitelisté au niveau `SecurityConfig` mais les écritures sont
  protégées par un contrôle de propriété au niveau contrôleur. À reprendre
  en Phase 1 si besoin de durcissement supplémentaire.
  Prochaine étape : Phase 1 (fondations manquantes de la marketplace).