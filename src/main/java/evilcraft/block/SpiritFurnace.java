package evilcraft.block;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import evilcraft.api.ILocation;
import evilcraft.client.gui.container.GuiSpiritFurnace;
import evilcraft.core.algorithm.Location;
import evilcraft.core.algorithm.Size;
import evilcraft.core.block.CubeDetector.IDetectionListener;
import evilcraft.core.config.BlockConfig;
import evilcraft.core.config.ExtendedConfig;
import evilcraft.core.config.configurable.ConfigurableBlockContainerGuiTankInfo;
import evilcraft.core.helper.LocationHelpers;
import evilcraft.core.helper.MinecraftHelpers;
import evilcraft.inventory.container.ContainerSpiritFurnace;
import evilcraft.tileentity.TileSpiritFurnace;

/**
 * A machine that can infuse stuff with blood.
 * @author rubensworks
 *
 */
public class SpiritFurnace extends ConfigurableBlockContainerGuiTankInfo implements IDetectionListener {
    
    private static SpiritFurnace _instance = null;
    
    private IIcon blockIconInactive;
    
    /**
     * Initialise the configurable.
     * @param eConfig The config.
     */
    public static void initInstance(ExtendedConfig<BlockConfig> eConfig) {
        if(_instance == null)
            _instance = new SpiritFurnace(eConfig);
        else
            eConfig.showDoubleInitError();
    }
    
    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static SpiritFurnace getInstance() {
        return _instance;
    }

    private SpiritFurnace(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.rock, TileSpiritFurnace.class);
        this.setHardness(5.0F);
        this.setStepSound(soundTypeStone);
        this.setHarvestLevel("pickaxe", 2); // Iron tier
        this.setRotatable(true);
        
        if (MinecraftHelpers.isClientSide())
            setGUI(GuiSpiritFurnace.class);
        setContainer(ContainerSpiritFurnace.class);
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if(!TileSpiritFurnace.canWork(world, new Location(x, y, z))) {
        	return true;
        }
    	return super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        blockIcon = iconRegister.registerIcon(getTextureName());
        blockIconInactive = iconRegister.registerIcon(getTextureName() + "_inactive");
        
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta == 1) {
            return this.blockIcon;
        }
        return this.blockIconInactive;
    }
    
    @Override
    public Item getItemDropped(int par1, Random random, int zero) {
        return Item.getItemFromBlock(this);
    }
    
    @Override
    public boolean hasComparatorInputOverride() {
            return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
    	TileSpiritFurnace tile = (TileSpiritFurnace) world.getTileEntity(x, y, z);
        float output = (float) tile.getTank().getFluidAmount() / (float) tile.getTank().getCapacity();
        return (int)Math.ceil(MinecraftHelpers.COMPARATOR_MULTIPLIER * output);
    }

    @Override
    public String getTankNBTName() {
        return TileSpiritFurnace.TANKNAME;
    }

    @Override
    public int getTankCapacity(ItemStack itemStack) {
        return TileSpiritFurnace.LIQUID_PER_SLOT;
    }
    
    private void triggerDetector(World world, int x, int y, int z, boolean valid) {
    	TileSpiritFurnace.detector.detect(world, new Location(new int[]{x, y, z}), valid);
    }
    
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
    	triggerDetector(world, x, y, z, true);
    }
    
    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
    	triggerDetector(world, x, y, z, false);
    	super.onBlockPreDestroy(world, x, y, z, meta);
    }

	@Override
	public void onDetect(World world, ILocation location, Size size, boolean valid) {
		Block block = LocationHelpers.getBlock(world, location);
		if(block == this) {
			TileSpiritFurnace.detectStructure(world, location, size, valid);
			TileEntity tile = LocationHelpers.getTile(world, location);
			if(tile != null) {
				((TileSpiritFurnace) tile).setSize(valid ? size : Size.NULL_SIZE);
			}
		}
	}

}
