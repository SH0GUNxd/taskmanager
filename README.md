# Gestionnaire de Tâches - Java Console & Web

**Auteur :** Félix Vandenbroucke

**Date :** 21/02/2026

---

## Description

Application de gestion de tâches en Java.
Deux modes de lancement : **console** (terminal interactif) et **web** (interface dark mode dans le navigateur).
Les tâches sont sauvegardées dans un fichier JSON (`tasks.json`) et rechargées automatiquement au démarrage.

---

## Structure du projet

```
taskmanager/
├── src/
│   ├── Main.java              point d'entrée — gère les modes console et web
│   ├── Task.java              modèle de données + sérialisation JSON
│   ├── TaskManager.java       logique CRUD + lecture/écriture fichier
│   ├── ConsoleUI.java         interface console (menus, saisies)
│   └── ApiServer.java         serveur HTTP REST (JDK built-in, zéro dépendance)
├── tests/
│   ├── TaskManagerTest.java   tests unitaires logique métier (102 assertions)
│   └── ApiServerTest.java     tests d'intégration API HTTP (69 assertions)
├── web/
│   └── index.html             frontend dark mode (HTML/CSS/JS, zéro framework)
├── out/                       classes compilées (généré, ignoré par git)
├── tasks.json                 fichier de sauvegarde (créé automatiquement)
├── .gitignore
└── README.md
```

### Rôle de chaque classe

- **Task** : contient les données d'une tâche et sait se convertir en JSON
- **TaskManager** : gère la liste en mémoire et la synchronise avec le fichier
- **ConsoleUI** : affiche les menus et lit les saisies, délègue à TaskManager
- **ApiServer** : serveur HTTP REST basé sur `com.sun.net.httpserver` (inclus dans le JDK)
- **Main** : crée les objets et lance le mode choisi

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

Depuis la racine du projet :

```bash
mkdir -p out
javac -d out src/Task.java src/TaskManager.java src/ConsoleUI.java src/ApiServer.java src/Main.java
```

---

## Lancement

### Mode console

```bash
java -cp out taskmanager.Main
```

### Mode web

```bash
java -cp out taskmanager.Main --web
```

Puis ouvrir **http://localhost:8080** dans le navigateur.

Port personnalisé :
```bash
java -cp out taskmanager.Main --web 3000
```

Fichier de sauvegarde personnalisé :
```bash
java -cp out taskmanager.Main --web mon_fichier.json
```

### Créer un JAR (optionnel)

```bash
jar --create --file TaskManager.jar --main-class taskmanager.Main -C out .
java -jar TaskManager.jar --web
```

---

## API REST

Le serveur expose les endpoints suivants :

| Méthode  | Endpoint                    | Description                            |
|----------|-----------------------------|----------------------------------------|
| `GET`    | `/api/tasks`                | Liste toutes les tâches                |
| `GET`    | `/api/tasks?status=TODO`    | Filtre par statut (TODO/DOING/DONE)    |
| `POST`   | `/api/tasks`                | Crée une tâche                         |
| `PUT`    | `/api/tasks/{id}`           | Modifie une tâche (champs partiels OK) |
| `DELETE` | `/api/tasks/{id}`           | Supprime une tâche                     |
| `GET`    | `/api/stats`                | Statistiques par statut                |
| `GET`    | `/`                         | Sert le frontend (`web/index.html`)    |

Corps JSON pour POST/PUT :
```json
{
  "title": "Nom de la tâche",
  "description": "Détails optionnels",
  "dueDate": "2026-06-15",
  "status": "TODO"
}
```

---

## Interface web

Fonctionnalités du frontend :

- **Stats** en temps réel (total, todo, doing, done)
- **Filtres** par statut + **recherche** live
- **Drag & drop** pour réordonner les tâches
- **Badge RETARD** automatique si la date d'échéance est dépassée
- **Modal** créer/éditer avec raccourcis clavier (`n` pour ouvrir, `Ctrl+Enter` pour valider, `Échap` pour fermer)
- **Toasts** de confirmation pour chaque action
- Zéro dépendance JS externe

---

## Tests

**171 assertions au total, zéro dépendance externe.**

### Tests unitaires - logique métier (102 assertions)

Couvrent `Task` et `TaskManager` : sérialisation JSON, CRUD, persistence, caractères spéciaux, cas limites.

```bash
javac -cp out -d out tests/TaskManagerTest.java
java -cp out taskmanager.TaskManagerTest
```

### Tests d'intégration - API HTTP (69 assertions)

Démarrent un vrai serveur HTTP sur un port libre et envoient de vraies requêtes HTTP.
Couvrent tous les endpoints REST, CORS, codes d'erreur, et des scénarios end-to-end.

```bash
javac -cp out -d out tests/ApiServerTest.java
java -cp out taskmanager.ApiServerTest
```

### Lancer tous les tests

```bash
javac -d out src/Task.java src/TaskManager.java src/ConsoleUI.java src/ApiServer.java src/Main.java
javac -cp out -d out tests/TaskManagerTest.java tests/ApiServerTest.java
java -cp out taskmanager.TaskManagerTest && java -cp out taskmanager.ApiServerTest
```

Retourne un code de sortie 0 si tous les tests passent, 1 sinon (compatible CI).

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
> mais affichée au format français (JJ/MM/AAAA) dans le terminal et sur l'interface web.
