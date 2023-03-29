package tfar.dimensionalquarry.network.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.util.S2CPacketHelper;

import java.util.ArrayList;
import java.util.List;

public class S2CCustomSyncDataPacket implements S2CPacketHelper {
    private final List<Pair<String,Boolean>> predicates;

    public S2CCustomSyncDataPacket(List<Pair<String,Boolean>> predicates) {
        this.predicates = predicates;
    }

    public S2CCustomSyncDataPacket(FriendlyByteBuf buf) {
        int i = buf.readShort();
        predicates = new ArrayList<>();
        for(int j = 0; j < i; ++j) {
            predicates.add(Pair.of(buf.readUtf(),buf.readBoolean()));
        }
    }

    @Override
    public void handleClient() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof FilterMenu filterMenu) {
            filterMenu.setPredicates(predicates);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(predicates.size());
        for (Pair<String, Boolean> stack : predicates) {
            buf.writeUtf(stack.getFirst());
            buf.writeBoolean(stack.getSecond());
        }
    }
}