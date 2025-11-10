package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.fluids.NapalmFluid;
import flaxbeard.immersivepetroleum.common.util.IPEffects;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CommonEventHandler{
	@SubscribeEvent
	public void onSave(LevelEvent.Save event){
		if(!event.getLevel().isClientSide()){
			IPSaveData.markDirty();
		}
	}
	
	@SubscribeEvent
	public void onUnload(LevelEvent.Unload event){
		if(!event.getLevel().isClientSide()){
			IPSaveData.markDirty();
			ReservoirRegionDataStorage.get().markAllDirty();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onServerStopped(ServerStoppedEvent event){
		ImmersivePetroleum.log.debug("[ReservoirIslands]: Clearing Cache...");
		ReservoirHandler.clearCache();
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(LivingDamageEvent.Pre event){
		Level level = event.getEntity().level();
		DamageSources dmg = level.damageSources();
		if(event.getSource() == dmg.lava() || event.getSource() == dmg.onFire() || event.getSource() == dmg.inFire()){
			LivingEntity entity = event.getEntity();
			if(entity.getVehicle() instanceof MotorboatEntity boat){
				if(boat.isFireproof){
					event.setNewDamage(0.0F);//event.setCanceled(true); // TODO Check that this works as expected
					return;
				}
			}
			
			if(entity.getRemainingFireTicks() > 0 && entity.getEffect(IPEffects.ANTI_DISMOUNT_FIRE) != null){
				entity.clearFire();
				entity.removeEffect(IPEffects.ANTI_DISMOUNT_FIRE);
				event.setNewDamage(0.0F);//event.setCanceled(true); // TODO Check that this works as expected
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(PlayerTickEvent event){
		Player entity = event.getEntity();
		if(entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity boat){
			if(boat.isFireproof){
				entity.clearFire();
				boat.setSharedFlag(0, false);
			}
		}
	}
	
	/**
	 * Handles dismounting the Speedboat while in lava to trying to avoid getting burned
	 */
	@SubscribeEvent
	public void handleDismountingBoat(EntityMountEvent event){
		if(event.getEntityMounting() instanceof LivingEntity living && event.getEntityBeingMounted() instanceof MotorboatEntity boat){
			if(event.isDismounting()){
				if(boat.isFireproof){
					FluidState fluidstate = event.getLevel().getBlockState(BlockPos.containing(boat.position().add(0.5, 0, 0.5))).getFluidState();
					if(fluidstate != Fluids.EMPTY.defaultFluidState() && fluidstate.is(FluidTags.LAVA)){
						living.addEffect(new MobEffectInstance(IPEffects.ANTI_DISMOUNT_FIRE, 1, 0, false, false));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesServer(LevelTickEvent.Post event){
		handleLubricatingMachines(event.getLevel());
	}
	
	static final Random random = new Random();
	@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
	public static void handleLubricatingMachines(Level world){
		Set<LubricatedTileInfo> toRemove = new HashSet<>();
		for(LubricatedTileInfo info:LubricatedHandler.lubricatedTiles){
			if(info.world == world.dimension() && world.isAreaLoaded(info.pos, 0)){
				BlockEntity blockEntity = world.getBlockEntity(info.pos);
				
				if(blockEntity instanceof IMultiblockBE<?> multiblockBE){
					final IMultiblockBEHelperMaster<?> masterHelper = Utils.getMultiblockMasterHelper(world, multiblockBE.getHelper());
					
					ILubricationHandler lubeHandler = LubricatedHandler.getHandlerForTile(masterHelper);
					if(lubeHandler != null){
						
						if(lubeHandler.isMachineEnabled(world, masterHelper)){
							if(world.isClientSide){
								lubeHandler.lubricateClient((ClientLevel) world, info.lubricant, info.ticks, masterHelper);
							}else{
								lubeHandler.lubricateServer((ServerLevel) world, info.lubricant, info.ticks, masterHelper);
							}
						}
						
						if(world.isClientSide && multiblockBE.getHelper().getContext() != null){
							IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
							IMultiblockLevel mbLevel = helper.getContext().getLevel();
							Vec3i size = helper.getMultiblock().getSize().apply(world);
							
							for(int y = 0;y < size.getY();y++){
								for(int z = 0;z < size.getZ();z++){
									for(int x = 0;x < size.getX();x++){
										if(random.nextFloat() >= 0.1F)
											continue;
										
										BlockPos pos = mbLevel.toAbsolute(new BlockPos(x, y, z));
										if(world.getBlockEntity(pos) instanceof IMultiblockBE<?> mb && mb.getHelper().getContext().getState() == helper.getContext().getState()){
											Direction randomDir = Direction.Plane.HORIZONTAL.getRandomDirection(world.random);
											
											if(!world.isEmptyBlock(pos.relative(randomDir)))
												continue;
											
											float px = pos.getX();
											float py = pos.getY();
											float pz = pos.getZ();
											
											switch(randomDir){
												case NORTH -> {
													px += random.nextFloat();
													py += random.nextFloat();
													pz -= 0.0625F;
												}
												case EAST -> {
													px += 1.0625F;
													py += random.nextFloat();
													pz += random.nextFloat();
												}
												case SOUTH -> {
													px += random.nextFloat();
													py += random.nextFloat();
													pz += 1.0625F;
												}
												case WEST -> {
													px -= 0.0625F;
													py += random.nextFloat();
													pz += random.nextFloat();
												}
											}
											
											world.addParticle(ParticleTypes.FALLING_HONEY, px, py, pz, 0, 0, 0);
										}
									}
								}
							}
						}
						
						if(info.ticks-- <= 0)
							toRemove.add(info);
					}
				}
			}
		}
		
		for(LubricatedTileInfo info:toRemove){
			LubricatedHandler.lubricatedTiles.remove(info);
		}
	}
	
	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinLevelEvent event){
		if(event.getEntity() instanceof Player player){
			if(event.getEntity() instanceof FakePlayer){
				return;
			}
			
			if(IPServerConfig.MISCELLANEOUS.autounlock_recipes.get()){
				List<RecipeHolder<?>> l = new ArrayList<>();
				Collection<RecipeHolder<?>> recipes = event.getLevel().getRecipeManager().getRecipes();
				recipes.forEach(recipe -> {
					ResourceLocation name = recipe.id();
					if(name.getNamespace().equals(ImmersivePetroleum.MODID)){
						l.add(recipe);
					}
				});
				
				player.awardRecipes(l);
			}
		}
	}
	
	@SubscribeEvent
	public void livingDeath(LivingDeathEvent event){
		if(event.getEntity() instanceof Skeleton skelly && !skelly.level().isClientSide){
			DamageSource src = event.getSource();
			if(src.getEntity() instanceof Player player && !player.level().isClientSide){
				if(player.getVehicle() instanceof MotorboatEntity motorboat && !motorboat.level().isClientSide){
					if(!src.isDirect() && motorboat.isSpinningFastEnough() && motorboat.hasRudders){
						Utils.unlockIPAdvancement(player, "main/rudders");
					}
				}
			}
		}
	}
	
	public static final Map<ResourceLocation, List<BlockPos>> napalmPositions = new HashMap<>();
	public static final Map<ResourceLocation, List<BlockPos>> toRemove = new HashMap<>();
	
	@SubscribeEvent
	public void handleNapalm(LevelTickEvent event){
		if(event.getLevel().isClientSide())
			return;
		
		ResourceLocation d = event.getLevel().dimension().location();
		
		if(event instanceof LevelTickEvent.Pre pre){
			if(napalmPositions.get(d) != null){
				List<BlockPos> trList = toRemove.computeIfAbsent(d, f -> new ArrayList<>());
				
				new ArrayList<>(napalmPositions.get(d)).forEach(pos -> {
					BlockState state = event.getLevel().getBlockState(pos);
					if(state.getBlock() instanceof LiquidBlock fluidBlock && fluidBlock == IPContent.Fluids.NAPALM.block().get()){
						NapalmFluid.processFire(IPContent.Fluids.NAPALM, event.getLevel(), pos);
					}
					trList.add(pos);
				});
			}
			
		}else if(event instanceof LevelTickEvent.Post post){
			if(toRemove.get(d) != null && napalmPositions.get(d) != null){
				List<BlockPos> list = new ArrayList<>(toRemove.get(d));
				napalmPositions.get(d).removeAll(list);
				toRemove.get(d).clear();
			}
			
		}
	}
}
