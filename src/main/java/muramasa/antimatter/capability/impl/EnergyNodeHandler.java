package muramasa.antimatter.capability.impl;

import muramasa.antimatter.Configs;
import muramasa.antimatter.capability.INodeHandler;
import muramasa.antimatter.cover.Cover;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.Tesseract;
import tesseract.api.electric.IElectricNode;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyNodeHandler implements IElectricNode, INodeHandler {

    private TileEntity tile;
    private IEnergyStorage storage;
    private boolean valid = true;
    
    private EnergyNodeHandler(TileEntity tile, IEnergyStorage storage) {
        this.tile = tile;
        this.storage = storage;
    }

    // TODO: Make sure that registartion on the server side

    @Nullable
    public static EnergyNodeHandler of(TileEntity tile) {
        LazyOptional<IEnergyStorage> capability = tile.getCapability(CapabilityEnergy.ENERGY);
        if (capability.isPresent()) {
            EnergyNodeHandler node = new EnergyNodeHandler(tile, capability.orElse(null));
            capability.addListener(o -> node.onRemove(null));
            Tesseract.ELECTRIC.registerNode(tile.getWorld().getDimension().getType().getId(), tile.getPos().toLong(), node);
            return node;
        }
        return null;
    }

    @Override
    public void onRemove(@Nullable Direction side) {
        if (side == null) {
            Tesseract.ELECTRIC.remove(tile.getWorld().getDimension().getType().getId(), tile.getPos().toLong());
            valid = false;
        }
    }

    @Override
    public void onUpdate(Direction side, Cover cover) {
        //if (cover instanceof CoverFilter)
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public long insert(long maxReceive, boolean simulate) {
        return storage.receiveEnergy((int)(maxReceive * Configs.GAMEPLAY.EU_TO_RF), simulate);
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public long getEnergy() {
        return storage.getEnergyStored();
    }

    @Override
    public long getCapacity() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public int getOutputAmperage() {
        return 0;
    }

    @Override
    public int getOutputVoltage() {
        return 0;
    }

    @Override
    public int getInputAmperage() {
        return 1;
    }

    @Override
    public int getInputVoltage() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canOutput() {
        return false;
    }

    @Override
    public boolean canInput() {
        return storage.canReceive();
    }

    @Override
    public boolean canOutput(@Nonnull Dir direction) {
        return false;
    }

    @Override
    public boolean connects(@Nonnull Dir direction) {
        return true;
    }
}
