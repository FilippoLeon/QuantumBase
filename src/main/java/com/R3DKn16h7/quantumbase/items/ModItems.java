package com.R3DKn16h7.quantumbase.items;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;

public class ModItems {
    public static Item portableBeacon;
    public static Canister canister;
    public static ExtraShield EXTRA_SHIELD;

    public static LabCoat A,B,C,D;
    public static void createItems() {

        portableBeacon = new PortableBeacon();
        canister = new Canister();
        EXTRA_SHIELD = new ExtraShield();

        A = new LabCoat(EntityEquipmentSlot.HEAD);
        B = new LabCoat(EntityEquipmentSlot.CHEST);
        C = new LabCoat(EntityEquipmentSlot.FEET);
        D = new LabCoat(EntityEquipmentSlot.LEGS);
//        IRenderFactory<ExtraShield> renderFactory;
//        RenderingRegistry.registerEntityRenderingHandler(ExtraShield.class, renderFactory);
    }
}
