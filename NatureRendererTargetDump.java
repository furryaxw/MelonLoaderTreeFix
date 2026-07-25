// Headless Ghidra helper for Nature Renderer IL2CPP targets.
// @category Sprocket

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import ghidra.app.cmd.disassemble.DisassembleCommand;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.SourceType;

public class NatureRendererTargetDump extends GhidraScript {
    private static class Target {
        final long rva;
        final String name;

        Target(long rva, String name) {
            this.rva = rva;
            this.name = name;
        }
    }

    private static final Target[] TARGETS = new Target[] {
        new Target(0x2B91B40L, "NatureRenderer.Initialize"),
        new Target(0x2B916A0L, "NatureRenderer.Load"),
        new Target(0x2B91F50L, "NatureRenderer.StartRendering"),
        new Target(0x2B92160L, "NatureRenderer.StopRendering"),
        new Target(0x2B92240L, "NatureRenderer.OnEnable"),
        new Target(0x2B92380L, "NatureRenderer.Start"),
        new Target(0x2B92460L, "NatureRenderer.LateUpdate"),
        new Target(0x2B92A90L, "NatureRenderer.OnDisable"),
        new Target(0x2B92D40L, "NatureRenderer.DisableUnityRenderer"),
        new Target(0x2B92F60L, "NatureRenderer.RestoreUnityRenderer"),
        new Target(0x2B99CF0L, "TerrainGrassRenderer.ctor"),
        new Target(0x2B99F00L, "TerrainGrassRenderer.LoadCells"),
        new Target(0x2B9A0B0L, "TerrainGrassRenderer.ClearLoadingQueue"),
        new Target(0x2B9A240L, "TerrainGrassRenderer.OnTerrainChanged"),
        new Target(0x2B9A420L, "TerrainGrassRenderer.Destroy"),
        new Target(0x2B9A470L, "TerrainGrassRenderer.LateUpdate"),
        new Target(0x2B9A6C0L, "TerrainGrassRenderer.Load"),
        new Target(0x2B9B400L, "TerrainGrassRenderer.Recycle"),
        new Target(0x2B9D7F0L, "TerrainGrassStreamer.ctor"),
        new Target(0x2B9DDD0L, "TerrainGrassStreamer.Dispose"),
        new Target(0x2B9DE10L, "TerrainGrassStreamer.Recycle"),
        new Target(0x2B9DEC0L, "TerrainGrassStreamer.LoadCells"),
        new Target(0x2B9E020L, "TerrainGrassStreamer.ClearQueue"),
        new Target(0x2B9E0D0L, "TerrainGrassStreamer.ClearStatus"),
        new Target(0x2B9E170L, "TerrainGrassStreamer.Update"),
        new Target(0x2B9E270L, "TerrainGrassStreamer.GetBuffersForCellsInRange"),
        new Target(0x2B9E5F0L, "TerrainGrassStreamer.OnCellInRange"),
        new Target(0x2B9E770L, "TerrainGrassStreamer.LoadCellAsync"),
        new Target(0x2B9E990L, "TerrainGrassStreamer.UpdateLoadCellsAsync"),
        new Target(0x2B9ED50L, "TerrainGrassStreamer.LoadCell")
        ,new Target(0x0A39CC0L, "SortedList.ValueList<double,int>.get_Item")
        ,new Target(0x0921F30L, "SortedList<double,int>.Add")
        ,new Target(0x0922730L, "SortedList<double,int>.get_Values")
        ,new Target(0x0923C90L, "SortedList<double,int>.IndexOfValue")
        ,new Target(0x0923E60L, "SortedList<double,int>.RemoveAt")
        ,new Target(0x2B78350L, "CellLayout.ctor")
        ,new Target(0x2B860B0L, "InstanceStreamer.ctor")
        ,new Target(0x2B862E0L, "InstanceStreamer.Dispose")
        ,new Target(0x2B86A00L, "InstanceStreamer.GetBuffersForCellsInRange")
    };

    @Override
    protected void run() throws Exception {
        String[] args = getScriptArgs();
        File outFile = new File(args.length > 0 ? args[0] : "nature-renderer-decomp.txt");
        DecompInterface decompiler = new DecompInterface();
        decompiler.openProgram(currentProgram);

        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            out.println("Program: " + currentProgram.getName());
            out.println("ImageBase: " + currentProgram.getImageBase());
            out.println();

            for (Target target : TARGETS) {
                Address address = currentProgram.getImageBase().add(target.rva);
                out.println("==== " + target.name + " @ " + address + " ====");
                new DisassembleCommand(address, null, true).applyTo(currentProgram, monitor);

                Function function = getFunctionAt(address);
                if (function == null) {
                    function = createFunction(address, sanitize(target.name));
                } else {
                    function.setName(sanitize(target.name), SourceType.USER_DEFINED);
                }

                if (function == null) {
                    out.println("NO_FUNCTION");
                    out.println();
                    continue;
                }

                DecompileResults results = decompiler.decompileFunction(function, 60, monitor);
                if (results != null && results.decompileCompleted()) {
                    out.println(results.getDecompiledFunction().getC());
                } else {
                    out.println("DECOMPILE_FAILED");
                    if (results != null) {
                        out.println(results.getErrorMessage());
                    }
                }
                out.println();
            }
        } finally {
            decompiler.dispose();
        }
        println("WROTE " + outFile.getAbsolutePath());
    }

    private String sanitize(String name) {
        return name.replace('.', '_').replace('$', '_').replace('<', '_').replace('>', '_');
    }
}
