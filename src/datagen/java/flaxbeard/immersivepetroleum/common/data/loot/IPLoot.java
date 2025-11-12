package flaxbeard.immersivepetroleum.common.data.loot;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class IPLoot implements LootTableSubProvider{
	public IPLoot(HolderLookup.Provider p){
	}
	
	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> pOutput){
		//@formatter:off
		LootPool.Builder pool = LootPool.lootPool()
				.name("cookie_for_your_trouble")
				.add(LootItem.lootTableItem(Items.COOKIE))
				.setRolls(ConstantValue.exactly(1))
				.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));
		//@formatter:on
		
		pOutput.accept(key("advancements/forming_coker_reward"), LootTable.lootTable().withPool(pool));
	}
	
	private ResourceKey<LootTable> key(String str){
		return ResourceKey.create(Registries.LOOT_TABLE, ResourceUtils.ip(str));
	}
}
