package flaxbeard.immersivepetroleum.client.render.multiblock;

record Face(int x, int y, int w, int h, float w16, float h16, float u0, float v0, float u1, float v1){
	public static Face of(int x, int y, int w, int h){
		float u0 = x / 64F;
		float v0 = y / 64F;
		float u1 = u0 + (w / 64F);
		float v1 = v0 + (h / 64F);
		float w16 = w / 16F;
		float h16 = h / 16F;
		return new Face(x, y, w, h, w16, h16, u0, v0, u1, v1);
	}
}
