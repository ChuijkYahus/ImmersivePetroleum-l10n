package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.FlarestackTileEntity;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import flaxbeard.immersivepetroleum.common.shaderscases.ShaderCaseProjector;
import flaxbeard.immersivepetroleum.common.util.IPItemStackHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import static net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import static net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import static net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;

@EventBusSubscriber(bus = Bus.MOD, modid = ImmersivePetroleum.MODID)
public class IPCapabilityRegistry{
	
	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event){
		IPFluid.IPBucketItem[] array = IPFluid.FLUIDS.stream().map(f -> f.bucket().get()).toArray(IPFluid.IPBucketItem[]::new);
		event.registerItem(FluidHandler.ITEM, (stack, _void) -> new FluidBucketWrapper(stack), array);
		
		event.registerBlockEntity(FluidHandler.BLOCK, IPTileTypes.FLARE.get(), FlarestackTileEntity::getCapability);
		event.registerBlockEntity(FluidHandler.BLOCK, IPTileTypes.AUTOLUBE.get(), AutoLubricatorTileEntity::getCapability);
		
		reg(event, IPTileTypes.GENERATOR.get(), new BlockCapability[]{
			FluidHandler.BLOCK,
			EnergyStorage.BLOCK
		});
		
		event.registerItem(CapabilityShader.ITEM, (stack, _void) -> new CapabilityShader.ShaderWrapper_Item(ShaderCaseProjector.TYPE, stack), IPContent.Items.PROJECTOR.get());
		event.registerItem(ItemHandler.ITEM, (stack, _void) -> new IPItemStackHandler(1), IPContent.Items.PROJECTOR.get());
	}
	
	private static <C, BE extends BlockEntity & IHasMultiCapability> void reg(RegisterCapabilitiesEvent event, BlockEntityType<BE> type, BlockCapability<C, Direction>[] caps){
		for(BlockCapability<C, Direction> cap: caps){
			event.registerBlockEntity(cap, type, (be, side) -> be.getCapability(cap, side));
		}
	}
	
	public interface IHasCapability{
		<T> T getCapability(Direction side);
	}
	
	public interface IHasMultiCapability{
		<C, T> T getCapability(BlockCapability<T, C> capability, Direction side);
	}
}
