package muramasa.gregtech.api.machines;

import muramasa.gregtech.api.data.Machines;
import muramasa.gregtech.api.machines.types.Machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public enum MachineFlag {

    BASIC(), //
    STEAM(),
    MULTI(), //Has structure
    HATCH(),
    ITEM(), //Can store items
    FLUID(),
    ENERGY(), //Needs power
    RECIPE(), //Has a recipe map
    GUI(),
    COVERABLE(),
    CONFIGURABLE();

    private int bit;
    private ArrayList<Machine> typeList;

    MachineFlag() {
        bit = 1 << ordinal();
        typeList = new ArrayList<>();
    }

    public void add(Machine... machines) {
        typeList.addAll(Arrays.asList(machines));
    }

    public int getBit() {
        return bit;
    }

    public Collection<Machine> getTypes() {
        return typeList;
    }

    public Collection<MachineStack> getStacks() {
        ArrayList<MachineStack> stacks = new ArrayList<>();
        for (Machine machine : typeList) {
            for (Tier tier : machine.getTiers()) {
                stacks.add(Machines.get(machine, tier));
            }
        }
        return stacks;
    }

    public static Collection<Machine> getTypes(MachineFlag... flags) {
        ArrayList<Machine> types = new ArrayList<>();
        for (MachineFlag flag : flags) {
            types.addAll(flag.getTypes());
        }
        return types;
    }
}