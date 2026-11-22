# Gen5W Split Screen Blocker

An [HMG-API](https://github.com/Open-HMG/HMG-API) mod that removes the right-hand split screen from your car's head unit, along with the slide-out tab on the right edge of the screen. On the car it's called **Gen5W Split Screen Blocker**.

By DeFlight, LLC.

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-0070BA?logo=paypal&logoColor=white)](https://paypal.me/deflight) [![Sponsor on GitHub](https://img.shields.io/badge/Sponsor-GitHub-EA4AAA?logo=githubsponsors&logoColor=white)](https://github.com/sponsors/DeFlight)

> [!IMPORTANT]
> **Tested on Hyundai Ioniq 6 (US) on 260507 software.**
>
> **Kia Sportage (and potentially other Kias) 260507 is new and untested.**
>
> **Tested on Kia K5 on 260507.** See [Compatibility](#compatibility).

## Contents

- [What it does](#what-it-does)
- [Turning it on and off](#turning-it-on-and-off)
- [Good to know](#good-to-know)
- [Requirements](#requirements) · [Compatibility](#compatibility) · [Install](#install)
- [How it works](#how-it-works)
- [Building](#building)
- [Support](#support)

## What it does

| On the car | With the mod on |
|---|---|
| Slide-out tab on the right edge | Gone from every screen |
| Right-hand split screen | Never opens |
| Everything else on the head unit | Unchanged |

There's nothing to set up. The mod has no settings page and no tile in the app menu.

## Turning it on and off

Use the mod's switch in the HMG-API app.

**To turn it off:**

1. In HMG-API, open **Mods** and pick **Gen5W Split Screen Blocker**.
2. Switch it off and tap **Apply changes**.

The split screen comes back at the next screen change, even if HMG-API shows **Restart needed**.

**To turn it back on:**

1. Switch it on and tap **Apply changes**.
2. Tap **Restart unit**.

## Good to know

- **The car's split-screen settings** are still in the car's menus, but they have no effect while the mod is on.
- **Side panel pages from other HMG-API mods** live in the split screen, so they can't be seen while this mod is on.
- **After a car software update,** HMG-API has to be installed again, and this mod stays inactive until it has a build for the new software.
- **Removing the mod** brings the split screen and its tab back after a restart.

## Requirements

- A car with a **Gen5W** navigation head unit. Tested on the car listed under [Compatibility](#compatibility); unconfirmed on other cars.
- **[HMG-API](https://github.com/Open-HMG/HMG-API)** installed ([Installing HMG-API](https://github.com/Open-HMG/HMG-API/wiki/Installing-HMG-API)), with its hooks on for your car. The top bar of the HMG-API app shows whether they are.

## Compatibility

**Tested:**

| Car | Firmware | `RightScreenHome.odex` MD5 |
|---|---|---|
| 2025 Hyundai Ioniq 6 Limited (US) | CEEV.USA.0000.V015.012.260507 | `cc1bd5131bee221ea3b5ed24a1c1d31a` |

**Additional builds checked from vehicle files, not yet tested:**

| Car | Firmware | `RightScreenHome.odex` MD5 |
|---|---|---|
| 2023 Kia Sportage Hybrid (US) | NQ5HEV.USA.0000.V015.012.260507 | `7b84ed639e85a711c4a9f2c78e33acf4` |
| 2022 Kia K5 (US) | DL322.USA.0000.V015.012.260507 | `bd97b438aeb3d9c77510b5e2da4234f7` |

HMG-API's maps list that build for these Kia US 260507 cars too: Sportage (incl. Hybrid and Plug-in Hybrid), Niro Hybrid and Plug-in Hybrid, Forte, Soul, Seltos, Telluride and Carnival.

**Unconfirmed on other cars.**

The method names inside the car's split-screen app change between software builds, so the mod only runs on the exact builds it was checked on, as listed above. On any other build it writes a line to its log and leaves the car alone.

Tried it on another car or software version, or something doesn't work? Please [open an issue](../../issues/new?template=bug-report.yml). The form asks for your car and its software, the versions, what happened, and a screenshot of the mod's log from the HMG-API app.

## Install

Install [HMG-API](https://github.com/Open-HMG/HMG-API/wiki/Installing-HMG-API) first. Then download one of these from [Releases](../../releases):

| File | For |
|---|---|
| `gen5w-split-screen-blocker-v<version>-hmgapi-usb.zip` | installing from the HMG-API app with a USB drive |
| `gen5w-split-screen-blocker-v<version>-nemesis-usb.zip` | installing from NEMESIS with a USB drive |
| `gen5w-split-screen-blocker-v<version>-nemesis-package.zip` | the NEMESIS package itself, for the Package Store or `nemesis install`. Not for unzipping onto a drive |
| `gen5w-split-screen-blocker-v<version>.zip` | the signed mod on its own |

### With the HMG-API app

1. Unzip `gen5w-split-screen-blocker-v<version>-hmgapi-usb.zip` onto the top level of a FAT32 or exFAT USB drive, so the drive has `HMGAPI/updates/gen5w-split-screen-blocker-v<version>.zip`.
2. Plug the drive into the car and install the mod from the updates page in the HMG-API app.
3. Tap **Apply changes**.

To remove it, use **Remove** on Gen5W Split Screen Blocker in the HMG-API app.

### With NEMESIS

1. Unzip `gen5w-split-screen-blocker-v<version>-nemesis-usb.zip` onto the top level of a FAT32 or exFAT USB drive, so the drive has `NEMESIS.data` and the `packages` folder at its top level. Keep only one NEMESIS package on the drive.
2. Once Android is up, plug the drive into the car and confirm the install of "Gen5W Split Screen Blocker".
3. Restart the head unit if it asks, then take the drive out.

HMG-API installs and applies the mod for you. To remove it, uninstall the package in NEMESIS Manager.

### After installing

**Restart the head unit.** The tab and the split screen are gone once it's back up.

## How it works

The mod's hooks run inside the car's split-screen app (`com.mobis.splitscreen.right`):

- **The panel:** requests to open it are dropped. Requests to close it run as usual.
- **The tab:** every request to show it (window `SS_Button_Window`) becomes the car's own request to hide it, and it starts hidden when the app starts.
- **The app keeps running,** because the car's other apps talk to it.
- **Nothing on the head unit's system files is changed.** Turning the mod off or removing it is all it takes to go back.

## Building

`gen5w-split-screen-blocker/build.sh` builds the hooks and all the release files into `gen5w-split-screen-blocker/out/`. It needs a JDK, Android build-tools, an API 19 `android.jar`, HMG-API's mod SDK, and your own author key; the variables are listed at the top of the script. A build signed with a different author key installs as a different author, so it can't update an install of the official release.

## Support

Gen5W Split Screen Blocker is free. If it's useful to you, you can leave a tip to support DeFlight's work on it and future mods: **[PayPal](https://paypal.me/deflight)** (one-time, no account needed) or **[GitHub Sponsors](https://github.com/sponsors/DeFlight)** (one-time or monthly, with a GitHub account). Tips are optional and don't unlock anything.

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-0070BA?logo=paypal&logoColor=white)](https://paypal.me/deflight) [![Sponsor on GitHub](https://img.shields.io/badge/Sponsor-GitHub-EA4AAA?logo=githubsponsors&logoColor=white)](https://github.com/sponsors/DeFlight)

## Credits

- [Open-HMG/HMG-API](https://github.com/Open-HMG/HMG-API), the mod platform this runs on.

## Disclaimer

Not affiliated with or endorsed by Hyundai, Kia or Genesis. Modifying your car's head unit is at your own risk.

## License

MIT, see [LICENSE](LICENSE).
