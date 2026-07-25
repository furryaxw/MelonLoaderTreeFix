using HarmonyLib;
using Il2CppVisualDesignCafe.Rendering.Nature;
using MelonLoader;

[assembly: MelonInfo(
    typeof(MelonLoaderTreeFix.MelonLoaderTreeFixMod),
    "MelonLoaderTreeFix",
    "1.3.1",
    "furryAxw")]
[assembly: MelonGame("HD", "Sprocket")]

namespace MelonLoaderTreeFix
{
    public sealed class MelonLoaderTreeFixMod : MelonMod
    {
        public override void OnInitializeMelon()
        {
            LoggerInstance.Msg("已启用 MelonLoaderTreeFix。");
        }
    }

    [HarmonyPatch(typeof(NatureRenderer), nameof(NatureRenderer.Initialize))]
    internal static class NatureRendererInitializePatch
    {
        private static void Prefix(NatureRenderer __instance)
        {
            if (__instance != null)
                __instance.RenderTreesWithNatureRenderer = false;
        }
    }

    [HarmonyPatch(typeof(TerrainGrassStreamer), nameof(TerrainGrassStreamer.LoadCellAsync))]
    internal static class TerrainGrassStreamerLoadCellAsyncPatch
    {
        private const int MaxErrorMessages = 3;
        private static int failureMessageCount;

        private readonly struct QueueState
        {
            public QueueState(int count)
            {
                Count = count;
            }

            public int Count { get; }
        }

        private static void Prefix(TerrainGrassStreamer __instance, out QueueState __state)
        {
            var queue = __instance?._sortedCellsToLoad;
            __state = new QueueState(queue == null ? -1 : queue.Count);
        }

        private static void Postfix(
            TerrainGrassStreamer __instance,
            int cellIndex,
            QueueState __state)
        {
            if (__instance == null || __instance.IsDisposed)
                return;

            var queue = __instance._sortedCellsToLoad;
            var cells = __instance.Cells;
            if (queue == null || cells == null || __state.Count < 0)
                return;

            int queueCount = queue.Count;
            if (queueCount != __state.Count + 1)
                return;

            int cellCount = cells.Length;
            if ((uint)cellIndex >= (uint)cellCount)
            {
                LogFailure(
                    $"LoadCellAsync 收到异常索引 {cellIndex}；有效范围为 [0, {cellCount})。");
                return;
            }

            var rawValues = queue.values;
            if (rawValues == null || rawValues.Length < queueCount)
            {
                LogFailure(
                    $"SortedList 底层 values 数组不可用或长度不足；" +
                    $"队列数量={queueCount}，数组长度={(rawValues == null ? -1 : rawValues.Length)}。");
                return;
            }

            int corruptSlot = -1;
            int corruptValue = 0;
            int corruptCount = 0;

            for (int index = 0; index < queueCount; index++)
            {
                int storedValue = rawValues[index];
                if ((uint)storedValue < (uint)cellCount)
                    continue;

                corruptSlot = index;
                corruptValue = storedValue;
                corruptCount++;
            }

            if (corruptCount != 1)
            {
                LogFailure(
                    $"入队后应当只有 1 个损坏值，但实际发现 {corruptCount} 个；" +
                    $"队列数量={queueCount}，请求单元={cellIndex}。");
                return;
            }

            rawValues[corruptSlot] = cellIndex;
            if (rawValues[corruptSlot] != cellIndex)
            {
                LogFailure(
                    $"无法将队列槽位 {corruptSlot} 从 {corruptValue} 改写为 {cellIndex}。");
                return;
            }
        }

        private static void LogFailure(string message)
        {
            if (failureMessageCount >= MaxErrorMessages)
                return;

            failureMessageCount++;
            MelonLogger.Error($"草地流送队列修复失败：{message}");
        }
    }
}
