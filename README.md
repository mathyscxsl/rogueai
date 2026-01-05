# 🎮 Rogue AI Override

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-1.5.3-green.svg)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-7.0%2B-brightgreen.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Shout orders. Save the world.**  
> Jeu coopératif multijoueur Android inspiré de Spaceteam

---

## 📋 Table des matières

- [À propos](#-à-propos)
- [Fonctionnalités](#-fonctionnalités)
- [Technologies](#-technologies)
- [Architecture](#-architecture)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Utilisation](#-utilisation)
- [Structure du projet](#-structure-du-projet)
- [Game Design](#-game-design)
- [API & WebSocket](#-api--websocket)
- [Captures d'écran](#-captures-décran)
- [Auteur](#-auteur)

---

## 🎯 À propos

**Rogue AI Override** est un jeu coopératif multijoueur (2-6 joueurs) sur Android où vous devez empêcher une IA hostile de dominer le monde. Chaque joueur reçoit des instructions qu'il ne peut pas toujours exécuter seul : la communication vocale entre joueurs est essentielle pour identifier qui possède quel contrôle et agir à temps.

### Concept

- **Durée** : 45 secondes
- **Objectif** : Maintenir la menace mondiale sous 100%
- **Challenge** : Instructions avec vocabulaire technique absurde, timers dégressifs, pression croissante

---

## ✨ Fonctionnalités

### Gameplay

- ✅ **Multijoueur temps réel** (2-6 joueurs via WebSocket)
- ✅ **Mode solo** pour entraînement
- ✅ **Communication vocale** obligatoire (pas de chat texte)
- ✅ **Difficulté progressive** (timers de 20s → 14s)
- ✅ **Jauge de menace dynamique** avec code couleur
- ✅ **Instructions aléatoires** avec vocabulaire absurde

### Technique

- ✅ **Architecture MVVM** propre et testable
- ✅ **Jetpack Compose** (UI déclarative)
- ✅ **WebSocket** pour latence minimale (~100-200ms)
- ✅ **StateFlow** pour réactivité UI
- ✅ **Coroutines** pour opérations asynchrones
- ✅ **Design cyberpunk** immersif avec animations

### Audio

- ✅ Musique de fond (loop)
- ✅ Sons d'actions (succès/échec)
- ✅ Sons de victoire/défaite
- ✅ Gestion pause/reprise automatique

---

## 🛠️ Technologies

### Frontend (Android)

| Technologie           | Version | Usage                    |
| --------------------- | ------- | ------------------------ |
| **Kotlin**            | 1.9.20  | Langage principal        |
| **Jetpack Compose**   | 1.5.3   | UI déclarative           |
| **Material Design 3** | Latest  | Design system            |
| **OkHttp**            | 4.12.0  | Client HTTP + WebSocket  |
| **Moshi**             | 1.15.0  | Parsing JSON (avec KSP)  |
| **Coroutines**        | 1.7.3   | Programmation asynchrone |
| **StateFlow**         | Latest  | Gestion d'état réactif   |

### Backend (fourni)

- **API REST** : Création/vérification salles
- **WebSocket** : Communication temps réel
- **URL** : `wss://backend.rogueai.surpuissant.io`

---

## 🏗️ Architecture

### Pattern MVVM

```
┌─────────────────────────────────────────────────┐
│                     VIEW                        │
│              (Jetpack Compose)                  │
│  • Affiche les données                          │
│  • Capture les événements                       │
│  • Observe StateFlow                            │
└───────────────────┬─────────────────────────────┘
                    │
                    │ observe / call
                    ▼
┌─────────────────────────────────────────────────┐
│                  VIEWMODEL                      │
│  • Logique métier                               │
│  • Gère l'état (StateFlow)                      │
│  • Survit aux rotations                         │
│  • Transforme données → UI state                │
└───────────────────┬─────────────────────────────┘
                    │
                    │ calls
                    ▼
┌─────────────────────────────────────────────────┐
│              REPOSITORY / DATA                  │
│  • GameRepository (REST API)                    │
│  • RoomSocket (WebSocket)                       │
│  • Parsing JSON (Moshi)                         │
└───────────────────┬─────────────────────────────┘
                    │
                    ▼
              [BACKEND SERVER]
```

### Cycle de vie Activity

```
onCreate()  → Init (sons, socket, repos)
onResume()  → Reprise musique
onPause()   → Pause musique
onDestroy() → Cleanup (fermeture socket, libération sons)
```

### Flow de données temps réel

```
USER ACTION
    ↓
View.onClick()
    ↓
ViewModel.executeAction()
    ↓
Socket.sendExecuteAction()
    ↓
[WebSocket] → Backend
    ↓
Backend répond
    ↓
Socket.playerBoard (Flow)
    ↓
ViewModel observe → StateFlow emit
    ↓
View collectAsState → Recompose
```

---

## 📦 Installation

### Prérequis

- **Android Studio** : Arctic Fox (2020.3.1) ou plus récent
- **JDK** : 17
- **Android SDK** : API 24 (Android 7.0) minimum
- **Gradle** : 8.0+

### Étapes

1. **Cloner le repository**

```bash
git clone https://github.com/votre-username/rogue-ai-override.git
cd rogue-ai-override
```

2. **Ouvrir dans Android Studio**

```
File > Open > Sélectionner le dossier du projet
```

3. **Sync Gradle**

```
File > Sync Project with Gradle Files
```

4. **Ajouter les fichiers audio** (optionnel)

```
Placer vos fichiers .mp3 dans :
app/src/main/res/raw/
```

Fichiers attendus :

- `player_joined.mp3`
- `player_ready.mp3`
- `game_start.mp3`
- `background_music.mp3`
- `correct_action.mp3`
- `wrong_action.mp3`
- `victory.mp3`
- `defeat.mp3`

5. **Build & Run**

```
Run > Run 'app'
```

---

## 🎮 Utilisation

### Créer une partie

1. Lancer l'application
2. Choisir **"Mode solo"** (optionnel)
3. Appuyer sur **"CRÉER UNE MISSION"**
4. Noter le code de la salle (6 caractères)
5. Partager le code avec vos amis

### Rejoindre une partie

1. Lancer l'application
2. Appuyer sur **"REJOINDRE UNE MISSION"**
3. Entrer le code de salle
4. Attendre que tous les joueurs soient prêts

### Pendant le jeu

1. **Lire l'instruction** affichée à voix haute
2. **Communiquer** : "Qui a le Taux de compression ?"
3. **Exécuter l'action** sur le bon panneau
4. **Gérer le stress** : timer réduit progressivement
5. **Objectif** : Survivre 3 minutes avec menace < 100%

---

## 📁 Structure du projet

```
app/src/main/java/com/rogueai/
├── data/
│   ├── GameRepository.kt          # REST API calls
│   ├── LobbyRepository.kt         # (optionnel)
│   └── GamePlayRepository.kt      # (optionnel)
│
├── model/
│   ├── GameModels.kt              # Command, Board, Instruction
│   ├── LobbyModels.kt             # Player, RoomInfo
│   └── RoomModels.kt              # CreateRoomRequest/Response
│
├── network/
│   ├── RoomsApi.kt                # REST client (OkHttp)
│   └── RoomSocket.kt              # WebSocket client
│
├── ui/
│   ├── common/
│   │   └── SfxManager.kt          # Gestionnaire de sons
│   │
│   ├── game/
│   │   ├── components/
│   │   │   ├── GameScreen.kt     # Écran de jeu
│   │   │   ├── GameViewModel.kt  # Logique jeu
│   │   │   └── GameViewModelFactory.kt
│   │   └── gameover/
│   │       └── GameOverScreen.kt # Écran fin de partie
│   │
│   ├── home/
│   │   ├── HomeScreen.kt          # Écran d'accueil
│   │   ├── HomeViewModel.kt       # Logique accueil
│   │   └── HomeViewModelFactory.kt
│   │
│   ├── lobby/
│   │   ├── LobbyScreen.kt         # Salle d'attente
│   │   ├── LobbyViewModel.kt      # Logique lobby
│   │   └── LobbyViewModelFactory.kt
│   │
│   └── theme/
│       └── Theme.kt               # Thème Material
│
└── MainActivity.kt                # Point d'entrée

app/src/main/res/
└── raw/                           # Fichiers audio (.mp3)
```

---

## 🎨 Game Design

### Palette cyberpunk

```kotlin
Background   : #0A0E27  // Bleu nuit
Surface      : #1E1E2E  // Gris anthracite
Primary      : #6200EE  // Violet néon
Secondary    : #03DAC6  // Cyan néon
Success      : #00FF41  // Vert Matrix
Error        : #FF1744  // Rouge alerte
Warning      : #FF9800  // Orange
```

### Mécaniques de tension

**Jauge de menace**

- 0-30% : 🟢 Sous contrôle
- 30-60% : 🟡 Attention
- 60-80% : 🟠 Danger
- 80-100% : 🔴 CRITIQUE (pulsation)

**Timers dégressifs**

**Règles de menace**

- Succès : -5%
- Timeout : +8%
- Mauvaise action : +5% (wrong target) ou +3% (wrong params)
- Victoire : 45s survécues avec menace < 100%

---

## 🌐 API & WebSocket

### REST API

**Base URL** : `https://backend.rogueai.surpuissant.io`

**Endpoints**

```
POST   /create-room
GET    /room-exists/{code}
GET    /health
```

**Exemple création room**

```json
POST /create-room
Body: {
  "gameType": "toggle",
  "soloGame": false
}

Response: {
  "roomCode": "H361PZ",
  "roomInfo": {
    "minPlayer": 2,
    "maxPlayer": 6,
    "gameDuration": 180000,
    "roomRestriction": "ToggleCommand"
  }
}
```

### WebSocket

**URL** : `wss://backend.rogueai.surpuissant.io/?room={roomCode}`

**Messages reçus**

```json
// État du jeu
{ "type": "game_state", "payload": { "state": "lobby_waiting" } }

// Informations salle
{
  "type": "room_info",
  "payload": {
    "you": { "id": "...", "name": "...", "ready": false },
    "players": [...],
    "room_state": "ready"
  }
}

// Panneau joueur
{
  "type": "player_board",
  "payload": {
    "board": { "commands": [...] },
    "instruction": {...},
    "threat": 30
  }
}
```

**Messages envoyés**

```json
// Prêt
{ "type": "room", "payload": { "ready": true } }

// Changer nom
{ "type": "refresh_name" }

// Exécuter action
{
  "type": "execute_action",
  "payload": {
    "command_id": "cross_validation",
    "action": "toggle"
  }
}
```

---

## 📸 Captures d'écran

### Écran d'accueil

- Toggle mode solo/multi
- Design cyberpunk avec animations
- Boutons néon

### Lobby

- Liste joueurs temps réel
- États prêt/attente
- Compte à rebours animé

### Jeu

- Header menace + timer
- Instruction urgente avec pulsation
- 4 panneaux de contrôle (toggle/slider)

### Game Over

- Victoire/Défaite stylisée
- Statistiques détaillées
- Taux de réussite

---

## 🧪 Tests

### Tests recommandés (à implémenter)

```kotlin
// ViewModels
@Test
fun `threat should increase on wrong action`() {
    val viewModel = GameViewModel(mockSocket)
    viewModel.onActionFailed()
    assertEquals(33, viewModel.uiState.value.threat)
}

@Test
fun `threat should not exceed 100`() {
    val viewModel = GameViewModel(mockSocket)
    repeat(20) { viewModel.onActionFailed() }
    assertEquals(100, viewModel.uiState.value.threat)
}

// JSON Parsing
@Test
fun `should parse player_board message correctly`() {
    val json = """{"type":"player_board"...}"""
    val result = parseMessage(json)
    assertNotNull(result.board)
}
```

---

<p align="center">
  <strong>Shout orders. Save the world. 🎮</strong>
</p>
