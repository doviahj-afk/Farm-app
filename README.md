# Farm Manager

A complete, original Android app for managing a poultry/livestock farm — flocks, egg production,
feeding, health records, income & expenses, reminders, and reports. All features are unlocked;
there's no paywall, subscription, or licensing gate anywhere in this codebase.

## Tech stack
- **Kotlin** + **Jetpack Compose** (Material 3) for the UI
- **Room** for local persistence (SQLite under the hood)
- **Navigation Compose** for the bottom-nav + screen graph
- **ViewModel + Kotlin Flow** for state management (MVVM)
- **KSP** for Room's annotation processing

## Features
- **Dashboard** — live totals for bird count, monthly eggs, income, expenses, and feed cost
- **Flocks** — add/view/delete flocks (name, breed, quantity, acquisition cost & date, **age**, cage count)
- **Egg Production** — log daily collection + breakage, **per flock and per cage (1-80)**
- **Feed Management** — log feed type, quantity (kg), and cost per flock
- **Health Records** — vaccinations, medications, disease events, checkups per flock
- **Income & Expenses** — categorized transaction ledger
- **Reminders** — real system alarms & notifications (like Google Calendar), with optional
  daily/weekly/monthly repeat, surviving app restarts and device reboots
- **Reports** — Overview, **Daily/Monthly/Yearly egg totals**, and **per-cage breakdown**
- **Backup & Export** — CSV and Excel (.xlsx) export of egg records, full JSON backup/restore,
  and optional **Google Drive cloud sync**

## Project structure
```
app/src/main/java/com/farmmanager/app/
├── data/
│   ├── entity/        Room entities (Flock, EggRecord, FeedRecord, HealthRecord, TransactionEntity, Reminder, Note)
│   ├── dao/            Room DAOs
│   ├── repository/     FarmRepository — single source of truth used by all ViewModels
│   └── AppDatabase.kt  Room database + Converters + migrations
├── receiver/            ReminderAlarmReceiver (fires notifications), BootReceiver (reschedules after reboot)
├── ui/
│   ├── dashboard/       Home screen + ViewModel
│   ├── flock/           Flock list/add + ViewModel (age, cage count)
│   ├── egg/             Egg records + ViewModel (per-cage logging)
│   ├── feed/            Feed records + ViewModel
│   ├── health/          Health records + ViewModel
│   ├── transaction/     Income/expense ledger + ViewModel
│   ├── reminder/        Reminders + ViewModel (real alarms, repeat)
│   ├── reports/         Daily/Monthly/Yearly/By-Cage reports screen
│   ├── backup/          Export (CSV/XLSX), full backup/restore, Google Drive sync
│   ├── more/            Hub screen for Feed/Health/Transactions/Reminders/Backup
│   ├── navigation/      Bottom nav + NavHost wiring
│   └── theme/           Compose Material 3 theme (colors, type)
├── util/                DateUtils, ExportUtils, BackupJson, AlarmScheduler, NotificationHelper,
│                        GoogleDriveBackupManager, ViewModelFactory
├── FarmApp.kt           Application class, owns the Room DB + repository singleton
└── MainActivity.kt      Entry point — requests notification permission on Android 13+
```

## How to open and run it
1. Install **Android Studio** (Koala/2024.1 or newer recommended).
2. Open this folder (`FarmManager/`) as a project — Android Studio will detect the Gradle
   project automatically via `settings.gradle.kts`.
3. Let Gradle sync (it will pull dependencies for Compose, Room, Navigation, Play Services Auth, etc.).
4. Run on an emulator or physical device (minSdk 24 / Android 7.0+).

The app works fully offline with local backup/export — **no setup required** for CSV/Excel export
or JSON backup/restore. Google Drive cloud sync is optional and needs a one-time setup below.

### Optional: enabling Google Drive cloud sync
The Drive sync feature talks to the Drive REST API directly (no heavy client library), but Google
requires you to register your own OAuth client before sign-in will work:
1. Go to [Google Cloud Console](https://console.cloud.google.com), create or pick a project.
2. Under **APIs & Services → Library**, enable the **Google Drive API**.
3. Under **APIs & Services → Credentials → Create Credentials → OAuth client ID**, choose
   **Android** as the application type.
4. Set the package name to `com.farmmanager.app`.
5. Get your signing certificate's SHA-1: run `./gradlew signingReport` in the project root (or
   `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android`
   for the debug build), and paste that SHA-1 into the OAuth client form.
6. Save. No `google-services.json` download is needed — the OAuth client tied to your package name
   + SHA-1 is enough for this REST-based sign-in flow.
7. Rebuild the app. The "Sign in with Google" button under Backup & Export will now work, and will
   back up/restore a single JSON file to a private "app data" folder in the user's Drive
   (invisible in their regular Drive UI, deleted if the app is uninstalled).

If you skip this setup, everything else in the app (including local backup/restore and CSV/Excel
export) works exactly the same — only the Drive buttons will fail with a sign-in error.

## Extending it
Everything is modular by feature package, so adding a new module (e.g. "Notes", which already has
an entity/DAO stubbed out but no UI yet) follows the same pattern as the others:
1. Add a `ViewModel` that exposes a `StateFlow` from the repository.
2. Add a Composable screen with a list + FAB-triggered add dialog.
3. Wire it into `AppNavigation.kt` (or into `MoreScreen.kt` if it's a secondary feature).
