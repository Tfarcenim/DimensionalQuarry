package tfar.dimensionalquarry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tfar.dimensionalquarry.init.ModOs;

public class DimensionalQuarryBlock extends Block implements EntityBlock {
    public DimensionalQuarryBlock(Properties pProperties) {
        super(pProperties);
    }

    public static final Component CONTAINER_TITLE = Component.translatable("container.dimensionalquarry");


    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        if (stack.is(Items.COAL_BLOCK)) {

            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            ((DimensionalQuarryBlockEntity)blockEntity).charge(stack.getCount());

            if (!pPlayer.getAbilities().instabuild) {
                stack.shrink(stack.getCount());
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }

        if (!pLevel.isClientSide)
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @javax.annotation.Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
    }


    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModOs.DIMENSIONAL_QUARRY_BE, DimensionalQuarryBlockEntity::tick);
    }


    @javax.annotation.Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DimensionalQuarryBlockEntity(pPos,pState);
    }
}
