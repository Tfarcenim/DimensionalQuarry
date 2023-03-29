package tfar.dimensionalquarry.init;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import tfar.dimensionalquarry.DimensionalQuarryBlock;
import tfar.dimensionalquarry.DimensionalQuarryBlockEntity;
import tfar.dimensionalquarry.menu.DimensionalQuarryMenu;
import tfar.dimensionalquarry.item.FilterItem;
import tfar.dimensionalquarry.menu.FilterMenu;

public class ModOs {
    public static final Block DIMENSIONAL_QUARRY_B = new DimensionalQuarryBlock(BlockBehaviour.Properties.of(Material.METAL));
    public static final Item DIMENSIONAL_QUARRY_I = new BlockItem(DIMENSIONAL_QUARRY_B,new Item.Properties());
    public static final BlockEntityType<DimensionalQuarryBlockEntity> DIMENSIONAL_QUARRY_BE = BlockEntityType.Builder.of(DimensionalQuarryBlockEntity::new,DIMENSIONAL_QUARRY_B).build(null);
    public static final MenuType<DimensionalQuarryMenu> DIMENSIONAL_QUARRY_M = new MenuType<>(DimensionalQuarryMenu::new, FeatureFlagSet.of(FeatureFlags.VANILLA));

    public static final Item FILTER = new FilterItem(new Item.Properties());

    public static final MenuType<FilterMenu> FILTER_M = IForgeMenuType.create((f, g, h)-> {
        ItemStack stack = h.readItem();
        return new FilterMenu(f,g,stack);});
}
