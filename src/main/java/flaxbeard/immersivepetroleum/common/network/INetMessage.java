package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface INetMessage extends CustomPacketPayload{
	void process(IPayloadContext context);
	
	static ServerPlayer serverPlacer(IPayloadContext ctx){
		return (ServerPlayer) ctx.player();
	}
	
	static <P extends CustomPacketPayload> Type<P> createType(String path){
		return new Type<>(ResourceUtils.ip(path));
	}
}
