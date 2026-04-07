# BeepMe - Original App Feature Documentation

> **App**: BeepMe - Experience Sampling (ESM/DES)
> **Package**: `com.glanznig.beepme`
> **Author**: Michael Glanznig (2012-2014)
> **License**: GNU GPLv3
> **Last Known Version**: 1.5.5 (build 22), released April 18, 2014
> **Status**: Deprecated / Source code no longer publicly available

## Overview

BeepMe was a free and open-source Android app for conducting **Experience Sampling Method (ESM)** and **Descriptive Experience Sampling (DES)** studies. Once activated, the app sent out beeps at random times throughout the day, prompting users to record their current inner experience. The collected data could be used directly for ESM research or as a basis for follow-up interviews in DES studies.

## Core Features

### 1. Random Beep Notifications
- The app emitted beeps/notifications at **random intervals** throughout the day
- Beep scheduling was controlled by configurable **Timer Profiles**
- Users could activate/deactivate the beeping system on demand

### 2. Sample Recording (at each beep)
When a beep occurred, users were prompted to record their current experience with:
- **Title/Headline**: A short summary of the current experience
- **Notes/Description**: Free-text field for detailed description of the inner experience at the moment of the beep
- **Tags**: Keyword tags to categorize the experience (reusable across samples)
- **Photo**: Capture a photo to accompany the sample entry

### 3. Timer Profiles
- Configurable profiles that controlled **when and how often** beeps occurred
- Managed the randomization of beep intervals
- Supported scheduling beeps within specific time windows (e.g., only during waking hours)

### 4. Uptime Tracking
- Tracked when the beeping system was active vs. inactive
- Maintained an uptime log stored in a dedicated database table (`UptimeTable`)
- Used to calculate statistics about sampling coverage

### 5. Beep History / Sample List
- Chronological list view of all recorded samples
- Ability to browse past entries and review recorded experiences
- Custom list adapters for displaying sample summaries

### 6. Data Export
- **SQLite database export**: The entire dataset could be exported as an SQLite database file
- Export functionality via a dedicated `ExportActivity`
- Data suitable for further analysis in research tools

### 7. Photo Management
- Photos attached to samples were stored locally
- Async image scaling for performance optimization (`AsyncImageScaler`)
- Photo utility helpers for camera integration (`PhotoUtils`)

## App Architecture (from source code analysis)

### Data Layer
| Class | Purpose |
|-------|---------|
| `Sample` | Data model for a single experience sample |
| `TimerProfile` | Data model for beep timer configuration |
| `PreferenceHandler` | Manages app preferences/settings |

### Database Layer
| Table | Purpose |
|-------|---------|
| `SampleTable` | Stores recorded experience samples |
| `SampleTagTable` | Stores tags associated with samples (many-to-many) |
| `ScheduledBeepTable` | Stores upcoming scheduled beep times |
| `TimerProfileTable` | Stores timer profile configurations |
| `UptimeTable` | Tracks when the app's beeping was active |
| `StorageHandler` | Database helper / connection management |

### View Layer
| Activity | Purpose |
|----------|---------|
| `MainActivity` | Main app screen / dashboard |
| `BeepActivity` | Shown when a beep fires; prompts user to record experience |
| `ExportActivity` | Data export interface |

### Helpers
| Class | Purpose |
|-------|---------|
| `AsyncImageScaler` | Background image resizing for photos |
| `PhotoUtils` | Camera/photo integration utilities |

## Localization

The app was available in 6 languages:
- English (en)
- Deutsch / German (de)
- Francais / French (fr)
- Nederlands / Dutch (nl)
- Romana / Romanian (ro)
- Russian (ru)

## Permissions (Inferred)

Based on features, the app likely required:
- **Camera**: For capturing photos with samples
- **Storage**: For saving photos and database exports
- **Vibrate**: For beep notifications
- **Wake Lock / Alarm**: For scheduling beeps at random intervals
- **Notifications**: For alerting the user when a beep fires

## User Workflow

1. **Setup**: User configures a timer profile (beep frequency, active hours)
2. **Activate**: User starts the beeping system
3. **Beep occurs**: At a random time, the app sends a notification/beep
4. **Record**: User opens the app and records their current experience (title, notes, tags, photo)
5. **Review**: User can browse their history of recorded samples
6. **Export**: User exports their data as an SQLite database for analysis

## Research Context

- **ESM (Experience Sampling Method)**: Data collected directly via the app for quantitative analysis of daily experiences
- **DES (Descriptive Experience Sampling)**: Data used as prompts/anchors for follow-up qualitative interviews about inner experiences

## Sources

- [F-Droid Package Page](https://f-droid.org/packages/com.glanznig.beepme/)
- [Fossdroid Listing](https://fossdroid.com/a/beepme.html)
- [Source Code References (java2s.com)](http://www.java2s.com/example/java-src/pkg/com/glanznig/beepme/beeperapp-aaefe.html)
- [Source Code References (javatips.net)](https://www.javatips.net/api/beepme-master/src/main/java/com/glanznig/beepme/db/UptimeTable.java)
- [Tutonaut Review](https://www.tutonaut.de/en/tipp-nutzerverhalten-per-experience-sampling-unter-android-untersuchen/)
