package evilcraft.entities.tileentities.tickaction.bloodchest;

import net.minecraft.item.ItemStack;
import evilcraft.api.entities.tileentitites.tickaction.ITickAction;
import evilcraft.entities.tileentities.TileBloodChest;

public class RepairItemTickAction implements ITickAction<TileBloodChest> {
    
    protected final static int MB_PER_DAMAGE = 10;
    protected final static int TICKS_PER_DAMAGE = 10;
    
    @Override
    public boolean canTick(TileBloodChest tile, ItemStack itemStack, int slot, int tick) {
        return !tile.getTank().isEmpty() && itemStack != null;
    }
    
    private void repair(TileBloodChest tile, ItemStack itemStack) {
        tile.getTank().drain(MB_PER_DAMAGE, true);
        int newDamage = itemStack.getItemDamage() - 1;
        itemStack.setItemDamage(newDamage);
    }

    @Override
    public void onTick(TileBloodChest tile, ItemStack itemStack, int slot, int tick) {
        if(tick >= getRequiredTicks(tile, slot)) {
            if(
                    !tile.getTank().isEmpty()
                    && tile.getTank().getFluidAmount() >= MB_PER_DAMAGE
                    && itemStack != null
                    && itemStack.isItemDamaged()
                    && itemStack.getItem().isRepairable()
                    ) {
                repair(tile, itemStack);
            }
        }
    }

    @Override
    public int getRequiredTicks(TileBloodChest tile, int slot) {
        return TICKS_PER_DAMAGE;
    }
    
}