package flaxbeard.immersivepetroleum.client.gui.displays;

public class TexCoords{
	public static final float TEX_SIZE = 128; // 128x128
	
	private final int x, y, w, h;
	private final float u0, v0, u1, v1;
	public TexCoords(int x, int y, int w, int h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		this.u0 = this.x / TEX_SIZE;
		this.v0 = this.y / TEX_SIZE;
		this.u1 = (this.x + this.w) / TEX_SIZE;
		this.v1 = (this.y + this.h) / TEX_SIZE;
	}
	
	public int x(){
		return this.x;
	}
	
	public int y(){
		return this.y;
	}
	
	public int w(){
		return this.w;
	}
	
	public int h(){
		return this.h;
	}
	
	public float u0(){
		return this.u0;
	}
	
	public float v0(){
		return this.v0;
	}
	
	public float u1(){
		return this.u1;
	}
	
	public float v1(){
		return this.v1;
	}
}
