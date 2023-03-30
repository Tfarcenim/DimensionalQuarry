package tfar.dimensionalquarry.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.Nullable;
import tfar.dimensionalquarry.init.ModOs;
import tfar.dimensionalquarry.network.PacketHandler;
import tfar.dimensionalquarry.network.client.S2CCustomSyncDataPacket;

import java.util.HashMap;
import java.util.Map;

public class FilterMenu extends AbstractContainerMenu {

    private Map<String,Boolean> predicates;

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

    public static Map<String,Boolean> loadPredicates(ItemStack stack) {
        Map<String,Boolean> list = new HashMap<>();

        CompoundTag tag = stack.getTagElement(TAG);
        if (tag != null) {
            for (String s : tag.getAllKeys()) {
                list.put(s,tag.getBoolean(s));
            }
        }
        return list;
    }

    public static void savePredicates(Map<String,Boolean> predicates,ItemStack stack) {
        if (predicates.isEmpty()) {
            stack.removeTagKey(TAG);
        } else {
            CompoundTag tag = new CompoundTag();
            for (Map.Entry<String,Boolean> predicate : predicates.entrySet()) {
                tag.putBoolean(predicate.getKey(),predicate.getValue());
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

    public boolean addPredicate(String s,boolean tag) {
        if (tag) {
            ITag<Item> tag1 = ForgeRegistries.ITEMS.tags().getTag(TagKey.create(Registries.ITEM, new ResourceLocation(s)));
            if (!tag1.isEmpty()) {
                predicates.put(s,true);
                markDirty();
                return true;
            }
        } else {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(s));
            if (item != Items.AIR) {
                predicates.put(s, false);
                markDirty();
                return true;
            }
        }
        return false;
    }

    public boolean removePredicate(String i) {
        if (predicates.containsKey(i)) {
            predicates.remove(i);
            markDirty();
            return true;
        }
        return false;
    }

    public void markDirty() {
        savePredicates(predicates,filter);
    }

    public void sendToClient(ServerPlayer player) {
        PacketHandler.sendToClient(new S2CCustomSyncDataPacket(predicates),player);
    }

    public void setPredicates(Map<String,Boolean> predicates) {
        this.predicates = predicates;
    }

    public Map<String,Boolean> getPredicates() {
        return predicates;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
