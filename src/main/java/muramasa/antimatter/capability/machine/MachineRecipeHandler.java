package muramasa.antimatter.capability.machine;

import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.IMachineHandler;
import muramasa.antimatter.machine.MachineFlag;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.event.ContentEvent;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.event.MachineEvent;
import muramasa.antimatter.recipe.Recipe;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.Utils;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static muramasa.antimatter.machine.MachineState.*;

public class MachineRecipeHandler<T extends TileEntityMachine> implements IMachineHandler {

    protected final T tile;
    protected final boolean generator;
    protected final IIntArray progress = new IntArray(1);

    protected Recipe activeRecipe;
    protected int currentProgress, maxProgress;
    protected int overclock;

    //20 seconds per check.
    static final int WAIT_TIME = 20*20;
    protected int tickTimer = 0;

    //Consuming resources can call into the recipe handler, causing a loop.
    //For instance, consuming fluid in the fluid handlers calls back into the MachineRecipeHandler, deadlocking.
    //So just 'lock' during recipe ticking.
    private boolean tickingRecipe = false;


    public MachineRecipeHandler(T tile, CompoundNBT tag) {
    public MachineRecipeHandler(T tile) {
        this.tile = tile;
        this.generator = tile.getMachineType().has(MachineFlag.GENERATOR);
    }

    public void onUpdate() {
        if (activeRecipe == null && tickTimer >= WAIT_TIME) {
            tickTimer = 0;
            checkRecipe();
        } else if (activeRecipe == null) {
            tickTimer++;
        }
        if (tile.getMachineState() != IDLE) tickMachineLoop();
    public IIntArray getProgressData() {
        return progress;
    }

    protected void tickMachineLoop() {
        //To avoid a feedback loop caused by events firing from inside the recipe handler
        //(such as consuming resources for a recipe and/or generator check so we dont overflow tick recipes.
        if (tickingRecipe) return;
    public void setClientProgress() {
        setClientProgress(Float.floatToRawIntBits(this.currentProgress / (float) this.maxProgress));
    }

    public void setClientProgress(int value) {
        this.progress.set(0, value);
    }

    @OnlyIn(Dist.CLIENT)
    public float getClientProgress() {
        return Float.intBitsToFloat(this.progress.get(0));
    }

    @Override
    public void init() {
        checkRecipe();
    }

    public void onServerUpdate() {
        switch (tile.getMachineState()) {
            case IDLE:
                break;
            case ACTIVE:
            case OUTPUT_FULL:
                tile.setMachineState(tickRecipe());
                break;
            default:
                MachineState state = tickRecipe();
                if (state != ACTIVE) {
                    tile.setMachineState(IDLE);
                } else {
                    tile.setMachineState(state);
                }
                break;
        }
    }

    public Recipe findRecipe() {
        return tile.getMachineType().getRecipeMap().find(tile.getMachineTier(), tile.itemHandler, tile.fluidHandler);
    }

    //called when a new recipe is found, to process overclocking
    public void activateRecipe() {
        //if (canOverclock)
        curProgress = 0;
        overclock = 0;
        if (this.tile.getMachineTier().getVoltage() > activeRecipe.getPower()) {
            int voltage = this.tile.getMachineTier().getVoltage();
            int tier = 0;
            //Dont use utils, because we allow overclocking from ulv.
            for (int i = 0; i < Ref.V.length; i++) {
                if (activeRecipe.getPower() <= Ref.V[i]) {
                    tier = i;
                    break;
                }
            }
            int tempoverclock = (this.tile.getMachineTier().getVoltage() / Ref.V[tier]);
            while (tempoverclock > 1) {
                tempoverclock >>= 2;
                overclock++;
            }
             */
            System.out.println("Check Recipe when active recipe is null");
            checkRecipe();
            return tile.getMachineState();
        } else if (this.currentProgress == this.maxProgress) {
            this.currentProgress = 0;
            System.out.println("Finishing recipe");
            if (!generator) {
                this.consumeInputs(); // Normal recipes will consume input when recipe is finished
            }
            tile.itemHandler.ifPresent(h -> {
                h.addOutputs(activeRecipe.getOutputItems());
                // this.onMachineEvent(MachineEvent.ITEMS_OUTPUTTED);
            });
            tile.fluidHandler.ifPresent(h -> {
                for (FluidStack stack : activeRecipe.getOutputFluids()) {
                    h.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                }
                // this.onMachineEvent(MachineEvent.FLUIDS_OUTPUTTED);
            });
            if (!canOutput()) {
                setClientProgress(0);
                return OUTPUT_FULL;
            } else if (!canRecipeContinue()) {
                this.resetRecipe();
                return IDLE;
            } else {
                activateRecipe();
                return ACTIVE;
            }
        } else if (!canOutput()) {
            return OUTPUT_FULL;
        } else {
            this.currentProgress++;
            setClientProgress();
            return tile.getMachineState();
        }
    }

    protected boolean checkRecipe() {
        if (tile.getMachineState().allowRecipeCheck()) {
            if ((activeRecipe = findRecipe()) != null) {
                if (activeRecipe.getPower() > tile.getMaxInputVoltage() && !this.generator) {
                    tile.setMachineState(INVALID_TIER);
                    return false;
                }
                if (generator && (!activeRecipe.hasInputFluids() || activeRecipe.getInputFluids().length != 1)) {
                    return false;
                }
                activateRecipe();
                return true;
            }
            setClientProgress(0);
        }
        tile.setMachineState(IDLE);
        return false;
    }

    protected void activateRecipe() {
        //if (canOverclock)
        this.currentProgress = 0;
        this.overclock = 0;
        int voltage = tile.getMachineTier().getVoltage();
        long recipeVoltage = activeRecipe.getPower();
        if (voltage > recipeVoltage) {
            int tempOc = voltage / Ref.V[Utils.getVoltageTier(recipeVoltage)];
            while (tempOc > 1) {
                tempOc >>= 2;
                this.overclock++;
            }
        }
        this.maxProgress = Math.max(1, activeRecipe.getDuration() / (1 << overclock));
        tile.setMachineState(ACTIVE);
        if (generator) {
            consumeInputs(); // Power generation recipes will consume input when recipe is activated
        }
        // onRecipeFound(); TODO: MachineEvent
    }

    @Nullable
    public Recipe getActiveRecipe() {
        return activeRecipe;
    }

    public void consumeInputs() {
        tile.itemHandler.ifPresent(h -> h.consumeInputs(activeRecipe,false));
        tile.fluidHandler.ifPresent(h -> h.consumeInputs(activeRecipe.getInputFluids()));
    }

    public boolean canOutput() {
        if (tile.itemHandler.isPresent() && activeRecipe.hasOutputItems() && !tile.itemHandler.get().canOutputsFit(activeRecipe.getOutputItems()))
            return false;
        if (tile.fluidHandler.isPresent() && activeRecipe.hasOutputFluids() && !tile.fluidHandler.get().canOutputsFit(activeRecipe.getOutputFluids()))
            return false;
        return true;
    }

    //Schedules a check next tick for a possible recipe.
    public void scheduleCheck() {
        tickTimer = WAIT_TIME;
    }

    public void addOutputs() {
        tile.itemHandler.ifPresent(h -> {
            h.addOutputs(activeRecipe.getOutputItems());
            tile.onMachineEvent(MachineEvent.ITEMS_OUTPUTTED);
        });
    }

    public boolean canRecipeContinue() {
        if (tile.itemHandler.isPresent() && !tile.itemHandler.get().consumeInputs(activeRecipe,true)) //!Utils.doItemsMatchAndSizeValid(activeRecipe.getInputItems(), tile.itemHandler.get().getInputs()))
            return false;
        if (tile.fluidHandler.isPresent() && (activeRecipe.hasInputFluids() && (!Utils.doFluidsMatchAndSizeValid(activeRecipe.getInputFluids(), tile.fluidHandler.get().getInputs()))))
            return false;
        return true;
    }

    protected boolean canRecipeContinue() {
        return tile.itemHandler.map(i -> i.consumeInputs(this.activeRecipe, true)).orElse(false) || Utils.doFluidsMatchAndSizeValid(activeRecipe.getInputFluids(), tile.fluidHandler.map(MachineFluidHandler::getInputs).orElse(new FluidStack[0]));
    }

    protected boolean consumeGeneratorResources() {
        if (!activeRecipe.hasInputFluids()) {
            throw new RuntimeException("Missing fuel in active generator recipe!");
        }
        boolean shouldRun = tile.energyHandler.map(h -> h.insert((long)(tile.getMachineType().getMachineEfficiency()*(double)tile.getMachineTier().getVoltage()),true) > 0).orElse(false);
        if (!shouldRun) return false;
        long toConsume = (long) ((double)tile.getMachineTier().getVoltage() / (activeRecipe.getPower() /(double) Objects.requireNonNull(activeRecipe.getInputFluids())[0].getAmount()));
        if (tile.fluidHandler.map(h -> {
            int amount = h.inputWrapper.drain(new FluidStack(activeRecipe.getInputFluids()[0],(int)toConsume), IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (amount == toConsume) {
                h.inputWrapper.drain(new FluidStack(activeRecipe.getInputFluids()[0],(int)toConsume), IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
            return false;
        }).orElse(false)) {
            //Input energy
            tile.energyHandler.ifPresent(handler -> {
                handler.insert((long)(tile.getMachineType().getMachineEfficiency()*(double)tile.getMachineTier().getVoltage()), false);
            });
            return true;
        }
        return false;
    }

    public void resetRecipe() {
        this.activeRecipe = null;
        this.currentProgress = 0;
        this.overclock = 0;
        setClientProgress(0);
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (event instanceof ContentEvent) {
            switch ((ContentEvent) event) {
                case FLUID_INPUT_CHANGED:
                case ITEM_INPUT_CHANGED:
                    if (tile.getMachineState().allowRecipeCheck()) {
                        this.checkRecipe();
                    }
                    break;
            }
        } else if (event instanceof MachineEvent) {
            switch ((MachineEvent) event) {
                case ENERGY_INPUTTED:
                    if (tile.getMachineState() == POWER_LOSS) {
                        tile.setMachineState(ACTIVE);
                    }
                    break;
                case ENERGY_DRAINED:
                    if (tile.getMachineState() == ACTIVE) {
                        tile.setMachineState(POWER_LOSS);
                    }
                    break;
            }
        }
    }

}
