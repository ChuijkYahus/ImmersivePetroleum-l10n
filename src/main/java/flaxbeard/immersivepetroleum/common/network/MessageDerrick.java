package flaxbeard.immersivepetroleum.common.network;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import flaxbeard.immersivepetroleum.client.gui.elements.PipeConfig;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class MessageDerrick implements INetMessage{
	public static final Type<MessageDerrick> ID = INetMessage.createType("derrick_sync");
	
	public static final StreamCodec<ByteBuf, MessageDerrick> CODEC = ByteBufCodecs.COMPOUND_TAG.map(MessageDerrick::new, MessageDerrick::toTag);
	
	public static void sendToServer(BlockPos derrickPos, PipeConfig.Grid grid){
		IPPacketHandler.sendToServer(new MessageDerrick(derrickPos, grid.toCompound()));
	}
	
	private final BlockPos derrickPos;
	private final CompoundTag grid;
	
	private MessageDerrick(BlockPos derrick, CompoundTag grid){
		this.derrickPos = derrick;
		this.grid = grid;
	}
	
	private MessageDerrick(CompoundTag tag){
		int x = tag.getInt("posX");
		int y = tag.getInt("posY");
		int z = tag.getInt("posZ");
		this.derrickPos = new BlockPos(x, y, z);
		this.grid = tag.getCompound("grid");
	}
	
	public CompoundTag toTag(){
		CompoundTag tag = new CompoundTag();
		tag.putInt("posX", this.derrickPos.getX());
		tag.putInt("posY", this.derrickPos.getY());
		tag.putInt("posZ", this.derrickPos.getZ());
		tag.put("grid", this.grid);
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
			if(context.connection().getDirection().getReceptionSide() == LogicalSide.SERVER){
				Player player = context.player();
				ServerLevel world = (ServerLevel) player.level();
				if(world.isAreaLoaded(this.derrickPos, 2)){
					BlockEntity te = world.getBlockEntity(this.derrickPos);
					
					if(te instanceof IMultiblockBE<?> mb && mb.getHelper().getContext().getState() instanceof DerrickLogic.State state){
						IMultiblockContext<?> ctx = mb.getHelper().getContext();
						
						state.gridStorage = PipeConfig.Grid.fromCompound(this.grid);
						ctx.markDirtyAndSync();
						ctx.requestMasterBESync();
						
						WellTileEntity well = state.getWell(ctx.getLevel(), ctx.getLevel().getAbsoluteOrigin());
						DerrickLogic.transferGridDataToWell(this.derrickPos, state, well);
					}
				}
			}
		});
	}
}
