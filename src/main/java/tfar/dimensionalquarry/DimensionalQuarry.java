package tfar.dimensionalquarry;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import tfar.dimensionalquarry.client.screen.DimensionalQuarryScreen;
import tfar.dimensionalquarry.client.screen.FilterScreen;
import tfar.dimensionalquarry.datagen.DataGenerators;
import tfar.dimensionalquarry.init.ModOs;
import tfar.dimensionalquarry.menu.FilterMenu;
import tfar.dimensionalquarry.network.PacketHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DimensionalQuarry.MODID)
public class DimensionalQuarry {
    public static final String MODID = "dimensionalquarry";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public DimensionalQuarry() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        bus.addListener(DataGenerators::setupDataGenerator);
        bus.addListener(this::client);
        bus.addListener(this::common);
       // MinecraftForge.EVENT_BUS.addListener(this::container);
    }

    private void container(PlayerContainerEvent.Open e) {
        AbstractContainerMenu abstractContainerMenu = e.getContainer();
        if (abstractContainerMenu instanceof FilterMenu filterMenu) {
            filterMenu.sendToClient((ServerPlayer) e.getEntity());
        }
    }

    private void setup(final RegisterEvent event) {
        event.register(Registries.BLOCK,NAME,() -> ModOs.DIMENSIONAL_QUARRY_B);
        event.register(Registries.ITEM,new ResourceLocation(MODID,"filter"),() -> ModOs.FILTER);
        event.register(Registries.ITEM,NAME,() -> ModOs.DIMENSIONAL_QUARRY_I);
        event.register(Registries.BLOCK_ENTITY_TYPE,NAME,() -> ModOs.DIMENSIONAL_QUARRY_BE);
        event.register(Registries.MENU,NAME,() -> ModOs.DIMENSIONAL_QUARRY_M);
        event.register(Registries.MENU,new ResourceLocation(MODID,"filter"),() -> ModOs.FILTER_M);
    }

    private void common(FMLCommonSetupEvent e) {
        PacketHandler.registerMessages();
    }

    private void client(FMLClientSetupEvent e){
        MenuScreens.register(ModOs.DIMENSIONAL_QUARRY_M, DimensionalQuarryScreen::new);
        MenuScreens.register(ModOs.FILTER_M, FilterScreen::new);
    }

    private static final ResourceLocation NAME = new ResourceLocation(MODID,"dimensional_quarry");
}
