# Guide d'intégration Frontend — État de l'API après corrections (2026-07-14)

> Ce document explique ce qui a changé côté backend depuis le début des corrections (Phase 0 : sécurité/stabilisation, Phase 1 : panier/stock/paiement/adresses), pour que le frontend s'y adapte. Voir aussi [PROJECT_CONTEXT.md](./PROJECT_CONTEXT.md) et [ROADMAP.md](./ROADMAP.md) pour le contexte complet.

Base URL : `http://<host>:<port>/baba` (context-path `/baba`).

## 1. Authentification

- `POST /baba/login` — body JSON `{"email": "...", "password": "..."}`. Retourne le JWT dans le **header de réponse** `Authorization: Bearer <token>` (pas dans le body).
- Toutes les routes protégées attendent le header `Authorization: Bearer <token>` sur chaque requête.
- Le token expire après 2h (configurable côté serveur).
- **CORS** : une seule origine autorisée, `http://localhost:4200`, avec `credentials: true`. Si le frontend tourne sur un autre port/host en dev, il faut le signaler pour ajuster `SecurityConfig.corsConfigurationSource()`.

## 2. Format des réponses d'erreur (nouveau)

Toutes les erreurs renvoient maintenant un JSON cohérent au lieu de pages d'erreur Spring :

```json
{
  "timestamp": "2026-07-14T10:00:00",
  "message": "Produit introuvable",
  "path": "/baba/produits/detailprod/999",
  "errorCode": "NOT_FOUND"
}
```

Codes HTTP et `errorCode` possibles :
| HTTP | errorCode | Cas |
|---|---|---|
| 404 | `NOT_FOUND` | Ressource introuvable |
| 409 | `EMAIL_ALREADY_EXISTS` | Email déjà utilisé à l'inscription |
| 409 | `STOCK_CONFLICT` | Stock modifié entre-temps (concurrence), réessayer |
| 400 | `INVALID_TOKEN` / `EXPIRED_TOKEN` | Token de vérification d'email invalide/expiré |
| 400 | `VALIDATION_FAILED` | Champs invalides — voir format spécial ci-dessous |
| 403 | `ACCESS_DENIED` | Pas les droits sur cette ressource (IDOR bloqué) |
| 401 | — | Token manquant/invalide (`{"error": "Unauthorized - ..."}`, format différent, géré au niveau filtre) |
| 500 | `INTERNAL_ERROR` | Erreur serveur inattendue |

### Erreurs de validation (`VALIDATION_FAILED`)

Format spécifique avec le détail par champ :
```json
{
  "timestamp": "...",
  "path": "...",
  "errorCode": "VALIDATION_FAILED",
  "message": "Donnees invalides",
  "fieldErrors": {
    "email": "L'email doit etre une adresse valide",
    "telephone": "Le telephone doit etre un numero valide (6 a 20 chiffres, prefixe + optionnel)"
  }
}
```

## 3. Contraintes de validation à respecter côté formulaire

Ces règles sont appliquées **côté serveur** sur les endpoints de création (voir §5). Le frontend devrait les répliquer pour un feedback immédiat :

- `nom` / `prenom` : lettres/espaces/apostrophes/tirets uniquement, 2 à 50 caractères
- `telephone` : chiffres et espaces, `+` optionnel en préfixe, 6 à 20 caractères
- `email` : format email valide
- `password` : 8 à 72 caractères (n'est **jamais** renvoyé par l'API, même en écriture — voir §4)
- `pays` / `ville` / `emplacement` (adresse) : obligatoires
- `nomProd` : obligatoire ; `prixProd` : strictement positif ; `quantite` : entier ≥ 0
- `nom` de catégorie/sous-catégorie : obligatoire

⚠️ Ces contraintes ne s'appliquent **qu'aux endpoints de création** (register, addFournisseur, addlivreur, addSl, addprod/update, addcat/updatecat, addsscat/updatecat). Les endpoints de mise à jour de profil (`updateinfos`, `updateL`, `updateSl`, `updateinfosuser`) acceptent encore des objets partiels sans validation stricte — à garder en tête si vous branchez un formulaire d'édition dessus.

## 4. Le mot de passe n'est plus jamais renvoyé

Tous les objets `Utilisateur`/`Marchand`/`Fournisseur`/`Livreur`/`ServiceLivraison` renvoyés par l'API n'incluent plus le champ `password` (avant, le hash BCrypt était exposé). Le champ reste accepté en **entrée** (login, inscription) mais disparaît de toute réponse JSON.

## 5. Contrôle d'accès par rôle — résumé

| Rôle | Accès notable |
|---|---|
| Public (pas de token) | lecture catalogue (`/categories`, `/souscategories`, `/produits` en GET), `/fournisseurs/**`, inscription marchand (`/marchands/register`) |
| ADMIN | gestion utilisateurs/marchands, écriture catégories/sous-catégories, création service de livraison, vue globale des commandes |
| ACHETEUR (marchand) | panier, adresses de livraison, ses propres commandes, mise à jour/suppression de son propre compte |
| FOURNISSEUR | CRUD sur ses produits |
| SERVICE_LIVRAISON | gestion de ses livreurs, assignation de livreur sur ses commandes |
| LIVREUR | ses propres commandes assignées |

**Important pour le frontend** : les endpoints self-service (changement de mot de passe, mise à jour de profil, suppression de compte) vérifient désormais que l'utilisateur authentifié est bien le propriétaire de la ressource ciblée — un appel avec l'id d'un autre utilisateur renvoie `403 ACCESS_DENIED` au lieu de réussir silencieusement comme avant.

## 6. Nouveau flux panier (à utiliser à la place de l'ancien `POST /commandes/newcommande` pour tout nouveau développement)

L'ancien flux `POST /commandes/newcommande` (envoyer une `Commande` complète en un seul appel) **existe toujours** et fonctionne, mais ne gère ni le stock ni le multi-fournisseurs. Le nouveau flux panier est recommandé :

1. **`GET /commandes/panier`** — récupère (ou crée) le panier en cours du marchand connecté. Retourne une `Commande` avec `etat: "EN_PANIER"`.
2. **`POST /commandes/panier/ajouter`** — body `{"produitId": 1, "quantite": 3}`. Ajoute une ligne (ou incrémente si le produit y est déjà). Retourne le panier à jour.
3. **`PUT /commandes/panier/lignes/{ligneId}`** — body `{"quantite": 5}`. Modifie la quantité d'une ligne.
4. **`DELETE /commandes/panier/lignes/{ligneId}`** — retire une ligne.
5. **`POST /commandes/panier/valider`** — transforme le panier en commande(s) réelles :
   ```json
   {
     "adresseLivraisonId": 3,          // OU
     "adresseLivraison": "texte libre",
     "emailRec": "contact@boutique.com",
     "numRec": "+221771234567"
   }
   ```
   - Vérifie le stock de chaque ligne (sinon `400` avec message explicite).
   - Décrémente le stock (protégé contre les accès concurrents — `409 STOCK_CONFLICT` possible si collision, à gérer avec un retry).
   - **Scinde automatiquement le panier en une `Commande` par fournisseur** si le panier contient des produits de fournisseurs différents (comme sur Amazon/Alibaba) — la réponse est donc un **tableau** de `Commande`, pas un objet unique.
   - Le panier est vidé/supprimé après validation.

6. **`PUT /commandes/{id}/payer`** — marque une commande comme payée (`paymentStatus: "PAYEE"`). C'est un **simulateur**, il n'y a pas d'intégration bancaire réelle pour l'instant.

### Nouveau champ sur `Commande`

`paymentStatus` : `"EN_ATTENTE"` | `"PAYEE"` | `"ECHOUEE"`.

## 7. Carnet d'adresses de livraison (nouveau)

Un marchand peut désormais enregistrer plusieurs adresses de livraison (utile s'il a plusieurs points de vente), séparément de son adresse de profil :

- `GET /marchands/adresses` — liste les adresses du marchand connecté
- `POST /marchands/adresses` — body `{"libelle": "Entrepot principal", "pays": "...", "ville": "...", "emplacement": "..."}`
- `PUT /marchands/adresses/{id}`
- `DELETE /marchands/adresses/{id}`

La première adresse ajoutée devient automatiquement `parDefaut: true`. Si l'adresse par défaut est supprimée, une autre est promue automatiquement s'il en reste.

Ces adresses sont utilisables via `adresseLivraisonId` lors de la validation du panier (§6).

## 8. Points d'attention / dette restante (à ne pas supposer résolus)

- `GET /commandes/getbyref/{ref}` n'a **pas** de contrôle de propriété — accessible à tout utilisateur authentifié.
- Les endpoints de mise à jour de profil (`updateinfos`, etc.) acceptent des objets partiels sans validation stricte des champs (voir §3).
- Pas encore de pagination sur les listes (`/produits/allprods`, `/marchands/all`, etc.) — à prévoir côté frontend si les volumes grossissent.
- Pas de rafraîchissement de token (refresh token) — à l'expiration (2h), l'utilisateur doit se reconnecter.

## Historique

- 2026-07-14 : création de ce guide après la Phase 0 (sécurité/stabilisation) et la Phase 1 (panier/stock/paiement/adresses).
