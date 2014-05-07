package evilcraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.EnumMap;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import evilcraft.Reference;

/**
 * Advanced packet handler of {@link PacketBase} instances.
 * An alternative would be {@link SimpleNetworkWrapper}.
 * Partially based on the SecretRooms mod packet handling:
 * https://github.com/AbrarSyed/SecretRoomsMod-forge
 * @author rubensworks
 *
 */
@Sharable
public class PacketHandler extends FMLIndexedMessageToMessageCodec<PacketBase> {

	private static final PacketHandler INSTANCE = new PacketHandler();
    private static final EnumMap<Side, FMLEmbeddedChannel> CHANNELS =
    		Maps.newEnumMap(Side.class);
	
    private PacketHandler() {
    	
    }
    
    /**
     * Initialize the packet handler.
     */
    public static void init() {
        if (!CHANNELS.isEmpty()) {
            return;
        }

        CHANNELS.putAll(NetworkRegistry.INSTANCE.newChannel(Reference.MOD_ID, INSTANCE));
    }
    
    /**
     * Register a new packet.
     * @param id The id for the packet.
     * @param packetType The class of the packet.
     */
    public static void register(int id, Class<? extends PacketBase> packetType) {
    	INSTANCE.addDiscriminator(id, packetType);
    }

	@Override
	public void encodeInto(ChannelHandlerContext context, PacketBase packet,
			ByteBuf target) throws Exception {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
        packet.encode(output);
        target.writeBytes(output.toByteArray());
	}

	@Override
	public void decodeInto(ChannelHandlerContext context, ByteBuf source,
			PacketBase packet) {
		ByteArrayDataInput input = ByteStreams.newDataInput(source.array());
        input.skipBytes(1); // skip the packet identifier byte
        packet.decode(input);
        
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            actionClient(packet);
        } else {
            actionServer(context, packet);
        }
	}
	
	@SideOnly(Side.CLIENT)
    private void actionClient(PacketBase packet) {
        Minecraft minecraft = Minecraft.getMinecraft();
        packet.actionClient(minecraft.theWorld, minecraft.thePlayer);
    }
    
    private void actionServer(ChannelHandlerContext context, PacketBase packet) {
        EntityPlayerMP player = ((NetHandlerPlayServer)context.channel()
        		.attr(NetworkRegistry.NET_HANDLER).get()).playerEntity;
        packet.actionServer(player.worldObj, player);
    }
    
    /**
     * Get the client-side channel.
     * @return The client channel.
     */
    public static FMLEmbeddedChannel getClientChannel() {
    	return CHANNELS.get(Side.CLIENT);
    }
    
    /**
     * Get the server-side channel.
     * @return The server channel.
     */
    public static FMLEmbeddedChannel getServerChannel() {
    	return CHANNELS.get(Side.SERVER);
    }
    
    /**
     * Send a packet to the server.
     * @param packet The packet.
     */
    public static void sendToServer(PacketBase packet) {
    	getClientChannel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
    	getClientChannel().writeAndFlush(packet);
    }
    
    /**
     * Send a packet to the player.
     * @param packet The packet.
     * @param player The player.
     */
    public static void sendToPlayer(PacketBase packet, EntityPlayer player) {
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
    	getServerChannel().writeAndFlush(packet);
    }

    /**
     * Send a packet to all in the target range.
     * @param packet The packet.
     * @param point The area to send to.
     */
    public static void sendToAllAround(PacketBase packet, NetworkRegistry.TargetPoint point) {
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
    	getServerChannel().writeAndFlush(packet);
    }

    /**
     * Send a packet to everything in the given dimension.
     * @param packet The packet.
     * @param dimension The dimension to send to.
     */
    public static void sendToDimension(PacketBase packet, int dimension) {
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimension);
    	getServerChannel().writeAndFlush(packet);
    }
    
    /**
     * Send a packet to everything.
     * @param packet The packet.
     */
    public static void sendToAll(PacketBase packet) {
    	getServerChannel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
    	getServerChannel().writeAndFlush(packet);
    }
    
    /**
     * Convert the given packet to a minecraft packet.
     * @param packet The packet.
     * @return The minecraft packet.
     */
    public static Packet toMcPacket(PacketBase packet) {
        return CHANNELS.get(FMLCommonHandler.instance().getEffectiveSide()).generatePacketFrom(packet);
    }
    
}