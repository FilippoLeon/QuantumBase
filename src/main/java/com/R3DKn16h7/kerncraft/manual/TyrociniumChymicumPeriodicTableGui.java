package com.R3DKn16h7.kerncraft.manual;

import com.R3DKn16h7.kerncraft.client.gui.widgets.BetterButton;
import com.R3DKn16h7.kerncraft.client.gui.widgets.Widget;
import com.R3DKn16h7.kerncraft.elements.Element;
import com.R3DKn16h7.kerncraft.elements.ElementRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Filippo on 08/12/2016.
 */
public class TyrociniumChymicumPeriodicTableGui extends TyrociniumChymicumGui {

    static int STARTING_ID = 20;
    private ArrayList<GuiButton> periodicButtonList =
            new ArrayList<>(ElementRegistry.NUMBER_OF_ELEMENTS);

    public TyrociniumChymicumPeriodicTableGui() {

    }

    @Override
    public void superPage() {
        super.superPage();
        mc.displayGuiScreen(new TyrociniumChymicumIndexGui());
    }

    @Override
    public void initGui() {
        super.initGui();

        int id = STARTING_ID;
        int x = 0;
        int y = 0;
        int a = 0;
        int b = 0;
        int padLeft = guiLeft + 10;
        int padTop = guiTop + 20;
        for (Element element : ElementRegistry.getElements()) {
            int g = element.group;
            int p = element.period;
            if (g == Element.Group.ACTINIDE.getValue()) {
                g = 3 + a++;
                p += 3;
            } else if (g == Element.Group.LANTHANIDE.getValue()) {
                g = 3 + b++;
                p += 3;
            }

            String formatting = element.state.toColor();

            Color color = element.family.toColor();

            GuiButton btn = new BetterButton(this, id++, 13 * (g - 1) + padLeft, 13 * (p - 1) + padTop,
                    14, 14)
                    .setText(formatting + element.symbol)
                    .setTint(color)
                    .setAlignment(Widget.Alignment.MIDDLE);
            addButton(btn);
            periodicButtonList.add(btn);
        }

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id >= STARTING_ID && button.id < STARTING_ID + ElementRegistry.NUMBER_OF_ELEMENTS) {
            mc.displayGuiScreen(new TyrociniumChymicumElementGui(ElementRegistry.getElement(button.id - STARTING_ID + 1)));
        }
    }

    @Override
    public void drawBackground(int par1, int par2, float par3) {

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(
                new ResourceLocation("kerncraft:textures/gui/blank_paper.png")
        );

        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    }

    @Override
    public String getTitle() {
        return I18n.format("manual.title.periodic_table");
    } // "of Elements"

    @Override
    public void drawScreen(int par1, int mouseY, float par3) {
        super.drawScreen(par1, mouseY, par3);

        List<String> str = new ArrayList<String>();
        for (GuiButton btn : periodicButtonList) {
            if (par1 > btn.x && par1 < btn.x + btn.width &&
                    mouseY > btn.y && mouseY < btn.y + btn.height) {
                Element elem = ElementRegistry.getElement(btn.id - STARTING_ID + 1);


                String formatting = elem.state.toColor();

                str.add(formatting + StringUtils.capitalize(elem.getName()) + " (" + elem.symbol + ")");
                str.add(formatting + "Element " + elem.id);
                break;
            }
        }
        drawHoveringText(str, par1, mouseY);
    }

}
