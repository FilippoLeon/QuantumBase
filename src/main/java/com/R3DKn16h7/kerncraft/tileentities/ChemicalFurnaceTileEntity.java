package com.R3DKn16h7.kerncraft.tileentities;

public class ChemicalFurnaceTileEntity extends SmeltingTileEntity {

    // Slot IDs
    static final public int inputSlotStart = 0;
    static final public int inputSlotSize = 2;
    static final public int outputSlotStart = 0;
    static final public int outputSlotSize = 2;
    static final private int NumberOfSlots = 4;

    public ChemicalFurnaceTileEntity() {
        super(2, 2);
    }

    @Override
    public boolean canSmelt(ISmeltingRecipe rec) {
        return false;
    }

    @Override
    public boolean tryProgress() {
        return false;
    }

    @Override
    public void doneSmelting() {

    }
}