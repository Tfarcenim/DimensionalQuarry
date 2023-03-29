package tfar.dimensionalquarry.datagen.assets;

import com.google.gson.JsonElement;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import tfar.dimensionalquarry.init.ModOs;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModItemModelGenerators extends ItemModelGenerators {
    public ModItemModelGenerators(BiConsumer<ResourceLocation, Supplier<JsonElement>> pOutput) {
        super(pOutput);
    }

    @Override
    public void run() {
        this.generateFlatItem(ModOs.FILTER, ModelTemplates.FLAT_ITEM);
    }
}
