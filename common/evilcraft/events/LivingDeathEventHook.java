package evilcraft.events;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;
import cpw.mods.fml.client.FMLClientHandler;
import evilcraft.api.HotbarIterator;
import evilcraft.blocks.BloodStainedDirtConfig;
import evilcraft.fluids.Blood;
import evilcraft.items.BloodExtractor;
import evilcraft.render.EntityBloodSplashFX;

public class LivingDeathEventHook {

    @ForgeSubscribe(priority = EventPriority.NORMAL)
    public void LivingDeath(LivingDeathEvent event) {
        bloodObtainEvent(event);
        bloodStainedDirtEvent(event);
    }
    
    private void bloodObtainEvent(LivingDeathEvent event) {
        Entity e = event.source.getEntity();
        if(e != null && e instanceof EntityPlayerMP && !e.worldObj.isRemote && event.entityLiving != null) {
            EntityPlayerMP player = (EntityPlayerMP) e;
           
            int health = MathHelper.floor_float(event.entityLiving.getMaxHealth());
            int toFill = health * 10 + (new Random()).nextInt(health * 90);
            
            HotbarIterator it = new HotbarIterator(player);
            while(it.hasNext() && toFill > 0) {
                ItemStack itemStack = it.next();
                if(itemStack != null && itemStack.getItem() == BloodExtractor.getInstance()) {
                    ItemFluidContainer container = (ItemFluidContainer) itemStack.getItem();
                    toFill -= container.fill(itemStack, new FluidStack(Blood.getInstance(), toFill), true);
                }
            }
        }
    }
    
    private void bloodStainedDirtEvent(LivingDeathEvent event) {
        if(event.source.damageType == DamageSource.fall.damageType) {
            int x = MathHelper.floor_double(event.entity.posX);
            int y = MathHelper.floor_double(event.entity.posY - event.entity.getYOffset() - 1);
            int z = MathHelper.floor_double(event.entity.posZ);
            int blockID = event.entity.worldObj.getBlockId(x, y, z);
            if(!event.entity.worldObj.isRemote && blockID == Block.dirt.blockID) {
                // Transform dirt into bloody dirt
                event.entity.worldObj.setBlock(x, y, z, BloodStainedDirtConfig._instance.ID);
                
                // Init particles
                Random random = new Random();
                EntityBloodSplashFX.spawnParticles(event.entity.worldObj, x, y + 1, z, ((int)event.entityLiving.getMaxHealth()) + random.nextInt(15), 5 + random.nextInt(5));
            }
        }
    }
    
}