package mods.eln.transparentnode.thermaldissipatoractive;

import mods.eln.Eln;
import mods.eln.i18n.I18N;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.NodePeriodicPublishProcess;
import mods.eln.node.transparent.TransparentNode;
import mods.eln.node.transparent.TransparentNodeDescriptor;
import mods.eln.node.transparent.TransparentNodeElement;
import mods.eln.sim.electrical.ElectricalLoad;
import mods.eln.sim.thermal.ThermalLoad;
import mods.eln.sim.electrical.mna.component.Resistor;
import mods.eln.sim.electrical.nbt.NbtElectricalLoad;
import mods.eln.sim.thermal.nbt.NbtThermalLoad;
import mods.eln.sim.watchdogs.ThermalLoadWatchDog;
import mods.eln.sim.watchdogs.VoltageStateWatchDog;
import mods.eln.sim.watchdogs.WorldExplosion;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ThermalDissipatorActiveElement extends TransparentNodeElement {
    ThermalDissipatorActiveDescriptor descriptor;
    NbtThermalLoad thermalLoad = new NbtThermalLoad("thermalLoad");
    NbtElectricalLoad positiveLoad = new NbtElectricalLoad("positiveLoad");
    ThermalDissipatorActiveSlowProcess slowProcess = new ThermalDissipatorActiveSlowProcess(this);
    Resistor powerResistor = new Resistor(positiveLoad, null);


    public ThermalDissipatorActiveElement(TransparentNode transparentNode,
                                          TransparentNodeDescriptor descriptor) {
        super(transparentNode, descriptor);
        thermalLoadList.add(thermalLoad);
        electricalLoadList.add(positiveLoad);
        electricalComponentList.add(powerResistor);

        slowProcessList.add(slowProcess);
        this.descriptor = (ThermalDissipatorActiveDescriptor) descriptor;
        slowProcessList.add(new NodePeriodicPublishProcess(node, 4f, 2f));


        slowProcessList.add(thermalWatchdog);

        thermalWatchdog
            .set(thermalLoad)
            .setTMax(this.descriptor.warmLimit)
            .set(new WorldExplosion(this).machineExplosion());

        WorldExplosion exp = new WorldExplosion(this).machineExplosion();
        slowProcessList.add(voltageWatchdog.set(positiveLoad).setUNominal(this.descriptor.nominalElectricalU).set(exp));

    }

    VoltageStateWatchDog voltageWatchdog = new VoltageStateWatchDog();
    ThermalLoadWatchDog thermalWatchdog = new ThermalLoadWatchDog();

    @Override
    public ElectricalLoad getElectricalLoad(Direction side, LRDU lrdu) {
        if (side == front || side == front.getInverse()) return positiveLoad;
        return null;
    }

    @Nullable
    @Override
    public ThermalLoad getThermalLoad(@NotNull Direction side, @NotNull LRDU lrdu) {

        if (side == Direction.YN || side == Direction.YP || lrdu != lrdu.Down) return null;
        if (side == front || side == front.getInverse()) return null;
        return thermalLoad;
    }

    @Override
    public int getConnectionMask(Direction side, LRDU lrdu) {

        if (side == Direction.YN || side == Direction.YP || lrdu != lrdu.Down) return 0;
        if (side == front || side == front.getInverse()) return node.maskElectricalPower;
        return node.maskThermal;
    }

    @NotNull
    @Override
    public String multiMeterString(@NotNull Direction side) {

        return Utils.plotVolt("U : ", positiveLoad.getU()) + Utils.plotAmpere("I : ", positiveLoad.getCurrent());
    }

    @NotNull
    @Override
    public String thermoMeterString(@NotNull Direction side) {

        return Utils.plotCelsius("T : ", thermalLoad.Tc) + Utils.plotPower("P : ", thermalLoad.getPower());
    }

    @Override
    public void initialize() {
        descriptor.applyTo(thermalLoad);
        descriptor.applyTo(positiveLoad, powerResistor);
        connect();
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, Direction side,
                                    float vx, float vy, float vz) {

        return false;
    }


    @Override
    public void networkSerialize(DataOutputStream stream) {

        super.networkSerialize(stream);
        try {
            stream.writeFloat(lastPowerFactor = (float) (powerResistor.getP() / descriptor.electricalNominalP));
        } catch (IOException e) {

            e.printStackTrace();
        }
        //Utils.println("DISIP");
    }

    public float lastPowerFactor;

    @NotNull
    @Override
    public Map<String, String> getWaila() {
        Map<String, String> info = new HashMap<String, String>();
        info.put(I18N.tr("Temperature"), Utils.plotCelsius("", thermalLoad.Tc));
        if (Eln.wailaEasyMode) {
            info.put(I18N.tr("Thermal power"), Utils.plotPower("", thermalLoad.getPower()));
        }
        return info;
    }


}
