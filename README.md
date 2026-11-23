# Gen5W Split Screen Blocker

An [HMG-API](https://github.com/Open-HMG/HMG-API) mod that removes the right-hand split screen and slide-out tab from compatible Hyundai and Kia Gen5W head units.

The original implementation was confirmed working on the Hyundai Ioniq 6. Expanded compatibility adds additional split-screen suppression and an OEM shutdown method, successfully tested on the Kia K5. Additional firmware families have been verified through off-car analysis but have not been tested with this mod on actual vehicles. See [Compatibility](#compatibility).

The mod is managed through HMG-API. Its settings page provides an enable/disable switch and a diagnostic log, but no additional configurable options or standalone application.

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-0070BA?logo=paypal&logoColor=white)](https://paypal.me/deflight) [![Sponsor on GitHub](https://img.shields.io/badge/Sponsor-GitHub-EA4AAA?logo=githubsponsors&logoColor=white)](https://github.com/sponsors/DeFlight)

## What it does

| On the head unit | With the mod enabled |
|---|---|
| Right-hand split-screen panel | Blocked on verified compatible vehicles; OEM shutdown attempted on other firmware |
| Slide-out tab | Hidden where the Java suppression hooks are effective |
| Other head-unit functionality | Left unchanged by the mod's intended behavior |

The mod operates within the head unit's existing split-screen application and does not replace its launcher or modify system files.

## Turning it on and off

Use the mod's switch in the HMG-API app.

**To turn it off:**

1. Open **HMG-API → Mods → Gen5W Split Screen Blocker**.
2. Switch it off and tap **Apply changes**.
3. Restart the head unit if the split screen does not return immediately.

**To turn it back on:**

1. Switch it on and tap **Apply changes**.
2. Tap **Restart unit**.

Disabling the mod stops further suppression, but an existing OEM shutdown request may have already closed the panel. A restart may be necessary to restore normal behavior.

## Good to know

- **Split-screen settings:** The car's original split-screen settings remain available but may have no effect while suppression is active.
- **Other mods:** Side-panel features provided by other HMG-API mods may become inaccessible while the split screen is blocked.
- **Software updates:** Updating the head unit may require reinstalling HMG-API. The Java hooks require recognized firmware, but the OEM shutdown fallback can still be attempted on unknown builds.
- **Uninstalling:** Removing the mod and restarting the head unit restores normal behavior, provided no other modification is suppressing the split screen.

## Requirements

- A Hyundai or Kia vehicle with a **Gen5W navigation head unit**.
- [HMG-API](https://github.com/Open-HMG/HMG-API) installed with its hook functionality enabled. See [Installing HMG-API](https://github.com/Open-HMG/HMG-API/wiki/Installing-HMG-API).

## Compatibility

The mod uses Java hooks and an OEM Binder shutdown method to suppress the right-hand split screen.

The Java hooks require recognized firmware. The OEM Binder shutdown is attempted independently of the firmware MD5 whitelist, including on unrecognized builds.

### Java hook compatibility

The mod identifies recognized builds using the MD5 hash of `/system/app/RightScreenHome.odex`.

A recognized MD5 allows the Java hooks to be installed, but does not guarantee that they will successfully suppress the entire split-screen interface.

| Year / Make / Model | Firmware | MD5 | Status |
|---|---|---|---|
| 2025 Hyundai Ioniq 6 Limited (US) | `CEEV.USA.0000.V015.012.260507` | `cc1bd5131bee221ea3b5ed24a1c1d31a` | **Tested on-car — Working** |
| 2022 Kia K5 (US) | `DL322.USA.0000.V015.012.260507` | `bd97b438aeb3d9c77510b5e2da4234f7` | **Tested on-car — Insufficient*** |
| Kia Sportage / Sportage Hybrid / Sportage PHEV / Forte / 2022 Carnival / Niro Hybrid / Niro PHEV / Soul / 2024 Seltos / Telluride / Carnival (US) | Kia US `V015.012.260507` (`BDPE`, `KA4`, `NQ5`, `NQ5HEV`, `NQ5PHEV`, `SG2HEV`, `SG2PHEV`, `SK3PE`, `SP2PE`, `ONPE`, `KA423`) | `7b84ed639e85a711c4a9f2c78e33acf4` | **Reported insufficient** on Sportage Hybrid; other models untested |

\* **Insufficient** refers specifically to the original four Java hooks, not the expanded seven-hook implementation. The combined seven-hook and OEM Binder implementation has been confirmed working on the Kia K5. The seven hooks and Binder shutdown have not been independently tested in isolation.

The shared Kia firmware row includes multiple vehicle models identified through HMG-API firmware mappings. A matching MD5 indicates the same recognized split-screen application build, not proof of successful operation on every listed vehicle.

### OEM Binder shutdown compatibility

The OEM Binder method communicates with the head unit's existing split-screen service to request that the panel close.

Unlike the Java hooks, this method **does not require a recognized MD5**. It can be attempted on any firmware exposing the expected OEM service interface.

The following vehicles and firmware families have been identified as candidates for the OEM shutdown method.

| Year / Make / Model | Firmware | Status |
|---|---|---|
| 2022 Kia K5 (US) | `DL322.USA.0000.V015.012.260507` | **Tested on-car — Working (combined implementation)** |
| Kia Sportage / Sportage Hybrid | `V015.012.260507` | Untested with expanded implementation |
| Kia Niro / Niro EV | `V015.012.260507` | OEM interface verified off-car; untested on-car |
| Kia EV6 | `V015.012.260507` | OEM interface verified off-car; untested on-car |
| Kia Sorento | `V015.012.260507` | OEM interface verified off-car; untested on-car |
| Hyundai Ioniq 5 | `V016.010.260903` | OEM interface verified off-car; untested on-car |
| Hyundai Ioniq 6 | `V016.010.260903` | OEM interface verified off-car; untested on-car |
| Hyundai Sonata | `V015.012.260507` | OEM interface verified off-car; untested on-car |

The listed versions are the latest firmware releases identified for each family in the compatibility research. Earlier versions may also work.

**Unlisted firmware is not automatically excluded.** The mod attempts to install its OEM shutdown lifecycle hook even when the ODEX MD5 is unrecognized.

The Binder method is best-effort on untested firmware. It requires the expected OEM service to exist, the lifecycle hook to install successfully, and the service to become ready.

A successful Binder shutdown request does not, by itself, guarantee that the panel cannot reopen or that the slide-out tab will remain hidden.

### Unknown firmware and logging

On recognized firmware, the mod installs its Java suppression hooks and attempts the OEM shutdown.

On unrecognized firmware, the mod skips the firmware-specific Java hooks but still attempts the OEM shutdown fallback.

The detected ODEX MD5 and its recognition status are recorded in the HMG-API diagnostic log when the ODEX file can be read.

If your vehicle or firmware is not listed, please [open a compatibility report](../../issues/new?template=bug-report.yml) with your vehicle model, firmware version, detected MD5, and relevant HMG-API logs.

## Install

Install [HMG-API](https://github.com/Open-HMG/HMG-API/wiki/Installing-HMG-API) first. Then download one of these files from [Releases](../../releases):

| File | For |
|---|---|
| `gen5w-split-screen-blocker-v<version>-hmgapi-usb.zip` | Installing through HMG-API using a USB drive |
| `gen5w-split-screen-blocker-v<version>-nemesis-usb.zip` | Installing through NEMESIS using a USB drive |
| `gen5w-split-screen-blocker-v<version>-nemesis-package.zip` | NEMESIS Package Store or `nemesis install`; not for extracting directly onto a USB drive |
| `gen5w-split-screen-blocker-v<version>.zip` | Standalone signed HMG-API mod |

### With the HMG-API app

1. Extract `gen5w-split-screen-blocker-v<version>-hmgapi-usb.zip` to the root of a FAT32 or exFAT USB drive. The resulting path should be `HMGAPI/updates/gen5w-split-screen-blocker-v<version>.zip`.
2. Connect the drive to the head unit and install the mod from the updates page in HMG-API.
3. Tap **Apply changes**.

To remove it, select **Remove** for Gen5W Split Screen Blocker in HMG-API.

### With NEMESIS

1. Extract `gen5w-split-screen-blocker-v<version>-nemesis-usb.zip` to the root of a FAT32 or exFAT USB drive. The drive should contain `NEMESIS.data` and a `packages` folder. Keep only one NEMESIS package on the drive.
2. Once Android has finished starting, connect the drive and confirm the installation of **Gen5W Split Screen Blocker**.
3. Restart the head unit if prompted, then remove the drive.

HMG-API installs and applies the mod. To remove it, uninstall the package through NEMESIS Manager.

### After installing

**Restart the head unit** to activate the mod.

On confirmed compatible vehicles, the right-hand split screen and its slide-out tab should be suppressed after startup. On untested firmware, behavior may vary depending on which suppression methods are available.

## How it works

The mod runs through HMG-API inside the head unit's split-screen application, `com.mobis.splitscreen.right`.

It combines three mechanisms:

### Original Java hooks

Four hooks intercept the split-screen application's behavior:

- Prevent requests to open the panel.
- Prevent the panel from being made visible.
- Change requests to show the slide-out tab into requests to hide it.
- Hide the slide-out tab after initialization.

### Additional OSD suppression

Three additional hooks target the split-screen on-screen display:

- Deny standard requests to display the right-side OSD.
- Block an additional OSD request path.
- Prevent the overlay renderer from showing the right-side pop-over.

These hooks are attempted on recognized firmware when the expected methods are available.

### OEM Binder shutdown

The mod uses the head unit's existing OEM split-screen service to request that the panel close.

After service startup or driver login, it checks service availability, interface identity, and readiness before sending the shutdown request. Failed requests are retried up to 15 times per scheduled run.

Binder operations run on a dedicated worker thread to avoid blocking the application's main thread.

**The OEM shutdown is not restricted by the MD5 whitelist.** It remains available as a best-effort fallback when the Java hooks are skipped.

The original split-screen application remains running because other head-unit components may depend on it. The mod does not patch or replace the head unit's system ODEX files.

## Building

`gen5w-split-screen-blocker/build.sh` builds the hooks and release packages into `gen5w-split-screen-blocker/out/`.

Building requires a JDK, Android build-tools, an API 19 `android.jar`, the HMG-API mod SDK, and your own author key. The required variables are documented at the top of the build script.

Builds signed with a different author key cannot update an installation signed by the original author.

## Support

Gen5W Split Screen Blocker is free.

If it's useful to you, you can support DeFlight's work on the original mod and future projects through [PayPal](https://paypal.me/deflight) or [GitHub Sponsors](https://github.com/sponsors/DeFlight).

Donations are optional and do not unlock any features.

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-0070BA?logo=paypal&logoColor=white)](https://paypal.me/deflight) [![Sponsor on GitHub](https://img.shields.io/badge/Sponsor-GitHub-EA4AAA?logo=githubsponsors&logoColor=white)](https://github.com/sponsors/DeFlight)

## Credits

- [Open-HMG/HMG-API](https://github.com/Open-HMG/HMG-API) — the mod platform and hook framework.

## Disclaimer

Not affiliated with or endorsed by Hyundai, Kia, or Genesis. Modifying your car's head unit is at your own risk.

## License

MIT, see [LICENSE](LICENSE).
