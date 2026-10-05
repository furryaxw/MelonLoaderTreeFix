# MelonLoaderTreeFix

[中文](README.zh.md) | **English**

A MelonLoader mod for Sprocket that fixes grass and tree rendering.

## Features

- Disables the Nature Renderer tree rendering path that triggers the error.
- Fixes occasional invalid cell indices in the ground-vegetation streaming load queue.
- Throttles repeated error logging so a fault does not flood the log.

## Installation

1. Install the MelonLoader version that matches your game version.
2. Place `MelonLoaderTreeFix.dll` into the `Mods` folder in the game root directory.

## Building

The project targets .NET 6 and references local Sprocket MelonLoader/IL2CPP assemblies. The default directory layout is:

```text
G:\Sprocket\
├── MelonLoader\
└── mod\MelonLoaderTreeFix\
```

```powershell
dotnet build .\MelonLoaderTreeFix\MelonLoaderTreeFix.csproj --configuration Release
```

`NatureRendererTargetDump.java` is a Ghidra analysis helper script used during development; it is not part of the mod build.

## License

[GPL-3.0-only](LICENSE.txt)
