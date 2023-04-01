package tfar.dimensionalquarry.datagen.assets;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import tfar.dimensionalquarry.DimensionalQuarry;
import tfar.dimensionalquarry.init.ModOs;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, DimensionalQuarry.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("container.dimensionalquarry","Dimensional Quarry");
        add("container.dimensionalquarry.filter","Filter");
        add("container.dimensionalquarry.energy","%s/%s FE");
        addItem(() -> ModOs.FILTER,"Filter");
        addBlock(() -> ModOs.DIMENSIONAL_QUARRY_B,"Dimensional Quarry");
    }
}
