package tfar.dimensionalquarry.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import tfar.dimensionalquarry.DimensionalQuarry;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.PacketHandler;
import tfar.dimensionalquarry.network.server.C2SAddPredicatePacket;

import javax.annotation.Nullable;
import java.util.List;

public class FilterScreen extends AbstractContainerScreen<FilterMenu> {

    private EditBox editBox;

    private DetailsList list;


    public FilterScreen(FilterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        imageHeight += 56;
        titleLabelY += 17;
        inventoryLabelY +=56;
    }
    static final ResourceLocation background = new ResourceLocation(DimensionalQuarry.MODID,"textures/screen/filter.png");

    public void containerTick() {
        super.containerTick();
        this.editBox.tick();
    }


    @Override
    protected void init() {
        super.init();
        this.list = new DetailsList(menu.getPredicates());
        list.setRenderBackground(false);
        list.setLeftPos(leftPos);
        list.setRenderTopAndBottom(false);
        this.addRenderableWidget(this.list);
        this.addRenderableWidget(Button.builder(Component.literal("+"),this::addPredicate).size(14,14).pos(leftPos+157,topPos+16).build());
        initEditbox();
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String s = this.editBox.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.editBox.setValue(s);
    }

    private void addPredicate(Button b) {
        PacketHandler.sendToServer(new C2SAddPredicatePacket(editBox.getValue(),false));
    }

    protected void initEditbox() {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.editBox = new EditBox(this.font, i + 8, j + 17, 145, 12, Component.translatable("container.repair"));
        this.editBox.setCanLoseFocus(true);
        this.editBox.setTextColor(-1);
        this.editBox.setTextColorUneditable(-1);
        this.editBox.setMaxLength(64);
        this.editBox.setResponder(this::onNameChanged);
        this.editBox.setValue("");
        this.editBox.setTextColor(0xff80ff80);
        this.addWidget(this.editBox);
        this.setInitialFocus(this.editBox);
    }

    private void onNameChanged(String string) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
        }

        if (this.editBox.keyPressed(keyCode, scanCode, modifiers) || this.editBox.canConsumeInput()) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTick);
        this.editBox.render(matrices, mouseX, mouseY, partialTick);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, background);
        blit(pPoseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }


    class DetailsList extends ObjectSelectionList<FilterScreen.DetailsList.Entry> {
        public DetailsList(List<Pair<String, Boolean>> predicates) {
            super(FilterScreen.this.minecraft, FilterScreen.this.imageWidth, FilterScreen.this.imageHeight, topPos + 38, FilterScreen.this.height, 24);

            for (Pair<String, Boolean> predicate : predicates) {
                this.addEntry(new Entry(predicate.getSecond()));
            }

        }

        public void setSelected(@Nullable FilterScreen.DetailsList.Entry pEntry) {
            super.setSelected(pEntry);
           // FilterScreen.this.updateButtonValidity();
        }

     //   protected boolean isFocused() {
    //       return FilterScreen.this.getFocused() == this;
     //   }

        protected int getScrollbarPosition() {
            return this.width - 70;
        }

        class Entry extends ObjectSelectionList.Entry<FilterScreen.DetailsList.Entry> {

            private boolean tag;

            public Entry(boolean tag) {
                this.tag = tag;
            }

            public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                ItemStack itemstack = new ItemStack(Items.COBBLESTONE);
                this.blitSlot(pPoseStack, pLeft, pTop, itemstack);
                FilterScreen.this.font.draw(pPoseStack, itemstack.getHoverName(), (float)(pLeft + 18 + 5), (float)(pTop + 3), 0xffffff);
                Component component = tag ? Component.literal("Tag") : Component.literal("Item");

                FilterScreen.this.font.draw(pPoseStack, component, (float)(pLeft + 2 + 213 - FilterScreen.this.font.width(component)), (float)(pTop + 3), 0xffffff);
            }

            public Component getNarration() {
                ItemStack itemstack = new ItemStack(Items.COBBLESTONE);
                return !itemstack.isEmpty() ? Component.translatable("narrator.select", itemstack.getHoverName()) : CommonComponents.EMPTY;
            }

            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                if (pButton == 0) {
                    FilterScreen.DetailsList.this.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }

            private void blitSlot(PoseStack pPoseStack, int pX, int pY, ItemStack pStack) {
                this.blitSlotBg(pPoseStack, pX + 1, pY + 1);
                if (!pStack.isEmpty()) {
                    FilterScreen.this.itemRenderer.renderGuiItem(pPoseStack,pStack, pX + 2, pY + 2);
                }

            }

            private void blitSlotBg(PoseStack pPoseStack, int pX, int pY) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GuiComponent.STATS_ICON_LOCATION);
                GuiComponent.blit(pPoseStack, pX, pY, 0, 0.0F, 0.0F, 18, 18, 128, 128);
            }
        }
    }
}
