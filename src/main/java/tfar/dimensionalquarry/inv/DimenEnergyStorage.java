package tfar.dimensionalquarry.inv;

import net.minecraftforge.energy.EnergyStorage;

public class DimenEnergyStorage extends EnergyStorage {
    public DimenEnergyStorage(int capacity) {
        super(capacity);
    }

    public DimenEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public DimenEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public DimenEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public void setEnergy0(int energy0) {
        energy = energy & 0xffff0000 | energy0;
    }

    public void setEnergy1(int energy1) {
        energy = energy & 0xffff | energy1 << 16;
    }
}
