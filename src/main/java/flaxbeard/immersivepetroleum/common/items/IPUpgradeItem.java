package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.tool.upgrade.IUpgrade;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeData;
import com.google.common.collect.ImmutableSet;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class IPUpgradeItem extends IPItemBase implements IUpgrade{
	private final Set<String> set;
	public IPUpgradeItem(String type){
		super(new Item.Properties().stacksTo(1));
		this.set = ImmutableSet.of(type);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext ctx, List<Component> tooltip, @Nonnull TooltipFlag flag){
		tooltip.add(Component.translatable("desc.immersivepetroleum.flavour." + RegistryUtils.getRegistryNameOf(this).getPath()));
	}
	
	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade){
		return this.set;
	}
	
	@Override
	public boolean canApplyUpgrades(UpgradeData existing, ItemStack upgrade){
		return true;
	}
	
	@Override
	public UpgradeData applyUpgrades(UpgradeData base, ItemStack upgrade){
		return null;
	}
}
