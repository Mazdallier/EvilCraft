// Date: 12/07/2014 11:42:39
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package evilcraft.client.render.model;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import evilcraft.Reference;
import evilcraft.core.client.render.model.ModelWavefront;

/**
 * Model for a chalice.
 * @author rubensworks
 *
 */
public class ModelChalice extends ModelWavefront {
	
	private static WavefrontObject model = new WavefrontObject(
			new ResourceLocation(Reference.MOD_ID, Reference.MODEL_PATH + "chalice.obj"));
	
	/**
	 * Make a new instance.
	 * @param texture The texture.
	 */
	public ModelChalice(ResourceLocation texture) {
		super(model, texture);
	}
	
	@Override
    public void renderAll() {
    	GL11.glTranslatef(0.5F, 0.24F, 0.5F);
    	GL11.glScalef(0.30F, 0.22F, 0.30F);
    	GL11.glRotatef(180F, 1F, 0F, 0F);
    	super.renderAll();
    }

}
