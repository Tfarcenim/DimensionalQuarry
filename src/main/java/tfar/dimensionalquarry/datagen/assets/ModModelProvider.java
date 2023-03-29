package tfar.dimensionalquarry.datagen.assets;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import tfar.dimensionalquarry.DimensionalQuarry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        Map<Block, BlockStateGenerator> map = Maps.newHashMap();
        Consumer<BlockStateGenerator> consumer = (blockStateGenerator) -> {
            Block block = blockStateGenerator.getBlock();
            BlockStateGenerator blockstategenerator = map.put(block, blockStateGenerator);
            if (blockstategenerator != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        };
        Map<ResourceLocation, Supplier<JsonElement>> map1 = Maps.newHashMap();
        Set<Item> set = Sets.newHashSet();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer = (location, elementSupplier) -> {
            Supplier<JsonElement> supplier = map1.put(location, elementSupplier);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + location);
            }
        };
        Consumer<Item> consumer1 = set::add;
        new ModBlockModelGenerators(consumer, biconsumer, consumer1).run();
        new ModItemModelGenerators(biconsumer).run();

        List<Block> allBlocks = getBlocks();

        List<Block> list = allBlocks.stream().filter((block) -> !map.containsKey(block)).toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Missing blockstate definitions for: " + list);
        } else {
            allBlocks.forEach((block) -> {
                Item item = Item.BY_BLOCK.get(block);
                if (item != null) {
                    if (set.contains(item)) {
                        return;
                    }

                    ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(item);
                    if (!map1.containsKey(resourcelocation)) {
                        map1.put(resourcelocation, new DelegatedModel(ModelLocationUtils.getModelLocation(block)));
                    }
                }

            });
            return CompletableFuture.allOf(this.saveCollection(pOutput, map,
                    (block) -> this.blockStatePathProvider.json(block.builtInRegistryHolder().key().location())),
                    this.saveCollection(pOutput, map1, this.modelPathProvider::json));
        }
    }

    protected List<Block> getBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(DimensionalQuarry.MODID)).toList();
    }
}
