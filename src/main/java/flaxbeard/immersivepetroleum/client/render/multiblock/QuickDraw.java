package flaxbeard.immersivepetroleum.client.render.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

record QuickDraw(VertexConsumer buf, PoseStack pose, int colorARGB, int overlay, int light){
	public void vertex(float x, float y, float z, float u, float v){
		//@formatter:off
		this.buf.addVertex(this.pose.last().pose(), x, y, z)
			.setColor(this.colorARGB)
			.setUv(u, v)
			.setOverlay(this.overlay)
			.setLight(this.light)
			.setNormal(1, 1, 1);
		//@formatter:on
	}
}
