package flaxbeard.immersivepetroleum.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class IPPacketHandler{
	public static void init(PayloadRegistrar registrar){
		registrar.commonToServer(MessageConsumeBoatFuel.ID, MessageConsumeBoatFuel.CODEC, MessageConsumeBoatFuel::process);
		registrar.commonToServer(MessageDebugSync.ID, MessageDebugSync.CODEC, MessageDebugSync::process);
		registrar.commonToServer(MessageProjectorSync.ID, MessageProjectorSync.CODEC, MessageProjectorSync::process);
		registrar.commonToServer(MessageDerrick.ID, MessageDerrick.CODEC, MessageDerrick::process);
		
		registrar.commonToServer(MessageSurveyResultDetails.ClientToServer.ID, MessageSurveyResultDetails.ClientToServer.CODEC, MessageSurveyResultDetails.ClientToServer::process);
		registrar.commonToServer(MessageSurveyResultDetails.ServerToClient.ID, MessageSurveyResultDetails.ServerToClient.CODEC, MessageSurveyResultDetails.ServerToClient::process);
	}
	
	/**
	 * Sends a server message directly to the player. Will not do anything if the provided instance is not {@link ServerPlayer}
	 *
	 * @param player  The {@link Player} to send to
	 * @param message The message to send
	 */
	public static <MSG extends CustomPacketPayload> void sendToPlayer(Player player, MSG message){
		if(message == null || !(player instanceof ServerPlayer serverPlayer))
			return;
		
		PacketDistributor.sendToPlayer(serverPlayer, message);
	}
	
	/** Client -> Server */
	public static <MSG extends CustomPacketPayload> void sendToServer(MSG message){
		if(message == null)
			return;
		
		PacketDistributor.sendToServer(message);
	}
	
	/**
	 * Sends a packet to everyone in the specified dimension.
	 * <pre>
	 * Server -> Client
	 * </pre>
	 */
	public static <MSG extends CustomPacketPayload> void sendToDimension(Level level, MSG message){
		if(message == null || !(level instanceof ServerLevel serverLevel))
			return;
		
		PacketDistributor.sendToPlayersInDimension(serverLevel, message);
	}
	
	/**
	 * Sends a packet to everyone.
	 * <pre>
	 * Server -> Client
	 * </pre>
	 */
	public static <MSG extends CustomPacketPayload> void sendAll(MSG message){
		if(message == null)
			return;
		
		PacketDistributor.sendToAllPlayers(message);
		/*
		INSTANCE.send(PacketDistributor.ALL.noArg(), message);
		*/
	}
}
