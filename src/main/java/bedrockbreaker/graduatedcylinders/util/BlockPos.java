package bedrockbreaker.graduatedcylinders.util;

import net.minecraftforge.common.util.ForgeDirection;

public class BlockPos {
	
	private int x;
	private int y;
	private int z;

	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}

	public BlockPos offset(ForgeDirection facing) {
		return new BlockPos(this.x + facing.offsetX, this.y + facing.offsetY, this.z + facing.offsetZ);
	}
}
