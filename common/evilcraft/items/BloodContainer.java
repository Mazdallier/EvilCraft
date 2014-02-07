package evilcraft.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.ItemFluidContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import evilcraft.api.HotbarIterator;
import evilcraft.api.IInformationProvider;
import evilcraft.api.config.ExtendedConfig;
import evilcraft.api.config.configurable.ConfigurableDamageIndicatedItemFluidContainer;
import evilcraft.fluids.Blood;

public class BloodContainer extends ConfigurableDamageIndicatedItemFluidContainer {
    
    private static BloodContainer _instance = null;
    
    private Icon[] icons = new Icon[BloodContainerConfig.getContainerLevels()];
    
    private static final int MB_FILL_PERTICK = 10;
    
    public static void initInstance(ExtendedConfig eConfig) {
        if(_instance == null)
            _instance = new BloodContainer(eConfig);
        else
            eConfig.showDoubleInitError();
    }
    
    public static BloodContainer getInstance() {
        return _instance;
    }

    private BloodContainer(ExtendedConfig eConfig) {
        super(eConfig, BloodExtractorConfig.containerSize, Blood.getInstance());
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister) {
        for(int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon(getIconString() + "_" + i);
        }
    }
    
    @Override
    public Icon getIconFromDamage(int damage) {
        return icons[Math.min(damage & 7, icons.length - 1)];
    }
    
    @SuppressWarnings({ "rawtypes"})
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(int id, CreativeTabs tab, List itemList) {
        for(int i = 0; i < icons.length; i++) {
            component.getSubItems(id, tab, itemList, fluid, i);
        }
    }
    
    @Override
    public int getCapacity(ItemStack container) {
        return capacity << (container.getItemDamage() & 7);
    }
    
    @Override
    public String getItemDisplayName(ItemStack itemStack) {
        return BloodContainerConfig.containerLevelNames[Math.min(itemStack.getItemDamage() & 7, icons.length - 1)];
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if(!world.isRemote && player.isSneaking()) {
            toggleContainerActivation(itemStack);
        }
        return super.onItemRightClick(itemStack, world, player);
    }
    
    @Override
    public boolean hasEffect(ItemStack itemStack){
        return isContainerActivated(itemStack);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        super.addInformation(itemStack, entityPlayer, list, par4);
        list.add(IInformationProvider.INFO_PREFIX+"Shift + Right click to toggle auto-supply.");
        String autoSupply = EnumChatFormatting.RESET+"Disabled";
        if(isContainerActivated(itemStack)) {
            autoSupply = EnumChatFormatting.GREEN + "Enabled";
        }
        list.add(EnumChatFormatting.BOLD + "Auto-supply: " + autoSupply);
    }
    
    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5) {
        if(entity instanceof EntityPlayer && !world.isRemote && isContainerActivated(itemStack)) {
            FluidStack tickFluid = this.getFluid(itemStack);
            if(tickFluid != null && tickFluid.amount > 0) {
                EntityPlayer player = (EntityPlayer) entity;
                ItemStack held = player.getCurrentEquippedItem();
                if(held != null && held != itemStack && held.getItem() instanceof IFluidContainerItem) {
                    IFluidContainerItem fluidContainer = (IFluidContainerItem) held.getItem();
                    FluidStack heldFluid = fluidContainer.getFluid(held);
                    if(tickFluid.amount >= MB_FILL_PERTICK
                            && (heldFluid == null || (heldFluid != null
                                                    && heldFluid.isFluidEqual(tickFluid)
                                                    && heldFluid.amount < fluidContainer.getCapacity(held)
                                                    )
                               )
                            ) {
                        int filled = fluidContainer.fill(held, new FluidStack(tickFluid.getFluid(), MB_FILL_PERTICK), true);
                        this.drain(itemStack, filled, true);
                    }
                }
            }
        }
        super.onUpdate(itemStack, world, entity, par4, par5);
    }
    
    public static boolean isContainerActivated(ItemStack itemStack) {
        return itemStack != null && itemStack.getTagCompound() != null && itemStack.getTagCompound().getBoolean("enabled");
    }
    
    public static void toggleContainerActivation(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();
        if(tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTagCompound(tag);
        }
        tag.setBoolean("enabled", !isContainerActivated(itemStack));
    }

}
