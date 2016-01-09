package mods.eln.item.regulator;

import mods.eln.generic.GenericItemUsingDamageSlot;
import mods.eln.item.regulator.IRegulatorDescriptor.RegulatorType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import static mods.eln.i18n.I18N.tr;

public class RegulatorSlot extends GenericItemUsingDamageSlot {

  private RegulatorType[] type;
  private final static String COMMENT = tr("Regulator Slot");

  public RegulatorSlot(IInventory inventory, int slot, int x, int y, int stackLimit, RegulatorType[] type, SlotSkin
          skin) {
    super(inventory, slot, x, y, stackLimit, IRegulatorDescriptor.class, skin, new String[]{COMMENT});
    this.type = type;
  }

  @Override
  public boolean isItemValid(ItemStack itemStack) {
    if (!super.isItemValid(itemStack)) return false;
    IRegulatorDescriptor element = (IRegulatorDescriptor) IRegulatorDescriptor.getDescriptor(itemStack);
    for (RegulatorType t : type) {
      if (t == element.getType()) return true;
    }
    return false;
  }
}
