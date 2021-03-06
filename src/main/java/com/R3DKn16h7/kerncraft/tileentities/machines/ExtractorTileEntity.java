package com.R3DKn16h7.kerncraft.tileentities.machines;

import com.R3DKn16h7.kerncraft.capabilities.element.ElementCapabilities;
import com.R3DKn16h7.kerncraft.capabilities.element.IElementContainer;
import com.R3DKn16h7.kerncraft.client.gui.MachineGuiContainer;
import com.R3DKn16h7.kerncraft.crafting.ExtractorRecipe;
import com.R3DKn16h7.kerncraft.crafting.KernCraftRecipes;
import com.R3DKn16h7.kerncraft.elements.ElementStack;
import com.R3DKn16h7.kerncraft.tileentities.SmeltingTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public class ExtractorTileEntity extends SmeltingTileEntity<ExtractorRecipe>
        implements ICapabilityProvider {

    // Slot IDs
    static final public int inputSlot = 0;
    static final public int catalystSlot = 1;
    static final public int canisterSlot = 2;
    static final public int fuelSlot = 3;
    private static final int[][] inputCoords = {{1, 0}, {3, 0}, {8, 0}, {1, 0}};
    private static final int[][] outputCoords = {{5, 2}, {6, 2}, {7, 2}, {8, 2}};
    //// Static constants
    public static final int CONSUMED_ENERGY_PER_FUEL_REFILL = 100;
    public static final int CONSUMED_FUEL_PER_TIC = 20;
    public static int GENERATED_FUEL_PER_ENERGY_DRAIN = 100;

    public ExtractorTileEntity() {
        super(1);
        GENERATED_FUEL_PER_ENERGY_DRAIN
                = TileEntityFurnace.getItemBurnTime(new ItemStack(Items.COAL));
    }

    @Override
    public int getInputSize() {
        return 4;
    }

    @Override
    public int getOutputSize() {
        return 4;
    }

    @Override
    public int[][] getInputCoords() {
        return inputCoords;
    }

    @Override
    public int[][] getOutputCoords() {
        return outputCoords;
    }

    @Override
    public int[] getFuelIconCoordinate() {
        return new int[]{1, 1};
    }

    @Override
    public Tuple<Integer[], MachineGuiContainer.ProgressIcon> getProgressIconCoordinate() {
        return new Tuple<>(new Integer[]{3, 1}, MachineGuiContainer.ProgressIcon.BREWING);
    }

    @Override
    public int[] getProgressTextCoordinate() {
        return new int[]{5, 0};
    }

    @Override
    public List<ExtractorRecipe> getRecipes() {

        return KernCraftRecipes.EXTRACTOR_RECIPES;
    }

    @Override
    public boolean canSmelt(ExtractorRecipe recipe) {
        // No input no smelting
        if (input == null || input.getStackInSlot(inputSlot).isEmpty()) return false;

        // Input have same item and sufficient count
        if (recipe.item != null && (!OreDictionary.itemMatches(input.getStackInSlot(inputSlot), recipe.item, false)
                || input.getStackInSlot(inputSlot).getCount() < recipe.item.getCount())) {
            return false;
        }

        // Check catalyst, in no catalyst needed return true
        if (recipe.catalyst != null) return true;

        // If catalyst empty but required false
        if (input.getStackInSlot(catalystSlot).isEmpty()) return false;

        // Check item matching and count
        return !(!OreDictionary.itemMatches(input.getStackInSlot(catalystSlot), recipe.catalyst, false)
                || input.getStackInSlot(catalystSlot).getCount() < recipe.catalyst.getCount());
    }

    /**
     * Try and consume one unit of fuel (depending on recipe)
     * if it fails, return false.
     * @return
     */
    @Override
    public boolean tryProgress() {
        if (storedFuel < CONSUMED_FUEL_PER_TIC) {
            ItemStack fuelItem = input.getStackInSlot(fuelSlot);
            int fuel = TileEntityFurnace.getItemBurnTime(fuelItem);
            //GameRegistry.getFuelValue(fuelItem);
            if (storage.getEnergyStored() > CONSUMED_ENERGY_PER_FUEL_REFILL) {
                storage.extractEnergy(CONSUMED_ENERGY_PER_FUEL_REFILL, false);
                storedFuel += GENERATED_FUEL_PER_ENERGY_DRAIN;
            } else if (fuelItem != ItemStack.EMPTY &&
                    fuelItem.getCount() >= 1 &&
                    fuel > 0) {
                fuelItem.setCount(fuelItem.getCount() - 1);
                if (fuelItem.getCount() == 0) {
//                    fuelItem = null;
                    input.setStackInSlot(fuelSlot, ItemStack.EMPTY);
                }
                storedFuel += fuel;
            } else {
                return false;
            }
        }
        storedFuel -= CONSUMED_FUEL_PER_TIC;
        return true;
    }

    @Override
    public void doneSmelting() {
        if( input.getStackInSlot(inputSlot).isEmpty() ) return;

        input.getStackInSlot(inputSlot).splitStack(currentlySmelting.item.getCount());

        if (currentlySmelting.catalyst != null) {
            input.getStackInSlot(catalystSlot).splitStack(currentlySmelting.catalyst.getCount());
        }

        // For each Element output, flag telling if this has already been produced
        int[] remaining = new int[currentlySmelting.outputs.length];
        for (int i = 0; i < currentlySmelting.outputs.length; ++i) {
            if(currentlySmelting.outputs[i] == null) continue;
            remaining[i] = currentlySmelting.outputs[i].quantity * (Math.random() < currentlySmelting.outputs[i].probability ? 1 : 0);
        }

        // Place outputs in canisters, only if output can be placed
        // (i.e. there isn't another element inside.
        // Try each output slot
        for (int rec_slot = 0; rec_slot < getOutputCoords().length; ++rec_slot) {
            ItemStack out = output.getStackInSlot(rec_slot);
            if (out.isEmpty() || !ElementCapabilities.hasCapability(out)) {
                continue;
            }

            IElementContainer cap = ElementCapabilities.getCapability(out);

            // Try each generated element
            int recipeElementNumber = 0;
            for (ElementStack rec_out : currentlySmelting.outputs) {
                // If all output has been produced break
                if (remaining[recipeElementNumber] <= 0) {
                    ++recipeElementNumber;
                    continue;
                }

                remaining[recipeElementNumber] -= cap.addAmountOf(rec_out.id, remaining[recipeElementNumber], false);
                ++recipeElementNumber;
            }
        }

        boolean finished = true;
        for (int aRemaining : remaining) {
            if (aRemaining > 0) finished = false;
        }
        if (finished) return;

        // If there is a CANISTER in CANISTER slot, pull if to the output slot and
        // fill it with element
        // Try each output slot
        for (int rec_slot = 0; rec_slot < getOutputCoords().length; ++rec_slot) {

            ItemStack out = output.getStackInSlot(rec_slot);
            if (out.isEmpty()) {
                // Try each element
                for (int i = 0; i < currentlySmelting.outputs.length; ++i) {
                    ElementStack rec_out = currentlySmelting.outputs[i];
                    if(rec_out == null) { continue; }

                    if (out.isEmpty()) {
                        // If Element has not been produced and there is a CANISTER in
                        // the slot
                        ItemStack containerStack = input.getStackInSlot(canisterSlot);
                        if (containerStack.isEmpty() || !ElementCapabilities.hasCapability(containerStack)) {
                            continue;
                        }

                        // Try to add element to item in canister slot.
                        // If you can, pull the stack down
                        IElementContainer cap = ElementCapabilities.getCapability(containerStack);
                        if(cap == null) continue;
                        int tryAdd = cap.addAmountOf(rec_out.id, remaining[i], true);
                        if (tryAdd > 0) {
                            ItemStack newStack = containerStack.splitStack(1);
                            IElementContainer newCap = ElementCapabilities.getCapability(newStack);
                            if(newCap == null) continue;
                            remaining[i] -= newCap.addAmountOf(rec_out.id, remaining[i], false, this);
                            output.setStackInSlot(rec_slot, newStack);
                            out = newStack;
                        }
                    } else {
                        IElementContainer cap = ElementCapabilities.getCapability(out);
                        if(cap == null) continue;
                        remaining[i] -= cap.addAmountOf(rec_out.id, remaining[i], false, this);
                    }
                }
            }
        }
        this.markDirty();
    }


}
