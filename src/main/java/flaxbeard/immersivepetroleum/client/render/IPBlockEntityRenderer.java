package flaxbeard.immersivepetroleum.client.render;

import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;

public abstract class IPBlockEntityRenderer<BE extends BlockEntity> implements BlockEntityRenderer<BE>{
	protected static final Quaternionf ROT_270 = Axis.YP.rotationDegrees(270F);
	protected static final Quaternionf ROT_180 = Axis.YP.rotationDegrees(180F);
	protected static final Quaternionf ROT_90 = Axis.YP.rotationDegrees(90F);
	protected static final Quaternionf ROT_0 = Axis.YP.rotationDegrees(0F);
}
