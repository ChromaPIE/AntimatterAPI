package muramasa.gregtech.api.structure;

import muramasa.gregtech.api.capability.IComponent;
import muramasa.gregtech.api.capability.GTCapabilities;
import muramasa.gregtech.api.data.Machines;
import muramasa.gregtech.api.enums.CasingType;
import muramasa.gregtech.api.enums.CoilType;
import muramasa.gregtech.api.util.Utils;
import muramasa.gregtech.api.util.int3;
import muramasa.gregtech.common.tileentities.base.multi.TileEntityMultiMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;

import java.util.HashMap;

public class StructureElement {

    private static HashMap<String, StructureElement> elementLookup = new HashMap<>();

    /** Component Elements **/
    public static StructureElement PBF = new StructureElement(Machines.PRIMITIVE_BLAST_FURNACE);
    public static StructureElement PBF_CASING = new StructureElement(CasingType.FIRE_BRICK);
    public static StructureElement BBF = new StructureElement(Machines.BRONZE_BLAST_FURNACE);
    public static StructureElement BBF_CASING = new StructureElement(CasingType.BRONZE_PLATED_BRICK);
    public static StructureElement BF_AIR_OR_LAVA = new StructureElement("airorlava") {
        @Override
        public boolean evaluate(TileEntityMultiMachine machine, int3 pos, StructureResult result) {
            IBlockState state = machine.getWorld().getBlockState(pos.asBlockPos());
            return AIR.evaluate(machine, pos, result) || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA;
        }
    };

    public static StructureElement EBF = new StructureElement(Machines.ELECTRIC_BLAST_FURNACE);
    public static StructureElement HATCH_OR_CASING_EBF = new StructureElement("hatchorcasingebf", CasingType.HEAT_PROOF, Machines.HATCH_ITEM_INPUT, Machines.HATCH_ITEM_OUTPUT);
    public static StructureElement ANY_COIL_EBF = new StructureElement("anycoilebf", CoilType.values());

    public static StructureElement VF_MACHINE = new StructureElement(Machines.VACUUM_FREEZER);
    public static StructureElement VF_HATCH_OR_CASING = new StructureElement("hatchorcasingvf", CasingType.FROST_PROOF, Machines.HATCH_ITEM_INPUT, Machines.HATCH_ITEM_OUTPUT, Machines.HATCH_ENERGY);

    public static StructureElement FR_MACHINE = new StructureElement(Machines.FUSION_REACTOR_1);
    public static StructureElement FUSION_CASING = new StructureElement(CasingType.FUSION_3);
    public static StructureElement FUSION_COIL = new StructureElement(CoilType.FUSION);

    /** Custom Elements **/
    public static StructureElement X = new StructureElement("x", false); //Used to skip positions for non-cubic structures
    public static StructureElement AIR = new StructureElement("air") { //Air Block Check
        @Override
        public boolean evaluate(TileEntityMultiMachine machine, int3 pos, StructureResult result) {
            IBlockState state = machine.getWorld().getBlockState(pos.asBlockPos());
            return state.getBlock().isAir(state, machine.getWorld(), pos.asBlockPos());
        }
    };

    private String elementName;
    private String[] elementIds;
    private boolean addToList;

    public StructureElement(IStringSerializable elementName) {
        this(elementName.getName(), true, elementName);
    }

    public StructureElement(String elementName, IStringSerializable... elementIds) {
        this(elementName, true, elementIds);
    }

    public StructureElement(String elementName, boolean addToList, IStringSerializable... elementIds) {
        this.elementName = elementName;
        this.addToList = addToList;
        this.elementIds = new String[elementIds.length];
        for (int i = 0; i < elementIds.length; i++) {
            this.elementIds[i] = elementIds[i].getName();
            elementLookup.put(elementIds[i].getName(), this);
        }
        elementLookup.put(elementName, this);
    }

    public String getName() {
        return elementName;
    }

    public boolean shouldAddToList() {
        return addToList;
    }

    public boolean evaluate(TileEntityMultiMachine machine, int3 pos, StructureResult result) {
        TileEntity tile = Utils.getTile(machine.getWorld(), pos.asBlockPos());
        if (tile != null && tile.hasCapability(GTCapabilities.COMPONENT, null)) {
            IComponent component = tile.getCapability(GTCapabilities.COMPONENT, null);
            for (int i = 0; i < elementIds.length; i++) {
                if (elementIds[i].equals(component.getId())) {
                    result.addComponent(elementName, component);
                    return true;
                }
            }
            result.withError("Expected: '" + elementName + "' Found: '" + component.getId() + "' @" + pos);
            return false;
        }
        result.withError("No valid component found @" + pos);
        return false;
    }

    public static StructureElement get(String name) {
        return elementLookup.get(name);
    }
}