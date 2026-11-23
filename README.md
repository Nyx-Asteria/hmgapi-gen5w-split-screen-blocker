<div align="center">

# Gen5W Split Screen Blocker

**Your entire screen. Your choice.**

Hide the factory split-screen overlay on compatible Hyundai and Kia Gen5W head units — with an optional side arrow for native navigation.

[![Version](https://img.shields.io/badge/version-1.2.0-3b82f6?style=flat-square)](gen5w-split-screen-blocker/mod.json)
[![HMG-API](https://img.shields.io/badge/HMG--API-%E2%89%A5%201.0.0-2563eb?style=flat-square)](https://github.com/Open-HMG/HMG-API)
[![License: MIT](https://img.shields.io/badge/license-MIT-22c55e?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Gen5W-64748b?style=flat-square)](#compatibility)

[Features](#features) · [Compatibility](#compatibility) · [Installation](#installation) · [Settings](#settings) · [Technical details](#technical-details)

</div>

---

## Overview

The factory Gen5W interface reserves the right-hand side of the display for a split-screen panel. **Gen5W Split Screen Blocker** prevents that overlay from appearing, giving the main interface the full display area.

Prefer keeping the little arrow on the right edge for the car's navigation view? You can enable that independently without restoring the split-screen overlay.

This is a community-maintained fork of [DeFlight's original project](https://github.com/DeFlight/hmgapi-gen5w-split-screen-blocker), built for [HMG-API](https://github.com/Open-HMG/HMG-API).

## Features

- **Full-screen interface** — blocks the factory split-screen OSD while the mod is enabled.
- **Optional navigation arrow** — keep the right-edge arrow for native navigation, without enabling the split-screen overlay.
- **Clean default** — the arrow is hidden unless you explicitly enable it.
- **HMG-API integration** — manage the mod and its setting through the HMG-API app.
- **Firmware safeguards** — hooks run only for recognized `RightScreenHome.odex` signatures.

### How the options behave

| Mod | Enable navigation arrow | Split-screen overlay | Side arrow |
| :--- | :--- | :--- | :--- |
| **On** | **Off** (default) | Blocked | Hidden |
| **On** | **On** | Blocked | Available for native navigation |
| **Off** | Any | Factory behavior | Factory behavior |

> [!NOTE]
> The navigation-only arrow behavior was observed on a **2022 Kia K5** during hook-isolation testing. Behavior on other vehicles has not yet been confirmed. The new fork configuration itself still needs an on-car test.

## Compatibility

### Firmware support

| Vehicle | Software version | Verification |
| :--- | :--- | :--- |
| **2022 Kia K5 (US)** | `DL322.USA.0000.V015.012.260507` | Fifth-hook behavior tested on-car |
| 2023 Kia Sportage Hybrid (US) | `NQ5HEV.USA.0000.V015.012.260507` | Original upstream mod tested; **this fork untested** |
| 2025 Hyundai Ioniq 6 Limited (US) | `CEEV.USA.0000.V015.012.260507` | Original upstream mod tested; **this fork untested** |

The mod checks the MD5 of `/system/app/RightScreenHome.odex` before installing any hooks:

<details>
<summary><strong>Recognized firmware signatures</strong></summary>

| Firmware family | MD5 |
| :--- | :--- |
| Kia K5 (DL322) | `bd97b438aeb3d9c77510b5e2da4234f7` |
| Kia Sportage-family | `7b84ed639e85a711c4a9f2c78e33acf4` |
| Hyundai Ioniq 6 | `cc1bd5131bee221ea3b5ed24a1c1d31a` |

</details>

Other Kia models may share these firmware builds. **A matching vehicle name alone is not enough**: the installed ODEX must match a recognized signature, and the expected methods must be present.

> [!WARNING]
> The fifth hook's exact signature has only been verified on the K5. The Hyundai and Sportage-family firmware may need additional compatibility work. If an expected method is missing, this fork will not install its hooks for that build. Do not treat upstream compatibility reports as proof that this version works on those vehicles.

## Installation

### Requirements

- A compatible **Gen5W** navigation head unit with recognized firmware.
- [HMG-API](https://github.com/Open-HMG/HMG-API) installed, with hooks enabled.
- A signed mod package built for this fork.

### Install with HMG-API

1. Obtain the signed **HMG-API USB** package from this fork's [Releases](../../releases), when available.
2. Extract it to the root of a FAT32 or exFAT USB drive. The resulting path should look like:

   ```text
   HMGAPI/
   └── updates/
       └── gen5w-split-screen-blocker-v<VERSION>.zip
   ```

3. Insert the drive into the head unit and install the mod from HMG-API's updates page.
4. Tap **Apply changes** and restart the head unit.

### Install with NEMESIS

If a **NEMESIS USB** release package is provided, extract it to the root of the USB drive, retaining the `NEMESIS.data` file and `packages` directory. Install using NEMESIS, then restart the head unit as directed.

> [!IMPORTANT]
> **Switching from DeFlight's original mod?** Remove the original before installing this fork. They share a mod ID, and builds signed by different authors are not interchangeable updates. Do not run both versions simultaneously.

## Settings

Open **HMG-API → Mods → Gen5W Split Screen Blocker → Settings**.

| Setting | Default | What it does |
| :--- | :--- | :--- |
| **Enable navigation arrow** | **Off** | Preserves the right-side arrow and its native navigation action while the split-screen overlay remains blocked |

Changes are applied through HMG-API. If you enable the arrow after it was hidden at startup, **restart the head unit** to give the factory interface a chance to recreate it.

To restore the factory split screen entirely, disable the mod in HMG-API and apply the change. A restart may be needed to restore the original interface fully.

## Technical details

This fork uses **three hooks** inside `com.mobis.splitscreen.right`, rather than the original five-hook proposal.

| Hook | Purpose |
| :--- | :--- |
| `f(ZZ)Z` | Suppresses requests to show the slide-out arrow when the arrow setting is off |
| `ij()Z` | Hides the initially created arrow when the arrow setting is off |
| `canShowSSOsd(ZZZ)Z` | Returns `false` for OSD display permission while the mod is enabled |

The original `bh(Z)V` and `show()V` panel hooks were removed. On the K5, isolated testing showed that `canShowSSOsd(ZZZ)Z` alone prevented the split-screen overlay, while leaving the arrow available for native navigation.

**Safety behavior:** The code checks the ODEX hash and expected method signatures before hooking. Unknown firmware is left unchanged. The hooks only override behavior while the mod is enabled; they do not patch system APKs or ODEX files.

### Build from source

The project lives in [`gen5w-split-screen-blocker/`](gen5w-split-screen-blocker/). Its [`build.sh`](gen5w-split-screen-blocker/build.sh) builds the hook JAR, signed mod archive and HMG-API USB package. NEMESIS packages are produced when its packaging helper is provided.

The build requires a JDK with Java 8 release support, Android API 19 `android.jar`, Android build-tools, HMG-API's signing SDK, and **your own author key**. The script reads:

| Variable | Purpose |
| :--- | :--- |
| `ANDROID_JAR` | Android API 19 platform JAR |
| `BUILD_TOOLS` | Android build-tools directory containing `d8` |
| `HMGAPI_CP` | Classpath containing `hmgapi.engine.Signing` |
| `MOD_KEY` | Your HMG-API signing key |
| `MKNEMESISMOD` | Optional path to HMG-API's NEMESIS packaging helper |

```sh
cd gen5w-split-screen-blocker
./build.sh
```

Generated files are written to `out/`. **Never commit signing keys.**

## Feedback and contributions

Found another compatible vehicle or firmware build? Open an [issue](../../issues) with your vehicle, model year, software version, `RightScreenHome.odex` MD5, and the relevant HMG-API log output. Please distinguish **tested behavior** from firmware-map matches.

Bug reports and contributions are welcome, especially for verifying the fifth hook on Hyundai and Sportage-family builds.

## Credits

- **[DeFlight, LLC](https://github.com/DeFlight/hmgapi-gen5w-split-screen-blocker)** — original Split Screen Blocker, foundational hooks, build tooling and documentation.
- **[Open-HMG](https://github.com/Open-HMG/HMG-API)** — HMG-API platform and tooling.
- **[Nyx-Asteria](https://github.com/Nyx-Asteria)** — maintenance of this fork, K5 testing and navigation-arrow configuration.

Released under the **[MIT License](LICENSE)**. Original attribution and license terms are retained.

---

<div align="center">

<sub>Not affiliated with or endorsed by Hyundai, Kia or Genesis. Head-unit modifications are performed at your own risk.</sub>

</div>
