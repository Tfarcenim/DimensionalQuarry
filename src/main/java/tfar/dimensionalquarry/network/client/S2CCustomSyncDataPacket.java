package tfar.dimensionalquarry.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import tfar.dimensionalquarry.client.screen.FilterScreen;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.util.S2CPacketHelper;

import java.util.HashMap;
import java.util.Map;

public class S2CCustomSyncDataPacket implements S2CPacketHelper {
    private final Map<String,Boolean> predicates;

    public S2CCustomSyncDataPacket(Map<String,Boolean> predicates) {
        this.predicates = predicates;
    }

    public S2CCustomSyncDataPacket(FriendlyByteBuf buf) {
        int i = buf.readShort();
        predicates = new HashMap<>();
        for(int j = 0; j < i; ++j) {
            predicates.put(buf.readUtf(),buf.readBoolean());
        }
    }

    @Override
    public void handleClient() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof FilterMenu filterMenu) {
            filterMenu.setPredicates(predicates);
            ((FilterScreen)Minecraft.getInstance().screen).initList(true);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(predicates.size());
        for (Map.Entry<String, Boolean> stack : predicates.entrySet()) {
            buf.writeUtf(stack.getKey());
            buf.writeBoolean(stack.getValue());
        }
    }
}