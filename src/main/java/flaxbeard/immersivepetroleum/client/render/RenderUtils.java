package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class RenderUtils{
	
	/*
		Both "immediate" methods may not be a good idea.
		But for now, this is only a tool to make the red go away.
	 */
	
	public static MultiBufferSource.BufferSource immediate(){
		return immediate(0x4000);
	}
	
	public static MultiBufferSource.BufferSource immediate(int bufferCapacity){
		return MultiBufferSource.immediate(new ByteBufferBuilder(bufferCapacity));
	}
	
	public static void drawColouredRect(GuiGraphics graphics, int x, int y, int w, int h){
		Matrix4f mat = graphics.pose().last().pose();
		VertexConsumer buffer = graphics.bufferSource().getBuffer(IPRenderTypes.TRANSLUCENT_POSITION_COLOR);
		int color = 0xAF_000000;
		buffer.addVertex(mat, x, y + h, 0).setColor(color);
		buffer.addVertex(mat, x + w, y + h, 0).setColor(color);
		buffer.addVertex(mat, x + w, y, 0).setColor(color);
		buffer.addVertex(mat, x, y, 0).setColor(color);
	}
}
