package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.client.gui.SeismicSurveyScreen;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static flaxbeard.immersivepetroleum.common.util.survey.SurveyScan.SCAN_RADIUS;
import static flaxbeard.immersivepetroleum.common.util.survey.SurveyScan.SCAN_SIZE;

public class MessageSurveyResultDetails{
	
	/** Request the server to give more information on the provided scan */
	public static void sendRequestToServer(SurveyScan scan){
		IPPacketHandler.sendToServer(new MessageSurveyResultDetails.ClientToServer(scan));
	}
	
	private static void sendReply(Player player, UUID scanId, BitSet replyBitSet){
		IPPacketHandler.sendToPlayer(player, new MessageSurveyResultDetails.ServerToClient(scanId, replyBitSet));
	}
	
	public static class ClientToServer implements INetMessage{
		public static final Type<ClientToServer> ID = INetMessage.createType("survey_client_to_server");
		
		public static final StreamCodec<ByteBuf, ClientToServer> CODEC = ByteBufCodecs.COMPOUND_TAG.map(ClientToServer::new, ClientToServer::toTag);
		
		private final int x, z;
		private final UUID scanId;
		public ClientToServer(SurveyScan scan){
			this.x = scan.x();
			this.z = scan.z();
			this.scanId = scan.uuid();
		}
		
		private ClientToServer(CompoundTag tag){
			this.x = tag.getInt("x");
			this.z = tag.getInt("z");
			this.scanId = tag.getUUID("scanId");
		}
		
		private CompoundTag toTag(){
			CompoundTag tag = new CompoundTag();
			tag.putInt("x", this.x);
			tag.putInt("z", this.z);
			tag.putUUID("scanId", this.scanId);
			return tag;
		}
		
		@Nonnull
		@Override
		public Type<? extends CustomPacketPayload> type(){
			return ID;
		}
		
		@Override
		public void process(IPayloadContext context){
			if(context.connection().getDirection().getReceptionSide() == LogicalSide.CLIENT)
				return;
			
			context.enqueueWork(() -> {
				final ServerPlayer sPlayer = (ServerPlayer) Objects.requireNonNull(context.player());
				final ServerLevel sLevel = sPlayer.serverLevel();
				
				if(sLevel.isAreaLoaded(new BlockPos(this.x, 0, this.z), SCAN_RADIUS)){
					final BitSet set = compileBitSet(sLevel);
					sendReply(sPlayer, this.scanId, set);
				}
			});
		}
		
		private BitSet compileBitSet(Level level){
			final List<ReservoirIsland> islandCache = new ArrayList<>();
			final BitSet set = new BitSet(SCAN_SIZE * SCAN_SIZE);
			final int r = SCAN_RADIUS;
			for(int j = -r, a = 0;j <= r;j++, a++){
				for(int i = -r, b = 0;i <= r;i++, b++){
					int x = this.x - i;
					int z = this.z - j;
					
					double current = ReservoirHandler.getValueOf(level, x, z);
					if(current != -1){
						Optional<ReservoirIsland> optional = islandCache.stream().filter(res -> {
							return res.contains(x, z);
						}).findFirst();
						
						ReservoirIsland nearbyIsland = optional.orElse(null);
						if(nearbyIsland == null){
							nearbyIsland = ReservoirHandler.getIslandNoCache(level, new ColumnPos(x, z));
							
							if(nearbyIsland != null){
								islandCache.add(nearbyIsland);
							}
						}
						
						if(nearbyIsland != null){
							set.set(a * SCAN_SIZE + b);
						}
					}
				}
			}
			return set;
		}
	}
	
	public static class ServerToClient implements INetMessage{
		public static final Type<ServerToClient> ID = INetMessage.createType("survey_server_to_client");
		
		public static final StreamCodec<ByteBuf, ServerToClient> CODEC = ByteBufCodecs.COMPOUND_TAG.map(ServerToClient::new, ServerToClient::toTag);
		
		private final BitSet replyBitSet;
		private final UUID scanId;
		public ServerToClient(UUID scanId, BitSet replyBitSet){
			this.scanId = scanId;
			this.replyBitSet = replyBitSet;
		}
		
		private ServerToClient(CompoundTag tag){
			this(tag.getUUID("scanId"), BitSet.valueOf(tag.getByteArray("bitset")));
		}
		
		private CompoundTag toTag(){
			CompoundTag tag = new CompoundTag();
			tag.putByteArray("bitset", this.replyBitSet.toByteArray());
			tag.putUUID("scanId", this.scanId);
			return tag;
		}
		
		@Nonnull
		@Override
		public Type<? extends CustomPacketPayload> type(){
			return ID;
		}
		
		@Override
		public void process(IPayloadContext context){
			if(context.connection().getDirection().getReceptionSide() == LogicalSide.SERVER)
				return;
			
			context.enqueueWork(() -> {
				if(MCUtil.getScreen() instanceof SeismicSurveyScreen surveyScreen && this.scanId.equals(surveyScreen.scan.uuid())){
					surveyScreen.setBitSet(this.replyBitSet);
				}
			});
		}
	}
}
