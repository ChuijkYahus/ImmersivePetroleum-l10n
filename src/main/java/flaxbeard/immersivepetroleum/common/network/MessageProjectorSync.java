package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class MessageProjectorSync implements INetMessage{
	public static final Type<MessageProjectorSync> ID = INetMessage.createType("projector_sync");
	
	public static final StreamCodec<ByteBuf, MessageProjectorSync> CODEC = ByteBufCodecs.COMPOUND_TAG.map(MessageProjectorSync::new, MessageProjectorSync::toTag);
	
	public static void sendToServer(Settings settings, InteractionHand hand){
		IPPacketHandler.sendToServer(new MessageProjectorSync(settings, hand, true));
	}
	
	public static void sendToClient(Player player, Settings settings, InteractionHand hand){
		IPPacketHandler.sendToPlayer(player, new MessageProjectorSync(settings, hand, false));
	}
	
	private final boolean forServer;
	private final CompoundTag nbt;
	private final InteractionHand hand;
	
	public MessageProjectorSync(Settings settings, InteractionHand hand, boolean toServer){
		this.nbt = settings.toNbt();
		this.forServer = toServer;
		this.hand = hand;
	}
	
	private MessageProjectorSync(CompoundTag tag){
		this.nbt = tag.getCompound("settings");
		this.hand = InteractionHand.values()[tag.getInt("hand")];
		this.forServer = tag.getBoolean("forServer");
	}
	
	private CompoundTag toTag(){
		CompoundTag tag = new CompoundTag();
		tag.put("settings", this.nbt);
		tag.putInt("hand", this.hand.ordinal());
		tag.putBoolean("forServer", this.forServer);
		return tag;
	}
	
	@Nonnull
	@Override
	public Type<? extends CustomPacketPayload> type(){
		return ID;
	}
	
	@Override
	public void process(IPayloadContext context){
		context.enqueueWork(() -> {
			if(context.connection().getDirection().getReceptionSide() == getSide()){
				Player player = context.player();
				ItemStack held = player.getItemInHand(this.hand);
				
				if(held.is(IPContent.Items.PROJECTOR.get())){
					Settings settings = new Settings(this.nbt);
					settings.applyTo(held);
				}
			}
		});
	}
	
	LogicalSide getSide(){
		return this.forServer ? LogicalSide.SERVER : LogicalSide.CLIENT;
	}
}
