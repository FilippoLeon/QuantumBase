package com.R3DKn16h7.kerncraft.tileentities;

import com.R3DKn16h7.kerncraft.elements.ElementBase;
import com.R3DKn16h7.kerncraft.items.Canister;
import com.R3DKn16h7.kerncraft.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;

public class ExtractorTileEntity extends TileEntity
        implements ITickable, IRedstoneSettable {


    // Slot IDs
    static final public int inputSlot = 0;
    static final public int catalystSlot = 1;
    static final public int canisterSlot = 2;
    static final public int fuelSlot = 3;
    static final public int outputSlotStart = 4;
    static final public int outputSlotSize = 4;
    static final private int NumberOfSlots = 8;
    //// Static constants
    private final static String name = "Extractor";
    static private final float ticTime = 5f;
    static private final int consumedEnergyPerFuelRefill = 100;
    static private final int generatedFuelPerEnergyDrain = 100;
    static private final int consumedFuelPerTic = 20;
    // Static register of recipes
    static public ArrayList<ExtractorRecipe> recipes;
    public final ItemStackHandler input = new ItemStackHandler(4);
    public final ItemStackHandler output = new ItemStackHandler(4);
    private final IItemHandler automationInput = new ItemStackHandler(4);
    private final IItemHandler automationOutput = new ItemStackHandler(4);
    // Internal storages
    public EnergyStorage storage = new EnergyStorage(1000);
    public FluidTank tank = new FluidTank(1000);
    // Has the input changed since last check?
    public boolean inputChanged = false;
    int mode = 0;
    //// Status variables
    // Are we currently smelting
    private boolean smelting = false;
    // Recipe Id currently smelting
    private int smeltingId = -1;
    // Current progress oin smelting
    private int progress;
    private int storedFuel = 0;
    //
    private float elapsed = 0f;

    public ExtractorTileEntity() {
        if (recipes == null) {
            recipes = new ArrayList<ExtractorRecipe>();
            registerRecipe(Item.getItemFromBlock(Blocks.IRON_ORE),
                    Item.getItemFromBlock(Blocks.ANVIL),
                    new ElementStack[]{new ElementStack(1, 1), new ElementStack(4, 1)}, 10);
            registerRecipe(Item.getItemFromBlock(Blocks.DIAMOND_ORE),
                    Item.getItemFromBlock(Blocks.LAPIS_BLOCK),
                    new ElementStack[]{new ElementStack(1, 1), new ElementStack(5, 5)}, 10);
            registerRecipe(Item.getItemFromBlock(Blocks.DIAMOND_BLOCK),
                    null,
                    new ElementStack[]{
                            new ElementStack("Be", 4, 0.5f),
                            new ElementStack("Ar", 5)
                    },
                    10);
            registerRecipe(Item.getItemFromBlock(Blocks.IRON_BLOCK),
                    null,
                    new ElementStack[]{
                            new ElementStack("Pu", 400, 0.5f),
                            new ElementStack("Pm", 5),
                            new ElementStack("H", 1, 0.8f),
                            new ElementStack("He", 2)
                    },
                    10);
        }
    }

    /**
     * Add new recipe
     * @param item Principal item that will allow to extract elements.
     * @param catalyst Additional item (optional) that acts as Catalyst
     * @param outs LAB_BONNET list of output Elements
     * @param energy The required energy (TODO: change to "cost") to perform a smelting operation.
     * @return Return false if registration failed (TODO)
     */
    static public boolean registerRecipe(Item item, Item catalyst,
                                         ElementStack[] outs, int energy) {
        recipes.add(new ExtractorRecipe(item, catalyst, outs, energy));
        return true;
    }

    public IItemHandler getInput()
    {
        return input;
    }

    public IItemHandler getOutput()
    {
        return output;
    }

    @Override
    public boolean hasCapability(Capability<?> capability,
                                 EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY ||
                capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
                capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability,
                               EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(storage);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            //return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(automationInput);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(input);
        }
        return super.getCapability(capability, facing);
    }

    public float getProgressPerc() {
        if(smeltingId == -1) return 0f;
        return progress / (float) recipes.get(smeltingId).energy;
    }

    public boolean canSmelt() {
        return canSmelt(smeltingId);
    }

    /**
     * Return true if recipe idx can be smelted.
     * @param idx
     * @return
     */
    public boolean canSmelt(int idx) {
        if (idx == -1) return false;
        ExtractorRecipe recipe = recipes.get(idx);
        return !(input == null ||
                input.getStackInSlot(inputSlot) == null ||
                // Check input
                input.getStackInSlot(inputSlot).getItem() != recipe.item ||
                input.getStackInSlot(inputSlot).stackSize < 1 ||
                // Check catalyst
                recipe.catalyst != null &&
                        (input.getStackInSlot(catalystSlot) == null ||
                                input.getStackInSlot(catalystSlot).getItem() != recipe.catalyst ||
                                input.getStackInSlot(catalystSlot).stackSize < 1
                        ));
    }

    /**
     * Try and consume one unit of fuel (depending on recipe)
     * if it fails, return false.
     * @return
     */
    public boolean tryConsumeFuel() {

        ExtractorRecipe recipe = recipes.get(smeltingId);
        if (storedFuel < consumedFuelPerTic) {
            ItemStack fuelItem = input.getStackInSlot(fuelSlot);
            int fuel = TileEntityFurnace.getItemBurnTime(fuelItem); //GameRegistry.getFuelValue(fuelItem);
            if (storage.getEnergyStored() > consumedEnergyPerFuelRefill) {
                storage.extractEnergy(consumedEnergyPerFuelRefill, false);
                storedFuel += generatedFuelPerEnergyDrain;
            } else if (fuelItem != null &&
                    fuelItem.stackSize >= 1 &&
                    fuel > 0) {
                fuelItem.stackSize -= 1;
                if (fuelItem.stackSize == 0) {
                    fuelItem = null;
                    input.setStackInSlot(fuelSlot, null);
                }
                storedFuel += fuel;
            } else {
                return false;
            }
        }
        storedFuel -= consumedFuelPerTic;
        return true;
    }

    /**
     * Do "one tick" of smelting, check if recipe changed, and if it
     * is possible to continue smelting.
     */
    public void progressSmelting() {

        if (mode == 0 && !this.worldObj.isBlockPowered(pos)) return;
        if (mode == 1 && this.worldObj.isBlockPowered(pos)) return;

        if (inputChanged) {
            findRecipe();
            inputChanged = false;
        }
        if (canSmelt()) {
            if( !tryConsumeFuel() ) return;
            ++progress;
            if (progress >= recipes.get(smeltingId).energy) {
                progress = 0;
                doneSmelting();
            }
        } else {
            progress = 0;
        }
    }

    public void doneSmelting() {
        input.setStackInSlot(inputSlot, new ItemStack(input.getStackInSlot(inputSlot).getItem(),
                input.getStackInSlot(inputSlot).stackSize - 1));
        if (input.getStackInSlot(inputSlot).stackSize == 0) input.setStackInSlot(inputSlot, null);
        if (recipes.get(smeltingId).catalyst != null) {
            input.setStackInSlot(catalystSlot, new ItemStack(input.getStackInSlot(catalystSlot).getItem(),
                    input.getStackInSlot(catalystSlot).stackSize - 1));
            if (input.getStackInSlot(catalystSlot).stackSize == 0) input.setStackInSlot(catalystSlot, null);
        }

        // For each Element output, flag telling if this has already been produced
        int[] remaining = new int[recipes.get(smeltingId).outs.length];
        for (int i = 0; i < recipes.get(smeltingId).outs.length; ++i) {
            remaining[i] = recipes.get(smeltingId).outs[i].quantity;
        }

        // Place outputs in canisters, only if output can be placed (i.e. there isn't another element inside.
        // Try each output slot
        for (int rec_slot = 0; rec_slot < outputSlotSize; ++rec_slot) {
            ItemStack out = output.getStackInSlot(rec_slot);
            if (out == null) {
                continue;
            }

            NBTTagCompound nbt;
            if (out.hasTagCompound()) {
                nbt = out.getTagCompound();
            } else {
                nbt = new NBTTagCompound();
            }

            // Try each generated element
            int i = 0;
            for (ElementStack rec_out : recipes.get(smeltingId).outs) {
                // If all output has been produced break
                if (remaining[i] <= 0) {
                    ++i;
                    continue;
                }
                if (nbt.hasKey("Element")) {
                    int elemId = nbt.getInteger("Element");
                    int qty = nbt.getInteger("Quantity");
                    if (elemId == rec_out.id) {
                        if (remaining[i] + qty <= Canister.CAPACITY) {
                            nbt.setInteger("Quantity", remaining[i] + qty);
                            remaining[i] = 0;
                            out.setTagCompound(nbt);
                            break;
                        } else {
                            remaining[i] -= Canister.CAPACITY - qty;
                            nbt.setInteger("Quantity", Canister.CAPACITY);
                            out.setTagCompound(nbt);
                        }
                    }
                } else {
                    nbt.setInteger("Element", rec_out.id);
                    nbt.setInteger("Quantity", rec_out.quantity);
                    out.setTagCompound(nbt);
                    remaining[i] = 0;
                    break;
                }
                ++i;
            }

        }

        // If there is a CANISTER in CANISTER slot, pull if to the output slot and
        // fill it with element
        // Try each output slot
        for (int rec_slot = 0; rec_slot < outputSlotSize; ++rec_slot) {

            ItemStack out = output.getStackInSlot(rec_slot);
            if (out == null) {
                // Try each element
                int i = 0;
                for (ElementStack rec_out : recipes.get(smeltingId).outs) {

                    // If Element has not been produced and there is a CANISTER in
                    // the slot
                    if (remaining[i] > 0 && input.getStackInSlot(canisterSlot) != null &&
                            input.getStackInSlot(canisterSlot).stackSize > 0 &&
                            input.getStackInSlot(canisterSlot).getItem() == ModItems.CANISTER) {
                        remaining[i] = 0;
                        output.setStackInSlot(rec_slot, new ItemStack(input.getStackInSlot(canisterSlot).getItem(), 1));
                        input.setStackInSlot(canisterSlot, new ItemStack(input.getStackInSlot(canisterSlot).getItem(),
                                input.getStackInSlot(canisterSlot).stackSize - 1));
                        if (input.getStackInSlot(canisterSlot).stackSize == 0) input.setStackInSlot(canisterSlot, null);
                        NBTTagCompound nbt = new NBTTagCompound();
                        nbt.setInteger("Element", rec_out.id);
                        nbt.setInteger("Quantity", rec_out.quantity);
                        output.getStackInSlot(rec_slot).setTagCompound(nbt);
                        break;
                    }

                    ++i;
                }
            }
        }
        this.markDirty();
    }

    /**
     *
     */
    public void abortSmelting() {
        smelting = false;
        smeltingId = -1;
    }

    /**
     * Find recipe that matches the input/catalyst slots
     */
    public void findRecipe() {
        int idx = 0;
        for (ExtractorRecipe recipe : recipes) {
            if (canSmelt(idx)) {
                smeltingId = idx;
                smelting = true;
                return;
            }

            ++idx;
        }
        abortSmelting();
    }

    /**
     * Update entity: check if needs to smelt item
     */
    @Override
    public void update() {
        if (elapsed > ticTime) {
            progressSmelting();
            elapsed = 0;
        } else {
            elapsed += 1;
        }
    }

    public float getFuelStoredPercentage() {
        return Math.min(storedFuel / (float)
                        TileEntityFurnace.getItemBurnTime(new ItemStack(Items.COAL)),
                1.0f
        );
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        System.out.println("adssaddsadsadsadsadsadsadsadsadsa");
        input.deserializeNBT(nbt.getCompoundTag("Input"));
        output.deserializeNBT(nbt.getCompoundTag("Output"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);
        System.out.println("WWWWadssaddsadsadsadsadsadsadsadsadsa");
        nbt.setTag("Input", input.serializeNBT());
        nbt.setTag("Output", output.serializeNBT());
        return nbt;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    static public class ElementStack {
        public int id;
        public int quantity;
        public float prob = 1.0f;

        public ElementStack(int id_, int quantity_) {
            id = id_;
            quantity = quantity_;
        }

        public ElementStack(String name_, int quantity_) {
            id = ElementBase.symbolToId(name_);
            quantity = quantity_;
        }

        public ElementStack(int id_, int quantity_, float prob_) {
            this(id_, quantity_);
            prob = prob_;
        }

        public ElementStack(String name_, int quantity_, float prob_) {
            this(name_, quantity_);
            prob = prob_;
        }
    }

    /**
     * Represents the recipe for the extractor.
     */
    static public class ExtractorRecipe {
        public Item item;
        public Item catalyst;
        public ElementStack[] outs;
        public int energy;

        public ExtractorRecipe(Item item_, Item catalyst_,
                               ElementStack[] outs_, int energy_) {
            item = item_;
            catalyst = catalyst_;
            outs = outs_;
            energy = energy_;
        }
    }
}
