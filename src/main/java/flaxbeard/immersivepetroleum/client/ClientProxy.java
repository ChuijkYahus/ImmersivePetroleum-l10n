package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.mixin.accessors.client.GuiSubtitleOverlayAccess;
import blusunrize.lib.manual.ManualElementItem;
import blusunrize.lib.manual.ManualElementTable;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualEntry.EntryData;
import blusunrize.lib.manual.ManualEntry.SpecialElementData;
import blusunrize.lib.manual.ManualInstance;
import com.electronwill.nightconfig.core.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_CokerUnit;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_Derrick;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_DistillationTower;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_Hydrotreater;
import flaxbeard.immersivepetroleum.client.particle.FluidParticleData;
import flaxbeard.immersivepetroleum.client.render.RenderUtils;
import flaxbeard.immersivepetroleum.client.render.SeismicResultRenderer;
import flaxbeard.immersivepetroleum.client.render.debugging.DebugRenderHandler;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.PumpjackLogic;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.RecipeReloadListener;
import flaxbeard.immersivepetroleum.common.sound.IPEntitySound;
import flaxbeard.immersivepetroleum.common.sound.IPTickableSound;
import flaxbeard.immersivepetroleum.common.sound.IPWorldSound;
import flaxbeard.immersivepetroleum.common.sound.IPlaySound;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientProxy extends CommonProxy{
	
	@Override
	public void setup(FMLCommonSetupEvent event){
		NeoForge.EVENT_BUS.register(new ClientEventHandler());
		NeoForge.EVENT_BUS.register(new RecipeReloadListener(null));
		
		NeoForge.EVENT_BUS.register(new DebugRenderHandler());
		NeoForge.EVENT_BUS.register(new SeismicResultRenderer());
	}
	
	@Override
	public void registerContainersAndScreens(RegisterMenuScreensEvent ev){
		ev.register(IPMenuTypes.DISTILLATION_TOWER.getType(), IPContainerScreen_DistillationTower::new);
		ev.register(IPMenuTypes.COKER.getType(), IPContainerScreen_CokerUnit::new);
		ev.register(IPMenuTypes.DERRICK.getType(), IPContainerScreen_Derrick::new);
		ev.register(IPMenuTypes.HYDROTREATER.getType(), IPContainerScreen_Hydrotreater::new);
	}
	
	@Override
	public void completed(FMLLoadCompleteEvent event){
		event.enqueueWork(() -> ManualHelper.addConfigGetter(str -> switch(str){
			case "distillationtower_operationcost" -> (int) (1024 * IPServerConfig.REFINING.distillationTower_energyModifier.get());
			case "coker_operationcost" -> (int) (1024 * IPServerConfig.REFINING.cokerUnit_energyModifier.get());
			case "hydrotreater_operationcost_lower" -> (int) (80 * IPServerConfig.REFINING.hydrotreater_energyModifier.get());
			case "hydrotreater_operationcost_upper" -> (int) (512 * IPServerConfig.REFINING.hydrotreater_energyModifier.get());
			case "pumpjack_consumption" -> IPServerConfig.EXTRACTION.pumpjack_consumption.get();
			case "pumpjack_speed" -> IPServerConfig.EXTRACTION.pumpjack_speed.get();
			case "pumpjack_days" -> {
				int oil_min = 1000000;
				int oil_max = 5000000;
				for(RecipeHolder<ReservoirType> holder:ReservoirType.map.values()){
					ReservoirType reservoir = holder.value();
					
					if(reservoir.name.equals("oil")){
						oil_min = reservoir.minSize;
						oil_max = reservoir.maxSize;
						break;
					}
				}
				
				float averageSize = (oil_min + oil_max) / 2F;
				float pumpspeed = IPServerConfig.EXTRACTION.pumpjack_speed.get();
				yield Mth.floor((averageSize / pumpspeed) / 24000F);
			}
			case "autolubricant_speedup" -> 1.25D;
			case "portablegenerator_flux" -> FuelHandler.getFluxGeneratedPerTick(IPContent.Fluids.GASOLINE.source().get());
			default -> {
				// Last resort
				Config cfg = IPServerConfig.getRawConfig();
				if(cfg.contains(str)){
					yield cfg.get(str);
				}
				yield null;
			}
		}));
		
		setupManualPages();
	}
	
	// TODO I think this can technically be retired
	@Override
	public void renderTile(BlockEntity te, VertexConsumer iVertexBuilder, PoseStack transform, MultiBufferSource buffer){
		BlockEntityRenderer<BlockEntity> tesr = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te);
		
		// Crash prevention
		if(tesr == null)
			return;
		
		if(te instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper().getContext() != null && multiblockBE.getHelper().getContext().getState() instanceof PumpjackLogic.State){
			IMultiblockBEHelper<PumpjackLogic.State> helper = multiblockBE.getHelper().asType(IPContent.Multiblock.PUMPJACK);
			
			transform.pushPose();
			transform.mulPose(Axis.YN.rotationDegrees(90));
			transform.translate(1, 1, -2);
			
			float pt = 0;
			if(MCUtil.getPlayer() != null && helper != null && helper.getState() != null){
				helper.getState().activeTicks = MCUtil.getPlayer().tickCount;
				pt = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks(); // TODO Make sure this is correct
			}
			
			tesr.render(te, pt, transform, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
			transform.popPose();
		}else{
			transform.pushPose();
			transform.mulPose(Axis.YN.rotationDegrees(90));
			transform.translate(0, 1, -4);
			
			tesr.render(te, 0, transform, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
			transform.popPose();
		}
	}
	
	@Override
	public void drawUpperHalfSlab(PoseStack transform, ItemStack stack){
		
		// Render slabs on top half
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockState state = IEBlocks.MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD).defaultBlockState();
		BakedModel model = blockRenderer.getBlockModelShaper().getBlockModel(state);
		
		MultiBufferSource.BufferSource buffers = RenderUtils.immediate();
		
		transform.pushPose();
		transform.translate(0.0F, 0.5F, 1.0F);
		blockRenderer.getModelRenderer().renderModel(transform.last(), buffers.getBuffer(RenderType.solid()), state, model, 1.0F, 1.0F, 1.0F, -1, -1, ModelData.EMPTY, RenderType.cutout());
		transform.popPose();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void spawnSpillParticles(Level world, BlockPos pos, Fluid fluid, int particles, float yOffset, float flow){
		if(fluid == null || fluid == Fluids.EMPTY){
			return;
		}
		
		for(int i = 0;i < particles;i++){
			float xa = (world.random.nextFloat() - .5F) / 2F;
			float ya = 0.25F + (0.5F + (world.random.nextFloat() * 0.25F)) * flow / 800;
			float za = (world.random.nextFloat() - .5F) / 2F;
			
			float rx = (world.random.nextFloat() - .5F) * 0.5F;
			float rz = (world.random.nextFloat() - .5F) * 0.5F;
			
			double x = (pos.getX() + 0.5) + rx;
			double y = (pos.getY() + yOffset);
			double z = (pos.getZ() + 0.5) + rz;
			
			world.addParticle(new FluidParticleData(fluid), x, y, z, xa, ya, za);
		}
	}
	
	@Override
	public Level getClientWorld(){
		return MCUtil.getLevel();
	}
	
	@Override
	public Player getClientPlayer(){
		return MCUtil.getPlayer();
	}
	
	private final Map<BlockPos, IPWorldSound> worldSoundMap = new HashMap<>();
	private final Map<UUID, IPEntitySound> entitySoundMap = new HashMap<>();
	private static final int SUBTITLE_RANGE = 16;
	
	@Override
	public void handleTileSound(Holder<SoundEvent> soundEvent, BlockEntity te, boolean active, float volume, float pitch){
		final BlockPos pos = te.getBlockPos();
		
		IPWorldSound worldSound = this.worldSoundMap.get(pos);
		if(worldSound == null && active){
			if(te instanceof IPlaySound soundPlayer && MCUtil.getPlayer().distanceToSqr(Vec3.atCenterOf(pos)) > soundPlayer.soundRadiusSqr())
				return;
			
			playSound(this.worldSoundMap, new IPWorldSound(pos, soundEvent.value(), volume, pitch), pos);
			
		}else if(worldSound != null){
			if(worldSound.isStopped() || !active || (!soundEvent.value().getLocation().equals(worldSound.getLocation()))){
				stopSound(this.worldSoundMap, worldSound, pos);
				
			}else if(!worldSound.isStopped() && MCUtil.getPlayer().tickCount % 20 == 0){
				WeighedSoundEvents weighedSoundEvents = worldSound.resolve(MCUtil.getSoundManager());
				
				if(weighedSoundEvents != null){
					getSubtitleOverlay().onPlaySound(worldSound, weighedSoundEvents, SUBTITLE_RANGE);
				}
			}
		}
	}
	
	@Override
	public void handleEntitySound(Holder<SoundEvent> soundEvent, Entity entity, boolean active, float volume, float pitch){
		IPEntitySound entitySound = this.entitySoundMap.get(entity.getUUID());
		if(entitySound == null && active){
			if(entity instanceof IPlaySound soundPlayer && MCUtil.getPlayer().distanceToSqr(entity) > soundPlayer.soundRadiusSqr())
				return;
			
			playSound(this.entitySoundMap, new IPEntitySound(entity, soundEvent.value(), volume, pitch), entity.getUUID());
			
		}else if(entitySound != null){
			if(entitySound.isStopped() || !active || (!soundEvent.value().getLocation().equals(entitySound.getLocation()))){
				stopSound(this.entitySoundMap, entitySound, entity.getUUID());
				
			}else if(!entitySound.isStopped() && MCUtil.getPlayer().tickCount % 20 == 0){
				WeighedSoundEvents weighedSoundEvents = entitySound.resolve(MCUtil.getSoundManager());
				
				if(weighedSoundEvents != null){
					getSubtitleOverlay().onPlaySound(entitySound, weighedSoundEvents, SUBTITLE_RANGE);
				}
			}
		}
	}
	
	private <Key, Sound extends IPTickableSound, M extends Map<Key, Sound>> void playSound(M map, Sound sound, Key key){
		map.put(key, sound);
		MCUtil.getSoundManager().play(sound);
	}
	
	private <Key, Sound extends IPTickableSound, M extends Map<Key, Sound>> void stopSound(M map, Sound sound, Key key){
		if(sound == null)
			return;
		
		sound.stop();
		MCUtil.getSoundManager().stop(sound);
		map.remove(key);
	}
	
	private SubtitleOverlay getSubtitleOverlay(){
		return ((GuiSubtitleOverlayAccess) Minecraft.getInstance().gui).getSubtitleOverlay();
	}
	
	public void setupManualPages(){
		handleReservoirManual(ResourceUtils.ip("reservoir"), 0);
		flarestack(ResourceUtils.ip("flarestack"), 12);
	}
	
	@SuppressWarnings({"deprecation"})
	private static void flarestack(ResourceLocation location, int priority){
		ManualInstance man = ManualHelper.getManual();
		
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(man);
		builder.readFromFile(location);
		builder.appendText(() -> {
			List<Component[]> list = new ArrayList<>();
			BuiltInRegistries.FLUID.stream().forEach(fluid -> {
				for(TagKey<Fluid> tag:FlarestackHandler.getSet()){
					if(fluid.is(tag)){
						Component[] entry = new Component[]{Component.empty(), new FluidStack(fluid, 1).getHoverName()};
						list.add(entry);
					}
				}
			});
			
			StringBuilder additionalText = new StringBuilder();
			List<SpecialElementData> newElements = new ArrayList<>();
			int nextLine = 0;
			for(int page = 0;nextLine < list.size();++page){
				final int linesOnPage = page == 0 ? 12 : 14;
				final int endIndex = Math.min(nextLine + linesOnPage, list.size());
				List<Component[]> onPage = list.subList(nextLine, endIndex);
				nextLine = endIndex;
				final String key = "flarestack_table" + page;
				additionalText.append("<&").append(key).append(">");
				newElements.add(new SpecialElementData(key, 0, new ManualElementTable(man, onPage.toArray(Component[][]::new), false)));
			}
			return Pair.of(additionalText.toString(), newElements);
		});
		
		man.addEntry(man.getRoot().getOrCreateSubnode(ResourceUtils.ip("petroleum")), builder.create(), priority);
	}
	
	
	
	private static void handleReservoirManual(ResourceLocation location, int priority){
		ManualInstance man = ManualHelper.getManual();
		
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(man);
		builder.setContent(ClientProxy::createContent);
		builder.setLocation(location);
		man.addEntry(man.getRoot().addNewSubnode(ResourceUtils.ip("petroleum"), 100), builder.create(), priority);
	}
	
	protected static EntryData createContent(){
		ArrayList<SpecialElementData> itemList = new ArrayList<>();
		
		StringBuilder contentBuilder = new StringBuilder();
		for(int i = 0;i < 5;i++){
			String tString = "ie.manual.entry.reservoirs.oil" + i;
			if(I18n.get(tString).equals(tString))
				break;
			
			contentBuilder.append(I18n.get(tString));
		}
		
		createReservoirPages(contentBuilder, itemList);
		
		String translatedTitle = I18n.get("ie.manual.entry.reservoirs.title");
		String tanslatedSubtext = I18n.get("ie.manual.entry.reservoirs.subtitle");
		String formattedContent = contentBuilder.toString().replaceAll("\r\n|\r|\n", "\n");
		return new EntryData(translatedTitle, tanslatedSubtext, formattedContent, itemList);
	}
	
	/** Creates a page for every single currently registered reservoir */
	private static void createReservoirPages(StringBuilder contentBuilder, ArrayList<SpecialElementData> itemList){
		ReservoirType[] reservoirs = ReservoirType.map.values().stream().map(RecipeHolder::value).toArray(ReservoirType[]::new);
		
		for(int i = 0;i < reservoirs.length;i++){
			ReservoirType reservoir = reservoirs[i];
			
			ImmersivePetroleum.log.debug("Creating entry for " + reservoir);
			
			String name = "desc.immersivepetroleum.info.reservoir." + reservoir.name;
			String localizedName = I18n.get(name);
			if(localizedName.equalsIgnoreCase(name))
				localizedName = reservoir.name;
			
			char c = localizedName.toLowerCase().charAt(0);
			boolean isVowel = (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u');
			String aOrAn = I18n.get(isVowel ? "ie.manual.entry.reservoirs.vowel" : "ie.manual.entry.reservoirs.consonant");
			
			String dimBWList = "", bioBWList = "";
			
			if(reservoir.getDimensions().hasEntries()){
				StringBuilder strBuilder = new StringBuilder();
				
				reservoir.getDimensions().forEach(rl -> {
					strBuilder.append((!strBuilder.isEmpty()) ? ", " : "").append("<dim;").append(rl).append(">");
				});
				
				if(reservoir.getDimensions().isBlacklist()){
					dimBWList = I18n.get("ie.manual.entry.reservoirs.dim.invalid", localizedName, strBuilder.toString(), aOrAn);
				}else{
					dimBWList = I18n.get("ie.manual.entry.reservoirs.dim.valid", localizedName, strBuilder.toString(), aOrAn);
				}
			}else{
				dimBWList = I18n.get("ie.manual.entry.reservoirs.dim.any", localizedName, aOrAn);
			}
			
			if(reservoir.getBiomes().hasEntries()){
				StringBuilder strBuilder = new StringBuilder();
				
				reservoir.getBiomes().forEach(rl -> {
					Biome biome = RegistryUtils.getBiomeFromRegistryName(rl);
					strBuilder.append((!strBuilder.isEmpty()) ? ", " : "");
					strBuilder.append(biome != null ? biome.toString() : rl);
				});
				
				if(reservoir.getBiomes().isBlacklist()){
					bioBWList = I18n.get("ie.manual.entry.reservoirs.bio.invalid", strBuilder.toString());
				}else{
					bioBWList = I18n.get("ie.manual.entry.reservoirs.bio.valid", strBuilder.toString());
				}
			}else{
				bioBWList = I18n.get("ie.manual.entry.reservoirs.bio.any");
			}
			
			String fluidName = "";
			Fluid fluid = reservoir.getFluid();
			if(fluid != null){
				fluidName = new FluidStack(fluid, 1).getHoverName().getString();
			}
			
			String repRate = "";
			if(reservoir.residual > 0){
				if(reservoir.equilibrium > 0)
					repRate = I18n.get("ie.manual.entry.reservoirs.replenish", reservoir.residual, fluidName, Utils.fDecimal(reservoir.equilibrium / 1000));
				else
					repRate = I18n.get("ie.manual.entry.reservoirs.replenish_depleted", reservoir.residual, fluidName);
			}
			contentBuilder.append("<&").append(reservoir.getType().toString()).append(">");
			contentBuilder.append(I18n.get("ie.manual.entry.reservoirs.content", dimBWList, fluidName, Utils.fDecimal(reservoir.minSize / 1000), Utils.fDecimal(reservoir.maxSize / 1000), repRate, bioBWList));
			
			if(i < (reservoirs.length - 1))
				contentBuilder.append("<np>");
			
			itemList.add(new SpecialElementData(reservoir.getType().toString(), 0, new ManualElementItem(ManualHelper.getManual(), new ItemStack(fluid.getBucket()))));
		}
	}
}
