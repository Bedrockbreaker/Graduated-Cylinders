package bedrockbreaker.graduatedcylinders.Util;

import net.minecraft.util.math.BlockPos;

// Because the normal built-in vector class from the minecraft source code sucks
public class Vec3d {

	public double x;
	public double y;
	public double z;

	public Vec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3d() {
		this(0, 0, 0);
	}

	public Vec3d(Vec3d other) {
		this(other.x, other.y, other.z);
	}

	public Vec3d(BlockPos blockPos) {
		this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public static Vec3d cross(Vec3d vec1, Vec3d vec2) {
		return new Vec3d(vec1.y * vec2.z - vec1.z * vec2.y, vec2.x * vec1.z - vec2.z * vec1.x, vec1.x * vec2.y - vec1.y * vec2.x);
	}

	public Vec3d set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vec3d set(Vec3d other) {
		return this.set(other.x, other.y, other.z);
	}

	public Vec3d set(BlockPos blockPos) {
		return this.set(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public Vec3d add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec3d add(double a) {
		return this.add(a, a, a);
	}

	public Vec3d add(Vec3d other) {
		return this.add(other.x, other.y, other.z);
	}

	public Vec3d add(BlockPos blockPos) {
		return this.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public Vec3d sub(double a) {
		return this.add(-a, -a, -a);
	}

	public Vec3d sub(Vec3d other) {
		return this.add(-other.x, -other.y, -other.z);
	}

	public Vec3d sub(BlockPos blockPos) {
		return this.add(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
	}

	public Vec3d scale(double s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}

	public double getMagnitude() {
		return Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}

	public Vec3d normalize() {
		return this.scale(1/this.getMagnitude());
	}

	public Vec3d cross(Vec3d other) {
		return this.set(Vec3d.cross(this, other));
	}
}
