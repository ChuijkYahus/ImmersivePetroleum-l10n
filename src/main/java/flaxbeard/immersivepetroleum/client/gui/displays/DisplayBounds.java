package flaxbeard.immersivepetroleum.client.gui.displays;

public record DisplayBounds(int x0, int y0, int x1, int y1, int width, int height){
	
	public static DisplayBounds of(int x, int y, int width, int height){
		return new DisplayBounds(x, y, x + width, y + height, width, height);
	}
	
	public DisplayBounds add(int x0, int y0, int x1, int y1){
		return new DisplayBounds(this.x0 + x0, this.y0 + y0, this.x1 + x1, this.y1 + y1, this.width, this.height);
	}
	
	public DisplayBounds move(int x, int y){
		return new DisplayBounds(this.x0 + x, this.y0 + y, this.x1 + x, this.y1 + y, this.width, this.height);
	}
	
	public boolean contains(int x, int y){
		return (x >= this.x0 && x < this.x1) && (y >= this.y0 && y < this.y1);
	}
}
