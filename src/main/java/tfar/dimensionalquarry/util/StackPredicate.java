package tfar.dimensionalquarry.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StackPredicate {

    private final ResourceLocation location;
    private final boolean tag;

    private List<Item> cache;

    public StackPredicate(ResourceLocation location, boolean tag) {
        this.location = location;
        this.tag = tag;
    }

    public boolean matches(ItemStack stack) {
        if (!tag) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(location);
        }
        TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, location);
        return stack.is(itemTagKey);
    }
}
