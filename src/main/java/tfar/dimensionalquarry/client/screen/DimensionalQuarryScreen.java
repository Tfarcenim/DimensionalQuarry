package tfar.dimensionalquarry.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tfar.dimensionalquarry.DimensionalQuarry;
import tfar.dimensionalquarry.DimensionalQuarryConfig;
import tfar.dimensionalquarry.inv.EnergyBarWidget;
import tfar.dimensionalquarry.menu.DimensionalQuarryMenu;

public class DimensionalQuarryScreen extends AbstractContainerScreen<DimensionalQuarryMenu> {
    public DimensionalQuarryScreen(DimensionalQuarryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        imageHeight += 20;
        titleLabelY -= 1;
        inventoryLabelY +=22;
    }
    static final ResourceLocation background = new ResourceLocation(DimensionalQuarry.MODID,"textures/screen/dimensional_quarry.png");

    static final String energyS = "container.dimensionalquarry.energy";

    @Override
    protected void init() {
        super.init();
        Button button = new EnergyBarWidget(leftPos+9,topPos+13,9,80,Component.empty(),b -> {}){
            @Override
            public void updateTooltip() {
                setTooltip(Tooltip.create(Component.translatable(energyS,menu.getEnergyClient(), DimensionalQuarryConfig.capacity)));
                super.updateTooltip();
            }
        };
        addRenderableOnly(button);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, background);
        blit(pPoseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int energy = menu.getEnergyClient();

        int h = (int) (energy * 80d / DimensionalQuarryConfig.capacity);

        int y1 = 80 - h;

        blit(pPoseStack, leftPos + 7,topPos + 13 + y1, 176,13, 9,h);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        BlockPos pos = menu.getMiningAt();
        String s = "Pos: "+pos.getX() + " "+ pos.getY() + " "+ pos.getZ();
        int w = font.width(s);
        this.font.draw(pPoseStack, s, imageWidth/2f-w/2f, 24, 0x404040);
    }
}
