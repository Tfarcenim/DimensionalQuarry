package tfar.dimensionalquarry.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.PacketHandler;
import tfar.dimensionalquarry.network.util.C2SPacketHelper;


public class C2SAddPredicatePacket implements C2SPacketHelper {

    private final String string;
    private final boolean tag;
    public C2SAddPredicatePacket(String string, boolean tag) {
        this.string = string;
        this.tag = tag;
    }

    public C2SAddPredicatePacket(FriendlyByteBuf buf) {
        string = buf.readUtf();
        tag = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(string);
        buf.writeBoolean(tag);
    }

    public static void send(String id,boolean tag) {
        PacketHandler.sendToServer(new C2SAddPredicatePacket(id,tag));
    }

    public void handleServer(ServerPlayer player) {
        AbstractContainerMenu container = player.containerMenu;
        if (container instanceof FilterMenu filterMenu) {
            filterMenu.addPredicate(string,tag);
        }
    }
}

