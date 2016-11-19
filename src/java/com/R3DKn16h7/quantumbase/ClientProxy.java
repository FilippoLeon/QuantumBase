package com.R3DKn16h7.quantumbase;

import com.R3DKn16h7.quantumbase.blocks.ModBlocks;

import com.R3DKn16h7.quantumbase.blocks.BlockRenderRegister;
import com.R3DKn16h7.quantumbase.items.ItemRenderRegister;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        
        ModBlocks.initModels();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        ItemRenderRegister.registerItemRenderer();
        BlockRenderRegister.registerBlockRenderer();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
}
