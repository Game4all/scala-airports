## Projet Functionnal Programming Scala


### Feature list
- [x] Parsing des fichiers CSV
- [x] Case classes pour les 3 modèles
- [x] Recherche des pistes + infos depuis nom pays / code pays
- [x] Queries du mode rapport
**Bonus**
- [x] GUI avec JavaFX
- [x] Fuzzy matching (fait dans l'UI + dans le code requête)
- [x] BDD SQL avec H2 + slick
- [x] Utiliser Futures pour la BDD      

### Explication de la structure de fichiers

- `src/scala`
    - `csv.scala` : Logique de parsing du CSV
    - `database.scala` : Logique de la BDD SQL H2 + def des tables pour bonus BDD
    - `models.scala` : Modèles (Aéroport, Piste et Pays + companion objects)
    - `queries.scala` : Code des requêtes demandées en SQL avec slick
    - `app.scala` : Interface utilisateur (GUI) bonus interface.
    - `gui_base.scala` : Classes commune pour interface utilisateur (GUI).
