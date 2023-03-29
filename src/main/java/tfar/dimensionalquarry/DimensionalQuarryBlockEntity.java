package tfar.dimensionalquarry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfar.dimensionalquarry.init.ModOs;
import tfar.dimensionalquarry.inv.DimenEnergyStorage;
import tfar.dimensionalquarry.menu.DimensionalQuarryMenu;
import tfar.dimensionalquarry.util.DimensionalUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DimensionalQuarryBlockEntity extends BlockEntity implements MenuProvider {

    private static final ResourceKey<Level> OVERWORLD = Level.OVERWORLD;

    private boolean active = true;
    private final DimenEnergyStorage energyStorage = new DimenEnergyStorage(DimensionalQuarryConfig.capacity);

    private List<ItemStack> backLog = new ArrayList<>();
    final ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
    private int cooldown = 0;

    private final ItemStackHandler handler = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (slot == 1) {
                bakeEnchantments(getStackInSlot(slot));
            }
            setChanged();
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            bakeEnchantments(getStackInSlot(1));
        }
    };

    protected void bakeEnchantments(ItemStack book) {
        tool.setTag(null);
        if (book.getItem() instanceof EnchantedBookItem) {
            Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(book);
            for (Map.Entry<Enchantment,Integer> entry : map1.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                if (enchantment.canEnchant(tool)) {
                    boolean compatible = true;
                    Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(tool);
                    for (Map.Entry<Enchantment,Integer> entry1 : map2.entrySet()) {
                        if (!enchantment.isCompatibleWith(entry1.getKey())) {
                            compatible = false;
                            break;
                        }
                    }
                    if (compatible) {
                        tool.enchant(enchantment,level);
                    }
                }
            }
        }
    }

    private final LazyOptional<IEnergyStorage> lazyOptional = LazyOptional.of(() -> energyStorage);

    private BlockPos.MutableBlockPos miningAt = new BlockPos.MutableBlockPos(0,0,0);

    private ServerLevel miningIn;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int pIndex) {
            int energyStored = energyStorage.getEnergyStored();

            return switch (pIndex) {
                case 0 -> energyStored & 0xffff;
                case 1 -> energyStored >>> 16;
                case 2 -> miningAt.getX();
                case 3 -> miningAt.getY();
                case 4 -> miningAt.getZ();
                default -> 0;
            };
        }

        @Override
        public void set(int pIndex, int pValue) {
            switch (pIndex) {
                case 0 -> energyStorage.setEnergy0(pValue);
                case 1 -> energyStorage.setEnergy1(pValue);
                case 2 -> miningAt.set(pValue,miningAt.getY(),miningAt.getZ());
                case 3 -> miningAt.set(miningAt.getX(),pValue,miningAt.getZ());
                case 4 -> miningAt.set(miningAt.getX(),miningAt.getY(),pValue);

            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public DimensionalQuarryBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public DimensionalQuarryBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(ModOs.DIMENSIONAL_QUARRY_BE, pPos, pBlockState);
    }

    private void setTarget(ResourceKey<Level> original) {
        if (!level.isClientSide) {
            miningIn = DimensionalUtils.getOrCreateCloneDimension(level.getServer(),level.getServer().getLevel(OVERWORLD));
        }
    }

    public void mineBlock() {
        if (miningIn != null) {
            BlockState state = miningIn.getBlockState(miningAt);
            FluidState fluidState = miningIn.getFluidState(miningAt);

            int energyCost = getEnergyCost(state,miningIn,miningAt);

            if (energyCost <= energyStorage.getEnergyStored()) {
                BlockEntity blockentity = miningIn.getBlockEntity(miningAt);
                LootContext.Builder lootcontext$builder = new LootContext.Builder(miningIn)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(miningAt.immutable())).withParameter(LootContextParams.BLOCK_STATE, state)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, null)
                        .withParameter(LootContextParams.TOOL, tool);
                backLog = state.getDrops(lootcontext$builder);
                energyStorage.extractEnergy(energyCost,false);
                movePos();
                setChanged();
            }
        } else {
            setTarget(null);
        }
    }

    public int getEnergyCost(BlockState state,Level level,BlockPos pos) {
        if (state.isAir()) return 0;

        int efficiency = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY,tool);
        double multiplier = 10d/ (10 + efficiency * efficiency);

        return (int) (multiplier * (DimensionalQuarryConfig.base_cost + DimensionalQuarryConfig.multiplier * state.getDestroySpeed(level,pos)));
    }

    //store items
    public boolean store() {
        for (Iterator<ItemStack> iterator = backLog.iterator(); iterator.hasNext(); ) {
            ItemStack stack = iterator.next();
            BlockPos above = getBlockPos().above();
            BlockEntity be = level.getBlockEntity(above);
            if (be != null) {

                IItemHandler remote = be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);

                if (remote != null) {
                    ItemStack reject = stack.copy();
                    for (int i = 0; i < remote.getSlots(); i++) {
                        reject = remote.insertItem(i, reject, false);
                        if (reject.isEmpty()) {
                            break;
                        }
                    }

                    if (reject.isEmpty()) {
                        iterator.remove();
                        continue;
                    }

                    int stored = stack.getCount() - reject.getCount();
                    if (stored > 0) {
                        stack.shrink(stored);
                    }
                }
            }
        }
        return backLog.isEmpty();
    }

    private void movePos() {
        int x = miningAt.getX();
        int y = miningAt.getY();
        int z = miningAt.getZ();
        //first, increase x
        boolean incrementZ = (x&15) == 15;
        boolean decrementY = incrementZ && (z&15) == 15;
        boolean nextChunk = incrementZ && y < miningIn.getMinBuildHeight();
        if (nextChunk) {
            int chunkX = 0;
            int chunkZ = 0;
            int yStart = DimensionalUtils.getHighestY((ServerLevel) level,new ChunkPos(chunkX,chunkZ));
            miningAt.set(chunkX *16,yStart,chunkZ*16);
        } else {
            if (decrementY) {
                miningAt.move(-15,-1,-15);
            } else {
                if (incrementZ) {
                    miningAt.move(-15,0,1);
                } else {
                    miningAt.move(1,0,0);
                }
            }
        }
    }

    private void moveChunk() {

    }

    public void charge(int coalBlocks) {
        energyStorage.receiveEnergy(200000 * coalBlocks,false);
        setChanged();
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DimensionalQuarryBlockEntity pBlockEntity) {
        if (pBlockEntity.active) {
            if (pBlockEntity.backLog.isEmpty()) {
                pBlockEntity.mineBlock();
            }
            boolean stored = pBlockEntity.store();
            if (!stored) {
                pBlockEntity.cooldown = DimensionalQuarryConfig.cooldown;
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.ENERGY.orEmpty(cap,lazyOptional);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("energy", energyStorage.serializeNBT());
        pTag.put("items",handler.serializeNBT());
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        energyStorage.deserializeNBT(pTag.get("energy"));
        handler.deserializeNBT(pTag.getCompound("items"));
    }

    @Override
    public Component getDisplayName() {
        return DimensionalQuarryBlock.CONTAINER_TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new DimensionalQuarryMenu(pContainerId,pPlayerInventory, ContainerLevelAccess.create(level,worldPosition),containerData,handler);
    }
}
