package tfar.dimensionalquarry.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import tfar.dimensionalquarry.init.ModOs;

public class DimensionalQuarryMenu extends AbstractContainerMenu {
    private ContainerLevelAccess access;
    private ContainerData containerData;

    protected DimensionalQuarryMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public DimensionalQuarryMenu(int i, Inventory inventory, ContainerLevelAccess access, ContainerData containerData, ItemStackHandler handler) {
        this(ModOs.DIMENSIONAL_QUARRY_M,i);
        this.access = access;
        this.containerData = containerData;

        addSlot(new SlotItemHandler(handler,0,44,43));
        addSlot(new SlotItemHandler(handler,1,116,43));

        addPlayerSlots(inventory);
        addDataSlots(containerData);
    }

    public DimensionalQuarryMenu(int i, Inventory pContainerId) {
        this(i,pContainerId,ContainerLevelAccess.NULL,new SimpleContainerData(5),new ItemStackHandler(2));
    }

    protected void addPlayerSlots(Inventory playerinventory) {
        int yStart = 104;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + yStart;
                this.addSlot(new Slot(playerinventory, col + row * 9 + 9, x, y));
            }
        }

        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = yStart + 58;
            this.addSlot(new Slot(playerinventory, row, x, y));
        }
    }

    public BlockPos getMiningAt() {
        return new BlockPos(containerData.get(2),containerData.get(3),containerData.get(4));
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    public int getEnergyClient() {
        return containerData.get(0) + (containerData.get(1) << 16);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, ModOs.DIMENSIONAL_QUARRY_B);
    }
}
