package dev.zontreck.otemod.implementation.scrubber;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.zontreck.otemod.OTEMod;
import dev.zontreck.otemod.implementation.MouseHelpers;
import dev.zontreck.otemod.implementation.energy.screenrenderer.EnergyInfoArea;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemScrubberScreen extends AbstractContainerScreen<ItemScrubberMenu>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(OTEMod.MOD_ID, "textures/gui/item_scrubber_gui.png");

    private EnergyInfoArea EIA;

    public ItemScrubberScreen(ItemScrubberMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);

        
        this.topPos=0;
        this.leftPos=0;
        
        this.imageWidth = 208;
        this.imageHeight = 165;
    }

    @Override
    protected void init()
    {
        super.init();
        assignEnergyArea();
    }

    private void assignEnergyArea() {
        int x = (width - imageWidth )/2;
        int y = (height - imageHeight)/2;

        EIA = new EnergyInfoArea(x+188, y+69, menu.entity.getEnergyStorage(), 7, 72);
    }

    @Override
    protected void renderBg(GuiGraphics poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        

        poseStack.blit(TEXTURE, this.leftPos, this.topPos, 0,0, imageWidth, imageHeight);
        renderUncraftingProgress(poseStack);
        EIA.draw(poseStack);
    }

    
    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY)
    {
        stack.drawString(font, this.title.getString(), 63, 12, 0xFFFFFF);
        
        int x = (width - imageWidth )/2;
        int y = (height - imageHeight)/2;
        renderEnergy(stack, mouseX, mouseY, x, y);
        //this.font.draw(stack, this.playerInventoryTitle.getString(), this.leftPos + 17, this.topPos + 123, 0xFFFFFF);
    }

    private void renderEnergy(GuiGraphics stack, int mouseX, int mouseY, int x, int y) {
        if(isMouseAbove(mouseX, mouseY, x, y, 188, 69, 7, 72)){
            stack.renderTooltip(font, EIA.getTooltips(), Optional.empty(), mouseX-x, mouseY-y);
        }
    }

    private void renderUncraftingProgress(GuiGraphics stack)
    {
        if(menu.isCrafting())
        {
            stack.blit(TEXTURE, leftPos+42, topPos+45, 1, 168, menu.getScaledProgress(),6);
        }
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float delta)
    {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        renderTooltip(stack, mouseX, mouseY);
    }

    private boolean isMouseAbove(int mouseX, int mouseY, int x, int y, int offsetX, int offsetY, int width, int height)
    {
        return MouseHelpers.isMouseOver(mouseX, mouseY, x+offsetX, y+offsetY, width, height);
    }
    
}
