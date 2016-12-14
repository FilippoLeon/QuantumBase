package com.R3DKn16h7.kerncraft.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Created by Filippo on 14/12/2016.
 */
public abstract class MachineTileEntity extends TileEntity
        implements ITickable {

    public final ItemStackHandler input;
    public final ItemStackHandler output;
    private final int inputSize;
    private final int outputSize;
    //private final IItemHandler automationInput = new ItemStackHandler(4);
    //private final IItemHandler automationOutput = new ItemStackHandler(4);

    public MachineTileEntity(int inputSize, int outputSize) {
        super();

        this.inputSize = inputSize;
        this.outputSize = outputSize;
        input = new ItemStackHandler(inputSize);
        output = new ItemStackHandler(outputSize);
    }

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    @Override
    public boolean hasCapability(Capability<?> capability,
                                 EnumFacing facing) {
        if (//capability == CapabilityEnergy.ENERGY ||
            //capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
                capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability,
                               EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(automationInput);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(input);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        input.deserializeNBT(nbt.getCompoundTag("Input"));
        output.deserializeNBT(nbt.getCompoundTag("Output"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setTag("Input", input.serializeNBT());
        nbt.setTag("Output", output.serializeNBT());
        return nbt;
    }
}