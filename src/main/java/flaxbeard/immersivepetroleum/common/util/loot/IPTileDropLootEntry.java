package flaxbeard.immersivepetroleum.common.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.common.blocks.interfaces.IBlockEntityDrop;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class IPTileDropLootEntry extends LootPoolSingletonContainer{
	public static final ResourceLocation ID = ResourceUtils.ip("tile_drop");
	
	public static final MapCodec<IPTileDropLootEntry> CODEC = RecordCodecBuilder.mapCodec(
		inst -> singletonFields(inst).apply(inst, IPTileDropLootEntry::new)
	);
	
	protected IPTileDropLootEntry(int weightIn, int qualityIn, List<LootItemCondition> conditionsIn, List<LootItemFunction> functionsIn){
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}
	
	@Override
	protected void createItemStack(@Nonnull Consumer<ItemStack> stackConsumer, LootContext context){
		if(context.hasParam(LootContextParams.BLOCK_ENTITY)){
			BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if(te instanceof IBlockEntityDrop dropTe){
				dropTe.getBlockEntityDrop(context).forEach(stackConsumer);
			}
		}
	}
	
	@Override
	@Nonnull
	public LootPoolEntryType getType(){
		return IPLootFunctions.TILE_DROP.value();
	}
	
	public static LootPoolSingletonContainer.Builder<?> builder(){
		return simpleBuilder(IPTileDropLootEntry::new);
	}
}
