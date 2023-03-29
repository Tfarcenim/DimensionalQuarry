package tfar.dimensionalquarry.inv;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class EnergyBarWidget extends Button {
    public EnergyBarWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
    }

    public EnergyBarWidget(Builder builder) {
        super(builder);
    }

    @Override
    public void renderWidget(PoseStack p_275468_, int p_275505_, int p_275674_, float p_275696_) {

    }
}
