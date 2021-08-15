package muramasa.antimatter.tile.single;

import muramasa.antimatter.gui.GuiInstance;
import muramasa.antimatter.gui.IGuiElement;
import muramasa.antimatter.gui.event.GuiEvent;
import muramasa.antimatter.gui.event.IGuiEvent;
import muramasa.antimatter.gui.widget.InfoRenderWidget;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.types.Machine;
import net.minecraft.entity.player.PlayerEntity;
import tesseract.api.capability.TesseractGTCapability;

public class TileEntityDigitalTransformer<T extends TileEntityDigitalTransformer<T>> extends TileEntityTransformer<T> {

    public TileEntityDigitalTransformer(Machine<?> type) {
        super(type, 0, (v) -> (8192L + v * 64L));
    }

    @Override
    public void onGuiEvent(IGuiEvent event, PlayerEntity playerEntity, int... data) {
        if (event == GuiEvent.EXTRA_BUTTON) {
            energyHandler.ifPresent(h -> {
                boolean shiftHold = data[1] != 0;
                switch (data[0]) {
                    case 0:
                        voltage /= shiftHold ? 512 : 64;
                        break;
                    case 1:
                        voltage -= shiftHold ? 512 : 64;
                        break;
                    case 2:
                        amperage /= shiftHold ? 512 : 64;
                        break;
                    case 3:
                        amperage -= shiftHold ? 512 : 64;
                        break;
                    case 4:
                        voltage /= shiftHold ? 16 : 2;
                        break;
                    case 5:
                        voltage -= shiftHold ? 16 : 1;
                        break;
                    case 6:
                        amperage /= shiftHold ? 16 : 2;
                        break;
                    case 7:
                        amperage -= shiftHold ? 16 : 1;
                        break;
                    case 8:
                        voltage += shiftHold ? 512 : 64;
                        break;
                    case 9:
                        voltage *= shiftHold ? 512 : 64;
                        break;
                    case 10:
                        amperage += shiftHold ? 512 : 64;
                        break;
                    case 11:
                        amperage *= shiftHold ? 512 : 64;
                        break;
                    case 12:
                        voltage += shiftHold ? 16 : 1;
                        break;
                    case 13:
                        voltage *= shiftHold ? 16 : 2;
                        break;
                    case 14:
                        amperage += shiftHold ? 16 : 1;
                        break;
                    case 15:
                        amperage *= shiftHold ? 16 : 2;
                        break;
                }

                setMachineState((long)(amperage * voltage) >= 0L ? getDefaultMachineState() : MachineState.DISABLED);

                if (isDefaultMachineState()) {
                    h.setInputVoltage(getMachineTier().getVoltage());
                    h.setOutputVoltage(voltage);
                    h.setInputAmperage(amperage);
                    h.setOutputVoltage(1);
                } else {
                    h.setInputVoltage(voltage);
                    h.setOutputVoltage(getMachineTier().getVoltage());
                    h.setInputAmperage(1);
                    h.setOutputVoltage(amperage);
                }

                this.refreshCap(TesseractGTCapability.ENERGY_HANDLER_CAPABILITY);
            });
        }
    }
/*
    @Override
    public void drawInfo(GuiInstance instance, MatrixStack stack, FontRenderer renderer, int left, int top) {
        renderer.drawString(stack,"Control Panel", left + 43, top + 21, 16448255);
        renderer.drawString(stack,"VOLT: " + voltage, left + 43, top + 40, 16448255);
        renderer.drawString(stack,"TIER: " + Tier.getTier(voltage < 0 ? -voltage : voltage).getId().toUpperCase(), left + 43, top + 48, 16448255);
        renderer.drawString(stack,"AMP: " + amperage, left + 43, top + 56, 16448255);
        renderer.drawString(stack,"SUM: " + (long)(amperage * voltage), left + 43, top + 64, 16448255);
    }*/

    @Override
    public void addWidgets(GuiInstance instance, IGuiElement parent) {
        super.addWidgets(instance, parent);
        instance.addWidget(InfoRenderWidget.build());
    }
}
