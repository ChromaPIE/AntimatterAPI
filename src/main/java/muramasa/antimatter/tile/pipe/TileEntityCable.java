package muramasa.antimatter.tile.pipe;

import muramasa.antimatter.pipe.types.Cable;
import muramasa.antimatter.pipe.types.PipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import tesseract.Tesseract;
import tesseract.api.electric.IElectricCable;
import tesseract.util.Dir;

import javax.annotation.Nonnull;

public class TileEntityCable extends TileEntityPipe implements IElectricCable {

    public TileEntityCable(PipeType<?> type) {
        super(type);
    }

    @Override
    public void refreshConnections() {
        if (isServerSide()) Tesseract.ELECTRIC.remove(getDimention(), pos.toLong());
        super.refreshConnections();
        if (isServerSide()) Tesseract.ELECTRIC.registerConnector(getDimention(), pos.toLong(), this);
    }

    @Override
    public void onRemove() {
        Tesseract.ELECTRIC.remove(getDimention(), pos.toLong());
    }

    @Override
    public boolean canConnect(TileEntity tile, Direction side) {
        return tile instanceof TileEntityCable/* && getCover(side).isEqual(Data.COVER_NONE)*/ || tile.getCapability(CapabilityEnergy.ENERGY).isPresent();
    }

    @Override
    public int getVoltage() {
        return ((Cable<?>)getPipeType()).getTier().getVoltage();
    }

    @Override
    public int getLoss() {
        return ((Cable<?>)getPipeType()).getLoss();
    }

    @Override
    public int getAmps() {
        return ((Cable<?>)getPipeType()).getAmps(getPipeSize());
    }

    @Override
    public boolean connects(@Nonnull Dir direction) {
        return true;//Connectivity.has(connections, direction);
    }
}
