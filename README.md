[![Donate](https://img.shields.io/badge/Donate-PayPal-00457C?style=for-the-badge&logo=paypal)](https://paypal.me/DanielAlvarez45767)
﻿<div align="center">

# ⚡ CleanPulse

### Next-Gen Adaptive Server Health & Entity Optimizer for Paper / Spigot 1.21+

[![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen?style=for-the-badge)](https://github.com/dr8553097-sudo/CleanPulse)
[![Platform](https://img.shields.io/badge/Paper-1.21.x-blue?style=for-the-badge&logo=papermc&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-GPL--3.0-purple?style=for-the-badge)](LICENSE)

---

</div>

## 📖 Overview

**CleanPulse** is an enterprise-grade server performance optimizer, entity manager, and diagnostic suite built natively for **Paper 1.21+** and **Java 21**.

Unlike legacy cleaners from 2014, CleanPulse operates with **micro-batched asynchronous pulses** that never freeze the main tick thread, provides an **in-game visual lag heatmap with particles**, protects player death drops with an **immunity shield**, prevents **redstone lag machine abuses**, and offers player-centric utilities like \/trash\ and auto-mining filters.

---

## ✨ Key Features

- ⚡ **Adaptive Pulse Engine:** Zero-lag asynchronous cleanup that dynamically schedules based on live server TPS.
- 🛡️ **Death Grace Shield:** Protects all dropped items from player deaths with a 5-10 minute immunity window.
- 🔍 **Visual Lag Inspector (\/cp inspect\):** Marks problematic hoppers, dense mob clusters, and heavy blocks with color-coded glowing particle rings.
- 🧨 **Redstone & Machine Guard:** Automatically detects and freezes rapid piston clocks and prevents minecart over-stacking.
- 🗑️ **Interactive \/trash\ GUI:** Clean disposal interface with safety confirmation.
- 📱 **Discord Webhooks:** Real-time embed notifications for TPS drops and performance diagnostics.
- 🌐 **Multi-Language:** Native Spanish (\es\), English (\en\), French (\r\), and Portuguese (\pt\).

---

## 💻 Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| \/cp\ o \/cleanpulse\ | Master help and status menu | \cleanpulse.admin\ |
| \/cp pulse\ | Execute manual optimization pulse | \cleanpulse.pulse\ |
| \/cp inspect\ | Activate visual lag particle heatmap | \cleanpulse.inspect\ |
| \/cp monitor\ | View live TPS, RAM and chunk load | \cleanpulse.monitor\ |
| \/cp reload\ | Reload configuration files | \cleanpulse.admin\ |
| \/trash\ | Open interactive disposal trash bin | \cleanpulse.trash\ |

---

Developed with ❤️ by **Dafealru** for the Minecraft server community.
