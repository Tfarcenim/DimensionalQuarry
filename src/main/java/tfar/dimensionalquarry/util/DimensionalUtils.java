package tfar.dimensionalquarry.util;

import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import tfar.dimensionalquarry.DimensionalQuarry;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DimensionalUtils {

    public static void cloneDimension(MinecraftServer server,ResourceLocation original) {
        ResourceLocation newID = createCloneKey(original);
        ResourceKey<Level> newKey = ResourceKey.create(Registries.DIMENSION, newID);

        // make sure new dimension's new name isn't already in use
        ServerLevel existingNewLevel = server.getLevel(newKey);
        if (existingNewLevel != null)
        {
           // throw new SimpleCommandExceptionType(new LiteralMessage(String.format("Error copying dimension: ID %s is already in use", newID))).create();
        }

        ResourceKey<Level> resourcekey = ResourceKey.create(Registries.DIMENSION, original);
        ServerLevel oldLevel = server.getLevel(resourcekey);

        Holder<DimensionType> typeHolder = oldLevel.dimensionTypeRegistration();

        ChunkGenerator chunkGenerator = copyChunkGenerator(server,resourcekey,oldLevel);

        BiFunction<MinecraftServer, ResourceKey<LevelStem>, LevelStem> dimensionFactory =  (server1,resourceKey)->
                new LevelStem(typeHolder, chunkGenerator);

        DynamicDimensionManager.getOrCreateLevel(server, newKey, dimensionFactory);
    }

    public static ServerLevel getOrCreateCloneDimension(MinecraftServer server,ServerLevel original) {
        ResourceLocation location = original.dimension().location();
        ResourceLocation clone = createCloneKey(location);
        ServerLevel newLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION,clone));
        if (newLevel == null) {
            cloneDimension(server,location);
        }
        return server.getLevel(ResourceKey.create(Registries.DIMENSION,clone));
    }

    public static ResourceLocation createCloneKey(ResourceLocation original) {
        return new ResourceLocation(DimensionalQuarry.MODID,original.getNamespace() +"-"+original.getPath()+"-clone");
    }

    private static ChunkGenerator copyChunkGenerator(MinecraftServer server, ResourceKey<Level> key, ServerLevel oldLevel) {
        // deep-copy the chunk generator (the chunk generator seed isn't necessarily preserved in chunk generators)
        DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
        ChunkGenerator oldChunkGenerator = oldLevel.getChunkSource().getGenerator();
        return ChunkGenerator.CODEC.encodeStart(ops, oldChunkGenerator)
                .flatMap(nbt -> ChunkGenerator.CODEC.parse(ops, nbt))
                .getOrThrow(false, s ->
                {
                    throw new CommandRuntimeException(Component.translatable(String.format("Error copying dimension: %s", s)));
                });
    }

    public static int getHighestY(ServerLevel level,ChunkPos pos) {
        int y = 0;
        for (int x = 0; x < 16;x++) {
            for (int z = 0; z < 16;z++) {
                y = Math.max(y,level.getHeight(Heightmap.Types.MOTION_BLOCKING,x + 16 * pos.x,z + 16 * pos.z));
            }
        }
        return y;
    }
}
