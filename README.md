# MelonLoaderTreeFix

适用于《Sprocket》的 MelonLoader 花草树木渲染修复模组。

## 功能

- 禁用会导致异常的 Nature Renderer 树木渲染路径。
- 修复地表植被流式加载队列中偶发的无效单元索引。
- 限制重复错误日志，避免故障情况下刷屏。

## 安装

1. 安装与游戏版本匹配的 MelonLoader。
2. 将 `MelonLoaderTreeFix.dll` 放入游戏根目录的 `Mods` 文件夹。

## 构建

项目目标框架为 .NET 6，并引用本地 Sprocket MelonLoader/IL2CPP 程序集。默认目录布局为：

```text
G:\Sprocket\
├── MelonLoader\
└── mod\MelonLoaderTreeFix\
```

```powershell
dotnet build .\MelonLoaderTreeFix\MelonLoaderTreeFix.csproj --configuration Release
```

`NatureRendererTargetDump.java` 是开发期间使用的 Ghidra 分析辅助脚本，不参与模组构建。

## License

[GPL-3.0-only](LICENSE.txt)
