package tfar.dimensionalquarry.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.PacketHandler;
import tfar.dimensionalquarry.network.util.C2SPacketHelper;


public class C2SRemovePredicatePacket implements C2SPacketHelper {

    private final String i;
    public C2SRemovePredicatePacket(String i) {
        this.i = i;
    }

    public C2SRemovePredicatePacket(FriendlyByteBuf buf) {
        i= buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(i);
    }

    public static void send(String i) {
        PacketHandler.sendToServer(new C2SRemovePredicatePacket(i));
    }

    public void handleServer(ServerPlayer player) {
        AbstractContainerMenu container = player.containerMenu;
        if (container instanceof FilterMenu filterMenu) {
            boolean success = filterMenu.removePredicate(i);
            if (success)
                filterMenu.sendToClient(player);
        }
    }
}

