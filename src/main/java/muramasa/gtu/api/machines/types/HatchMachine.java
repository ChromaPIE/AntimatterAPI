package muramasa.gtu.api.machines.types;

import muramasa.gtu.GregTech;
import muramasa.gtu.Ref;
import muramasa.gtu.api.machines.MachineFlag;
import muramasa.gtu.api.machines.Tier;
import muramasa.gtu.common.blocks.BlockMachine;
import muramasa.gtu.api.tileentities.multi.TileEntityHatch;

import static muramasa.gtu.api.machines.MachineFlag.*;

public class HatchMachine extends Machine {

    public HatchMachine(String name, MachineFlag... flags) {
        super(name, new BlockMachine(name), TileEntityHatch.class);
        setTiers(Tier.getAllElectric());
        addFlags(HATCH, CONFIGURABLE, COVERABLE);
        addFlags(flags);
        if (hasFlag(GUI)) {
            addGUI(GregTech.INSTANCE, Ref.HATCH_ID);
        }
    }

    public HatchMachine(String name, Class tileClass, MachineFlag... flags) {
        this(name, flags);
        setTileClass(tileClass);
    }
}