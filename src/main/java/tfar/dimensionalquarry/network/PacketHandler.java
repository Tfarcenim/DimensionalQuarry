package tfar.dimensionalquarry.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tfar.dimensionalquarry.DimensionalQuarry;
import tfar.dimensionalquarry.network.client.S2CCustomSyncDataPacket;
import tfar.dimensionalquarry.network.server.C2SAddPredicatePacket;

public class PacketHandler {

    public static SimpleChannel INSTANCE;

    public static void registerMessages() {

        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(DimensionalQuarry.MODID, DimensionalQuarry.MODID), () -> "1.0", s -> true, s -> true);

        int i = 0;

        INSTANCE.registerMessage(i++,
                C2SAddPredicatePacket.class,
                C2SAddPredicatePacket::encode,
                C2SAddPredicatePacket::new,
                C2SAddPredicatePacket::handle);


        ///////

        INSTANCE.registerMessage(i++,
                S2CCustomSyncDataPacket.class,
                S2CCustomSyncDataPacket::encode,
                S2CCustomSyncDataPacket::new,
                S2CCustomSyncDataPacket::handle);
    }

    public static <MSG> void sendToClient(MSG packet, ServerPlayer player) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.sendToServer(packet);
    }
}
