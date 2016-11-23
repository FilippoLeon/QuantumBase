package com.R3DKn16h7.quantumbase.plugins.JEI;

import com.R3DKn16h7.quantumbase.elements.ElementBase;
import com.R3DKn16h7.quantumbase.items.ModItems;
import com.R3DKn16h7.quantumbase.tileentities.ExtractorTileEntity;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filippo on 23/11/16.
 */
public class ExtractorJEIRecipeWrapper extends BlankRecipeWrapper {

    ExtractorTileEntity.ExtractorRecipe recipe;

    ExtractorJEIRecipeWrapper(ExtractorTileEntity.ExtractorRecipe recipe_) {
        recipe = recipe_;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ArrayList<ItemStack> inputs = new ArrayList<ItemStack>();

        inputs.add(new ItemStack(recipe.item));
        if(recipe.catalyst != null) inputs.add(new ItemStack(recipe.catalyst));


        inputs.add(new ItemStack(ModItems.canister));
        ingredients.setInputs(ItemStack.class, inputs);

        ArrayList<ItemStack> outputs = new ArrayList<ItemStack>();

        for (ExtractorTileEntity.ElementStack i : recipe.outs) {
            outputs.add(ElementBase.getItem(i));
        }
        ingredients.setOutputs(ItemStack.class, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft,
                         int recipeWidth, int recipeHeight,
                         int mouseX, int mouseY) {

        int j = 0;
        for (ExtractorTileEntity.ElementStack i : recipe.outs) {
            if (i.prob == 1) continue;
            String prob_string = String.format("%d%%",
                    (int) Math.floor(i.prob * 100));
            int color;
            if (i.prob < 0.9f) color = Color.red.getRGB();
            if (0.6f < i.prob && i.prob <= 0.9f) color = Color.yellow.getRGB();
            else color = Color.green.getRGB();

            minecraft.fontRendererObj.drawStringWithShadow(prob_string,
                    18 * (5 + j), 18 * 3 - 3, color);
            ++j;
        }
    }

    @Override
    public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {

    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(Minecraft minecraft,
                               int mouseX, int mouseY,
                               int mouseButton) {
        return false;
    }
}