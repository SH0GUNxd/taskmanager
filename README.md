# Gestionnaire de Tâches - Java Console

**Auteur :** Félix Vandenbroucke

**Date :** 21/02/2026

---

## Description

Application console en Java pour gérer une liste de tâches.
Les tâches sont sauvegardées dans un fichier JSON (`tasks.json`)
et rechargées automatiquement au démarrage.

---

## Structure du projet

```
taskmanager/
├── src/
│   ├── Main.java              point d'entrée, crée le manager et l'UI
│   ├── Task.java              modèle de données + sérialisation JSON
│   ├── TaskManager.java       logique CRUD + lecture/écriture fichier
│   └── ConsoleUI.java         interface console (menus, saisies)
├── tests/
│   └── TaskManagerTest.java   tests autonomes (sans dépendance externe)
├── out/                       classes compilées (généré, ignoré par git)
├── tasks.json                 fichier de sauvegarde (créé automatiquement)
├── .gitignore
└── README.md
```

### Rôle de chaque classe

- **Task** : contient les données d'une tâche et sait se convertir en JSON
- **TaskManager** : gère la liste en mémoire et la synchronise avec le fichier
- **ConsoleUI** : affiche les menus et lit les saisies, délègue à TaskManager
- **Main** : crée les objets et lance la boucle principale
- **TaskManagerTest** : suite de tests autonomes (102 assertions, pas de JUnit)

---

## Prérequis

- **Java 17 ou supérieur** (on utilise les switch expressions)

Vérifier la version installée :
```bash
java -version
javac -version
```

---

## Compilation

Depuis la racine du projet (le dossier qui contient `src/`) :

```bash
# 1. Créer le dossier de destination
mkdir -p out

# 2. Compiler tous les fichiers .java
javac -d out src/Main.java src/Task.java src/TaskManager.java src/ConsoleUI.java
```

Si la commande réussit, vous ne verrez aucun message d'erreur et le dossier `out/` contiendra les `.class`.

---

## Lancement

```bash
java -cp out taskmanager.Main
```

Pour utiliser un fichier de sauvegarde différent :
```bash
java -cp out taskmanager.Main mon_fichier.json
```

### Créer un JAR (optionnel)

```bash
jar --create --file TaskManager.jar --main-class taskmanager.Main -C out .
java -jar TaskManager.jar
```

---

## Tests

Les tests sont dans `tests/` et compilés séparément (ils dépendent des `.class` dans `out/`).

```bash
# 1. Compiler les sources si ce n'est pas déjà fait
javac -d out src/Main.java src/Task.java src/TaskManager.java src/ConsoleUI.java

# 2. Compiler les tests
javac -cp out -d out tests/TaskManagerTest.java

# 3. Lancer
java -cp out taskmanager.TaskManagerTest
```

Retourne un code de sortie 0 si tous les tests passent, 1 sinon (compatible CI).

---

## Exemple de session

```
         GESTIONNAIRE DE TACHES  v1.0

[INFO] 3 tache(s) chargee(s) depuis tasks.json

  ------------------------------------------------------------
  MENU PRINCIPAL
  ------------------------------------------------------------
  [1] Ajouter une tache
  [2] Lister les taches
  [3] Modifier une tache
  [4] Supprimer une tache
  [5] Statistiques
  [0] Quitter
  ------------------------------------------------------------
  > Votre choix : 1

  --- AJOUTER UNE TACHE ---

  > Titre : Préparer la présentation
  > Description (facultative, Entree pour ignorer) : Slides pour la soutenance du TP
  > Date d'echeance (format : dd/MM/yyyy) : 15/03/2025
  > Statut (TODO / DOING / DONE) [defaut : TODO] :

  Tache ajoutee avec succes :
  +------------------------------------------+
  |  ID          : 4
  |  Titre       : Préparer la présentation
  |  Description : Slides pour la soutenance du TP
  |  Echeance    : 15/03/2025
  |  Statut      : TODO
  +------------------------------------------+

  > Votre choix : 2

  --- LISTE DES TACHES ---

  Filtrer par statut :
  [1] Toutes les taches
  [2] TODO seulement
  [3] DOING seulement
  [4] DONE seulement
  > Filtre : 1

  -----------------------------------------------------------------------------------------------
  ID    STATUT      TITRE                           DESCRIPTION                               ECHEANCE
  -----------------------------------------------------------------------------------------------
  1     [ DONE  ]   Configurer l'environnement      Installer JDK 21 et configurer PATH       28/02/2025
  2     [ DOING ]   Écrire les tests unitaires      Couvrir Task et TaskManager avec JUnit    10/03/2025
  3     [ TODO  ]   Rédiger la documentation        Compléter le README et les Javadoc        15/03/2025
  4     [ TODO  ]   Préparer la présentation        Slides pour la soutenance du TP           15/03/2025
  -----------------------------------------------------------------------------------------------
  4 tache(s) affichee(s)

  > Votre choix : 0

Au revoir !
```

---

## Format du fichier `tasks.json`

Le fichier est mis à jour automatiquement après chaque ajout, modification ou suppression.
L'écriture est atomique : un fichier temporaire est écrit puis déplacé sur le fichier final,
ce qui évite toute perte de données en cas de coupure.

```json
[
  {"id":1,"title":"Configurer l'environnement","description":"Installer JDK 21 et configurer PATH","dueDate":"2025-02-28","status":"DONE"},
  {"id":2,"title":"Ecrire les tests unitaires","description":"Couvrir Task et TaskManager avec JUnit","dueDate":"2025-03-10","status":"DOING"},
  {"id":3,"title":"Rediger la documentation","description":"Completer le README et les Javadoc","dueDate":"2025-03-15","status":"TODO"}
]
```

Chaque objet JSON contient : `id`, `title`, `description`, `dueDate` (format ISO : AAAA-MM-JJ), `status`.

> **Note** : la date est stockée au format ISO (AAAA-MM-JJ) dans le fichier pour faciliter le parsing,
> mais affichée au format français (JJ/MM/AAAA) dans le terminal.
