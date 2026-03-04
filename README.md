# 🔥 Exam Fever Planner

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" alt="Exam Fever Planner Icon"/>
</p>

<p align="center">
  <strong>Your all-in-one exam preparation companion for Android</strong><br/>
  Stay organised. Study smarter. Never miss a deadline.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?logo=android" />
  <img src="https://img.shields.io/badge/Language-Java-orange?logo=java" />
  <img src="https://img.shields.io/badge/Min%20SDK-26-blue" />
  <img src="https://img.shields.io/badge/License-MIT-lightgrey" />
</p>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Permissions](#permissions)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

**Exam Fever Planner** is a productivity Android application designed specifically for students. It helps you manage your exam timetable, organise study materials, track your to-do list with reminders, and structure your revision by subject and topic — all in one place.

---

## ✨ Features

### 📌 To-Do List
- Add tasks with a **title** and **description**
- Set a **specific date and time** for each task
- Receive an **exact-time push notification** reminder
- Mark tasks as completed (with strikethrough effect)
- **Delete** individual tasks (alarm is also cancelled automatically)
- Swipe-to-delete support

### 📅 Exam Timetable
- Add upcoming exams with **subject, date, time, and venue/location**
- Live **countdown** showing days remaining until each exam
- Optional **notification reminder** one day before the exam
- **Delete** exam entries with confirmation

### 📚 Study Materials
- Add materials by **URL** (web link) or **local file** (picked from phone storage)
- Tap any entry to open the URL in a browser or the file in the appropriate app
- **Delete** materials with confirmation
- Persistent URI permissions for local files

### 🎓 Subjects & Topics
- Add subjects with auto-assigned **colour coding**
- Tap a subject to manage its **topics**
- Mark topics as **completed** (with checkbox + strikethrough)
- **Delete** subjects or individual topics with confirmation

### 🔔 Smart Notifications
- Notifications fire at the **exact time and day** you set
- Uses `AlarmManager.setExactAndAllowWhileIdle` for precise delivery
- Two notification channels: **Task Reminders** and **Exam Alerts**
- Proper runtime permission request on Android 13+

---

## 📸 Screenshots

> _Screenshots will be added here._

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with a Repository pattern:

```
UI Layer (Activities / Adapters)
        │
        ▼
ViewModel Layer (AndroidViewModel)
        │
        ▼
Repository Layer
        │
        ▼
Room Database (DAOs + Entities)
```

### Notification Pipeline

```
User sets time  →  AlarmManager schedules exact alarm
                          │
                    Alarm fires
                          │
                   AlarmReceiver (BroadcastReceiver)
                          │
               NotificationHelper posts notification
```

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Architecture | MVVM + Repository Pattern |
| Database | Room Persistence Library |
| Async | ExecutorService (background threads) |
| UI | Material Design Components |
| Notifications | NotificationManager + AlarmManager |
| File Access | Storage Access Framework (`ACTION_OPEN_DOCUMENT`) |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 34 (Android 14) |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 8+
- Android device or emulator running **Android 8.0 (API 26)+**

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/fussy0064/ExamFeverPlanner.git

# Open in Android Studio
File → Open → ExamFeverPlanner/

# Sync Gradle and run on your device
Run → Run 'app'
```

> **Note:** The first run will clear any existing database if the schema has changed (destructive migration is enabled for development convenience).

---

## 🔐 Permissions

The app requests the following permissions:

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Show task and exam reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Fire alarms at precisely the user-selected time |
| `USE_EXACT_ALARM` | Backup exact alarm permission (Android 12+) |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule alarms after device reboot *(planned)* |
| `READ_EXTERNAL_STORAGE` | Browse and attach study material files |

---

## 📁 Project Structure

```
app/src/main/java/com/fussy/examfeverplanner/
│
├── data/
│   ├── database/        # AppDatabase (Room)
│   ├── dao/             # DAOs (TaskDao, ExamDao, SubjectDao, TopicDao, StudyMaterialDao)
│   └── entity/          # Entities (Task, Exam, Subject, Topic, StudyMaterial)
│
├── repository/          # Repositories (TaskRepository, ExamRepository, …)
│
├── viewmodel/           # ViewModels (TaskViewModel, ExamViewModel, …)
│
├── ui/                  # Activities & Adapters
│   ├── MainActivity.java
│   ├── TaskActivity.java / TaskAdapter.java
│   ├── ExamActivity.java / ExamAdapter.java
│   ├── MaterialActivity.java / MaterialAdapter.java
│   ├── SubjectsActivity.java / SubjectAdapter.java
│   └── TopicsActivity.java / TopicAdapter.java
│
└── utils/
    ├── NotificationHelper.java   # Channel creation & alarm scheduling
    └── AlarmReceiver.java        # BroadcastReceiver for notifications

app/src/main/res/
├── layout/              # XML layouts for activities and dialogs
├── mipmap-*/            # App launcher icons (all densities)
└── drawable/            # ic_notification.xml and other drawables
```

---

## 🤝 Contributing

Contributions, issues and feature requests are welcome!

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

---

## 📄 License

```
MIT License

Copyright (c) 2026 fussy0064

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<p align="center">Made with ❤️ for students who hustle 🔥</p>
