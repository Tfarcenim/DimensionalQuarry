package tfar.dimensionalquarry.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import tfar.dimensionalquarry.menu.FilterMenu;

public class FilterItem extends Item {
    public FilterItem(Properties pProperties) {
        super(pProperties);
    }

    public static final Component CONTAINER_TITLE = Component.translatable("container.dimensionalquarry.filter");

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide) {
            NetworkHooks.openScreen(
                    (ServerPlayer) pPlayer,
                    new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer1) -> new FilterMenu(pContainerId,pPlayerInventory,stack),
                    CONTAINER_TITLE),buf -> buf.writeItem(stack));
        }

        return InteractionResultHolder.sidedSuccess(stack,pLevel.isClientSide);
    }
}
