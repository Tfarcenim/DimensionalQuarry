package tfar.dimensionalquarry.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class StackPredicate implements Predicate<ItemStack> {

    private final ResourceLocation location;
    private final boolean tag;

    private Item itemcache;

    public StackPredicate(ResourceLocation location, boolean tag) {
        this.location = location;
        this.tag = tag;

        if (!tag) {
            itemcache = BuiltInRegistries.ITEM.get(location);
        }
    }

    public boolean test(ItemStack stack) {
        if (!tag) {
            return stack.is(itemcache);
        }
        TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, location);
        return stack.is(itemTagKey);
    }
}
