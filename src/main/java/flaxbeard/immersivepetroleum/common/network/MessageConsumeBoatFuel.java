package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class MessageConsumeBoatFuel implements INetMessage{
	public static final Type<MessageConsumeBoatFuel> ID = INetMessage.createType("consume_speedboat_fuel");
	
	public static final StreamCodec<ByteBuf, MessageConsumeBoatFuel> CODEC = ByteBufCodecs.INT.map(MessageConsumeBoatFuel::new, message -> message.amount);
	
	private final int amount;
	public MessageConsumeBoatFuel(int amount){
		this.amount = amount;
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
				Entity entity = context.player().getVehicle();
				
				if(entity instanceof MotorboatEntity boat){
					FluidStack fluid = boat.getContainedFluid();
					
					if(fluid != null && fluid != FluidStack.EMPTY)
						fluid.setAmount(Math.max(0, fluid.getAmount() - amount));
					
					boat.setContainedFluid(fluid);
				}
			}
		});
	}
}
