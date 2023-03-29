package tfar.dimensionalquarry.datagen;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import tfar.dimensionalquarry.datagen.assets.ModLangProvider;
import tfar.dimensionalquarry.datagen.assets.ModModelProvider;
import tfar.dimensionalquarry.datagen.data.tags.ModBlockTagsProvider;
import tfar.dimensionalquarry.datagen.data.tags.ModItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void setupDataGenerator(GatherDataEvent e) {
        DataGenerator generator = e.getGenerator();
        ExistingFileHelper helper = e.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = e.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(packOutput, lookupProvider, helper);
        generator.addProvider(e.includeServer(), blockTagsProvider);
        generator.addProvider(e.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), helper));
        generator.addProvider(e.includeClient(), new ModModelProvider(packOutput));
        generator.addProvider(e.includeClient(), new ModLangProvider(packOutput));
    }
}
