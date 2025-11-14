package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.common.IPContent;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class MessageDebugSync implements INetMessage{
	public static final Type<MessageDebugSync> ID = INetMessage.createType("debug_sync");
	
	// TODO Should probably just use Integer since DebugItem.Mode was/is being stored as such in NBT anyway
	public static final StreamCodec<ByteBuf, MessageDebugSync> CODEC = ByteBufCodecs.COMPOUND_TAG.map(MessageDebugSync::new, message -> message.nbt);
	
	private final CompoundTag nbt;
	public MessageDebugSync(CompoundTag nbt){
		this.nbt = nbt;
	}
	
	@Nonnull
	@Override
	public Type<? extends CustomPacketPayload> type(){
		return ID;
	}
	
	@Override
	public void process(IPayloadContext context){
		context.enqueueWork(() -> {
			if(context.connection().getDirection().getReceptionSide() == LogicalSide.SERVER){
				Player player = context.player();
				ItemStack mainItem = player.getMainHandItem();
				ItemStack secondItem = player.getOffhandItem();
				boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.DEBUGITEM.get();
				boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.DEBUGITEM.get();
				
				if(main || off){
					ItemStack target = main ? mainItem : secondItem;
					
					/* // TODO FIXME etc.
					CompoundTag targetNBT = target.getOrCreateTagElement("settings");
					targetNBT.merge(this.nbt);
					*/
				}
			}
		});
	}
}
