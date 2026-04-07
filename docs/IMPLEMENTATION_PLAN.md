# BeepMeToo - Implementation Plan

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room (SQLite abstraction)
- **Scheduling**: AlarmManager + WorkManager (for reliable background beeps)
- **Architecture**: MVVM with repository pattern
- **DI**: Hilt
- **Navigation**: Compose Navigation
- **Camera**: CameraX / ActivityResultContracts
- **Min SDK**: 26 (Android 8.0) — covers 95%+ of active devices
- **Target SDK**: 34

## Project Structure

```
app/src/main/java/com/beepmetoo/
├── BeepMeTooApp.kt              # Application class (Hilt entry point)
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt       # Room database definition
│   │   ├── dao/
│   │   │   ├── SampleDao.kt
│   │   │   ├── TagDao.kt
│   │   │   ├── TimerProfileDao.kt
│   │   │   ├── ScheduledBeepDao.kt
│   │   │   └── UptimeDao.kt
│   │   └── entity/
│   │       ├── Sample.kt
│   │       ├── Tag.kt
│   │       ├── SampleTagCrossRef.kt
│   │       ├── TimerProfile.kt
│   │       ├── ScheduledBeep.kt
│   │       └── UptimeEntry.kt
│   ├── repository/
│   │   ├── SampleRepository.kt
│   │   ├── TimerProfileRepository.kt
│   │   └── UptimeRepository.kt
│   └── export/
│       └── DataExporter.kt
├── service/
│   ├── BeepScheduler.kt         # Schedules random beeps via AlarmManager
│   ├── BeepReceiver.kt          # BroadcastReceiver that fires on alarm
│   └── BeepForegroundService.kt # Foreground service for uptime tracking
├── ui/
│   ├── theme/
│   │   └── Theme.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   ├── home/
│   │   ├── HomeScreen.kt        # Dashboard: toggle beeping, see status
│   │   └── HomeViewModel.kt
│   ├── record/
│   │   ├── RecordScreen.kt      # Record a sample (title, notes, tags, photo)
│   │   └── RecordViewModel.kt
│   ├── history/
│   │   ├── HistoryScreen.kt     # Browse past samples
│   │   └── HistoryViewModel.kt
│   ├── detail/
│   │   ├── DetailScreen.kt      # View/edit a single sample
│   │   └── DetailViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt    # Timer profile config, export
│   │   └── SettingsViewModel.kt
│   └── export/
│       ├── ExportScreen.kt
│       └── ExportViewModel.kt
└── util/
    ├── PhotoManager.kt          # Save/load/scale photos
    └── NotificationHelper.kt    # Notification channel + builder
```

## Phases

### Phase 1: Project Scaffolding
- Initialize Android project with Kotlin + Jetpack Compose
- Configure Gradle dependencies (Room, Hilt, Compose, Navigation, CameraX)
- Set up Hilt application class
- Create Material 3 theme
- Create navigation graph with placeholder screens

### Phase 2: Database Layer
- Define Room entities: `Sample`, `Tag`, `SampleTagCrossRef`, `TimerProfile`, `ScheduledBeep`, `UptimeEntry`
- Define DAOs with queries for CRUD + relationships
- Create `AppDatabase` with migrations strategy
- Build repository classes

**Entities:**

```
Sample
  - id: Long (PK, auto)
  - title: String
  - description: String?
  - photoPath: String?
  - timestamp: Long (epoch ms — when beep occurred)
  - createdAt: Long (epoch ms — when user submitted)

Tag
  - id: Long (PK, auto)
  - name: String (unique)

SampleTagCrossRef
  - sampleId: Long (FK)
  - tagId: Long (FK)

TimerProfile
  - id: Long (PK, auto)
  - name: String
  - isActive: Boolean
  - beepsPerDay: Int (approximate target)
  - startHour: Int (0-23, window start)
  - startMinute: Int (0-59)
  - endHour: Int (0-23, window end)
  - endMinute: Int (0-59)

ScheduledBeep
  - id: Long (PK, auto)
  - scheduledTime: Long (epoch ms)
  - timerProfileId: Long (FK)
  - fired: Boolean

UptimeEntry
  - id: Long (PK, auto)
  - startTime: Long (epoch ms)
  - endTime: Long? (epoch ms, null if still active)
```

### Phase 3: Beep Scheduling Engine
- Implement `BeepScheduler`: given a `TimerProfile`, compute random beep times within the active window, spread across the day
- Use `AlarmManager.setExactAndAllowWhileIdle()` for each scheduled beep (exact timing matters for ESM)
- `BeepReceiver` receives alarm, posts notification, logs to `ScheduledBeep` table
- Re-schedule beeps on device boot via `BOOT_COMPLETED` receiver
- Handle Android 12+ exact alarm permission (`SCHEDULE_EXACT_ALARM`)

**Randomization algorithm:**
1. Divide the active window into N equal slots (N = beepsPerDay)
2. Pick one random time within each slot
3. Schedule an exact alarm for each

### Phase 4: Notification + Beep UI
- Create notification channel (high priority, with sound + vibration)
- `BeepReceiver` posts a notification with a full-screen intent to `RecordScreen`
- Tapping notification opens `RecordScreen` pre-filled with the beep timestamp
- If user dismisses notification, sample is recorded as "missed" (no data)

### Phase 5: Sample Recording Screen
- Title text field
- Description multiline text field
- Tag chips: show existing tags, allow adding new ones
- Photo button: launch camera, save scaled image, show thumbnail
- Save button: persist `Sample` + tag associations to Room
- Cancel / skip button

### Phase 6: History Screen
- LazyColumn of samples, grouped by day
- Each item shows: timestamp, title, tag chips, photo thumbnail
- Tap to open detail view
- Detail view allows editing title/description/tags (not photo — keep it simple)

### Phase 7: Home Screen (Dashboard)
- Big toggle: Start / Stop beeping
- Current status: active timer profile name, next scheduled beep time
- Today's stats: beeps received, samples recorded, acceptance rate
- Quick link to history

### Phase 8: Settings + Timer Profile
- Timer profile editor: name, beeps per day, active hours window
- Support multiple profiles (only one active at a time)
- App preferences: notification sound, vibration on/off

### Phase 9: Data Export
- Export as SQLite `.db` file (copy Room database)
- Export as CSV (samples + tags flattened)
- Share via Android share sheet (email, cloud storage, etc.)
- Include photos as a zip option

### Phase 10: Uptime Tracking
- Log start/stop times when user toggles beeping
- Track via `UptimeEntry` table
- Show total uptime stats in settings or a simple stats view

### Phase 11: Polish + Edge Cases
- Handle `BOOT_COMPLETED` to reschedule alarms after reboot
- Handle Doze mode / battery optimization (guide user to exempt app)
- Handle notification permission (Android 13+)
- Handle exact alarm permission (Android 12+)
- Proper lifecycle handling (don't lose draft sample on rotation/backgrounding)
- Empty states for history, tags

## Build Order (Dependency Graph)

```
Phase 1 (scaffolding)
  └─> Phase 2 (database)
        ├─> Phase 3 (scheduling) ─> Phase 4 (notifications)
        ├─> Phase 5 (recording)
        └─> Phase 6 (history)
      Phase 7 (home) depends on Phase 3 + 5
      Phase 8 (settings) depends on Phase 3
      Phase 9 (export) depends on Phase 2
      Phase 10 (uptime) depends on Phase 3
      Phase 11 (polish) last
```

## Key Design Decisions

1. **Jetpack Compose over XML layouts** — Modern, less boilerplate, better for a greenfield project
2. **Room over raw SQLite** — Type-safe queries, compile-time verification, easy migrations
3. **AlarmManager over WorkManager for beeps** — ESM requires exact timing; WorkManager is for deferrable work
4. **Single active timer profile** — Keeps UX simple; matches original app behavior
5. **Min SDK 26** — Notification channels required (API 26), and drops very old devices with minimal user loss
6. **CSV export in addition to SQLite** — More accessible for researchers who use Excel/R/SPSS
