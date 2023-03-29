package tfar.dimensionalquarry.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tfar.dimensionalquarry.init.ModOs;
import tfar.dimensionalquarry.network.PacketHandler;
import tfar.dimensionalquarry.network.client.S2CCustomSyncDataPacket;

import java.util.ArrayList;
import java.util.List;

public class FilterMenu extends AbstractContainerMenu {

    private List<Pair<String,Boolean>> predicates;

    ItemStack filter;

    public static final String TAG = "predicates";

    protected FilterMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    public FilterMenu(int i, Inventory inventory, ItemStack stack) {
        this(ModOs.FILTER_M,i);
        this.filter = stack;
        addPlayerSlots(inventory);
        predicates = loadPredicates(filter);
    }

    public static List<Pair<String,Boolean>> loadPredicates(ItemStack stack) {
        List<Pair<String,Boolean>> list = new ArrayList<>();

        CompoundTag tag = stack.getTagElement(TAG);
        if (tag != null) {
            for (String s : tag.getAllKeys()) {
                list.add(Pair.of(s,tag.getBoolean(s)));
            }
        }
        return list;
    }

    public static void savePredicates(List<Pair<String,Boolean>> predicates,ItemStack stack) {
        if (predicates.isEmpty()) {
            stack.removeTagKey(TAG);
        } else {
            CompoundTag tag = new CompoundTag();
            for (Pair<String,Boolean> predicate : predicates) {
                tag.putBoolean(predicate.getFirst(),predicate.getSecond());
            }
            stack.addTagElement(TAG,tag);
        }
    }

    public FilterMenu(int i, Inventory inventory) {
        this(i,inventory,ItemStack.EMPTY);
    }

    protected void addPlayerSlots(Inventory playerinventory) {
        int yStart = 104 + 36;
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

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    public void addPredicate(String s,boolean tag) {
        predicates.add(Pair.of(s,tag));
        markDirty();
    }

    public void markDirty() {
        savePredicates(predicates,filter);
    }

    public void sendToClient(ServerPlayer player) {
        PacketHandler.sendToClient(new S2CCustomSyncDataPacket(predicates),player);
    }

    public void setPredicates(List<Pair<String,Boolean>> predicates) {
        this.predicates = predicates;
    }

    public List<Pair<String, Boolean>> getPredicates() {
        return predicates;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
