# MEDITRACK

A medication tracking app for Android and Wear OS, built with Kotlin and Jetpack Compose.

## Overview

MEDITRACK helps users stay on top of their medication schedules. The phone companion app and the Wear OS app stay synchronized in real time, so users can manage and confirm their medications directly from their wrist.

## Features

- **Medication dashboard** – view today's schedule grouped by frequency, with at-a-glance adherence percentage
- **Status tracking** – mark each medication as Taken, Snoozed, or Missed
- **Reminder screen** – full-screen reminder dialog with one-tap Taken / Snooze / Missed actions
- **Add medications** – log a new medication with name, dosage, frequency, scheduled times, instructions, and date range
- **Wear OS companion** – dedicated watch app that mirrors the phone's medication list and lets users update statuses from their wrist
- **Real-time sync** – phone and watch stay in sync via the Wearable Data API

## Project Structure

```
wear-project/
├── app/      # Android phone application
├── wear/     # Wear OS watch application
└── shared/   # Common data models and utilities (Medication, MedicationRepository, serialization)
```

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Wear UI | Compose for Wear OS |
| Data sync | Play Services Wearable API |
| Build | Gradle (Kotlin DSL) |
| Min SDK (phone) | API 24 |
| Min SDK (watch) | API 30 |

## Authors

Jonas and Arturo Boss

