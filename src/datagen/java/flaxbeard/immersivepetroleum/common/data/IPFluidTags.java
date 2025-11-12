package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.api.IETags;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.Tags.Fluids;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class IPFluidTags extends FluidTagsProvider{
	public IPFluidTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper exHelper){
		super(output, lookup, ImmersivePetroleum.MODID, exHelper);
	}
	
	@Nonnull
	@Override
	public String getName(){
		return getClass().getSimpleName();
	}
	
	@Override
	protected void addTags(@Nonnull HolderLookup.Provider provider){
		tag(IPTags.Fluids.crudeOil).add(IPContent.Fluids.CRUDEOIL.get());
		
		tag(IPTags.Fluids.naphtha).add(IPContent.Fluids.NAPHTHA.get());
		tag(IPTags.Fluids.kerosene).add(IPContent.Fluids.KEROSENE.get());
		tag(IPTags.Fluids.diesel_sulfur).add(IPContent.Fluids.DIESEL_SULFUR.get());
		tag(IPTags.Fluids.lubricant).add(IPContent.Fluids.LUBRICANT.get());
		
		tag(IPTags.Fluids.diesel)
			.add(IPContent.Fluids.DIESEL.get())
			.add(IPContent.Fluids.DIESEL_SULFUR.get());
		tag(IPTags.Fluids.gasoline).add(IPContent.Fluids.GASOLINE.get());
		
		tag(IPTags.Fluids.benzol).add(IPContent.Fluids.BENZOL.get());
		tag(IPTags.Fluids.petroleum_gas).add(IPContent.Fluids.PETROLEUM_GAS.get());
		
		tag(IPTags.Fluids.napalm).add(IPContent.Fluids.NAPALM.get());
		
		tag(IPTags.Utility.burnableInFlarestack)
			.addTag(IPTags.Fluids.lubricant)
			.addTag(IPTags.Fluids.diesel)
			.addTag(IPTags.Fluids.diesel_sulfur)
			.addTag(IPTags.Fluids.gasoline)
			.addTag(IPTags.Fluids.naphtha)
			.addTag(IPTags.Fluids.benzol)
			.addTag(IPTags.Fluids.petroleum_gas)
			.addTag(IPTags.Fluids.kerosene)
			.addTag(IETags.fluidPlantoil)
			.addTag(IETags.fluidCreosote)
			.addTag(IETags.fluidEthanol);
		
		tag(IETags.drillFuel)
			.addTag(IPTags.Fluids.diesel)
			.addTag(IPTags.Fluids.kerosene)
			.addTag(IPTags.Fluids.diesel_sulfur);
		
		tag(Fluids.GASEOUS)
			.add(IPContent.Fluids.PETROLEUM_GAS.get());
	}
}
