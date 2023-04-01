package tfar.dimensionalquarry.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import tfar.dimensionalquarry.DimensionalQuarry;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.server.C2SAddPredicatePacket;
import tfar.dimensionalquarry.network.server.C2SRemovePredicatePacket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterScreen extends AbstractContainerScreen<FilterMenu> {

    private EditBox editBox;

    private DetailsList list;
    private Button removePredicateB;


    public FilterScreen(FilterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        imageHeight += 56;
        inventoryLabelY +=56;
    }
    static final ResourceLocation background = new ResourceLocation(DimensionalQuarry.MODID,"textures/screen/filter.png");

    public void containerTick() {
        super.containerTick();
        this.editBox.tick();
        this.list.tick();
    }


    @Override
    protected void init() {
        super.init();
        initList(false);
        this.addRenderableWidget(Button.builder(Component.literal("+"),this::addPredicate).size(14,14).pos(leftPos+134,topPos+17).build());
        removePredicateB = Button.builder(Component.literal("-"),this::removePredicate).size(14,14).pos(leftPos+134,topPos+32).build();
        this.addRenderableWidget(removePredicateB);
        initEditbox();
    }

    public void initList(boolean refresh) {
        if (refresh) {
            removeWidget(list);
        }
        this.list = new DetailsList(menu.getPredicates());
        list.setRenderBackground(false);
        list.setLeftPos(leftPos);
        list.setRenderTopAndBottom(false);
        this.addRenderableWidget(this.list);
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String s = this.editBox.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.editBox.setValue(s);
    }

    private void addPredicate(Button b) {

        ItemStack stack = menu.getItemStackHandler().getStackInSlot(0);

        if (!stack.isEmpty()) {
            C2SAddPredicatePacket.send(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), false);
        } else {
            boolean tag = editBox.getValue().startsWith("#");
            C2SAddPredicatePacket.send(editBox.getValue(), tag);
        }
    }

    private void removePredicate(Button b) {
        DetailsList.Entry selected = list.getSelected();
        if (selected != null) {
        C2SRemovePredicatePacket.send(selected.string);
        }
    }

    protected void initEditbox() {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.editBox = new EditBox(this.font, i + 8, j + 18, 123, 12, Component.translatable("container.repair"));
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

        private int frame;
        public DetailsList(Map<String, Boolean> predicates) {
            super(FilterScreen.this.minecraft, FilterScreen.this.imageWidth, FilterScreen.this.imageHeight, topPos + 48,topPos + 48 + 78, 18);

            for (Map.Entry<String, Boolean> predicate : predicates.entrySet()) {
                String s = predicate.getKey();
                boolean tag = predicate.getValue();
                if (tag) {
                    addEntry(new Entry(ForgeRegistries.ITEMS.tags().getTag(TagKey.create(Registries.ITEM,new ResourceLocation(predicate.getKey()))).stream().map(Item::getDefaultInstance).collect(Collectors.toList()),s));
                } else {
                    this.addEntry(new Entry(List.of(BuiltInRegistries.ITEM.get(new ResourceLocation(predicate.getKey())).getDefaultInstance()),s));
                }
            }
        }


        protected void renderItem(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
            FilterScreen.DetailsList.Entry e = this.getEntry(pIndex);
            e.renderBack(pPoseStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, Objects.equals(this.getHovered(), e), pPartialTick);
            if (this.renderSelection && this.isSelectedItem(pIndex)) {
                int i = this.isFocused() ? 0xffffffff : 0xff808080;//color of border
                this.renderSelection(pPoseStack, pTop, pWidth, pHeight, i, 0xffffcccc);//color of background selection
            }

            e.render(pPoseStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, Objects.equals(this.getHovered(), e), pPartialTick);
        }

           public boolean isFocused() {
           return FilterScreen.this.getFocused() == this;
        }

        public void tick() {
            ++frame;
        }

        @Override
        public int getRowWidth() {
            return imageWidth - 6;
        }

        protected int getScrollbarPosition() {
            return 1000;//this.width/2 + 60;
        }

        class Entry extends ObjectSelectionList.Entry<FilterScreen.DetailsList.Entry> {
            private final List<ItemStack> stacks;
            private final String string;
            public Entry(List<ItemStack> stacks,String string) {
                this.stacks = stacks;
                this.string = string;
            }

            public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                ItemStack itemstack = getStack();
                int offset = -1;
                int color = 0xff0000;
                this.blitSlot(pPoseStack, pLeft+offset, pTop - 2, itemstack);
                FilterScreen.this.font.draw(pPoseStack, itemstack.getHoverName(), pLeft + offset + 20, pTop + 4, color);
                Component component = stacks.size() > 1 ? Component.literal("Tag") : Component.literal("Item");

                FilterScreen.this.font.draw(pPoseStack, component, pLeft + 166 - FilterScreen.this.font.width(component), pTop + 3, color);
            }


            protected ItemStack getStack() {
                int o = (frame/20) % stacks.size();
                return stacks.get(o);
            }

            public Component getNarration() {
                ItemStack itemstack = getStack();
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
                this.blitSlotBg(pPoseStack, pX, pY);
                if (!pStack.isEmpty()) {
                    FilterScreen.this.itemRenderer.renderGuiItem(pPoseStack,pStack, pX + 1, pY + 1);
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
