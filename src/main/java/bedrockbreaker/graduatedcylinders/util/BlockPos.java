package bedrockbreaker.graduatedcylinders.util;

import net.minecraft.util.EnumFacing;

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

	public BlockPos offset(EnumFacing facing) {
		return new BlockPos(this.x + facing.getFrontOffsetX(), this.y + facing.getFrontOffsetY(), this.z + facing.getFrontOffsetZ());
	}
}
