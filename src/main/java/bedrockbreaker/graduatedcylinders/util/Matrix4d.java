package bedrockbreaker.graduatedcylinders.util;

// Because the normal Matrix4d class sucks
public class Matrix4d {

	public double m00 = 0;
	public double m01 = 0;
	public double m02 = 0;
	public double m03 = 0;
	public double m10 = 0;
	public double m11 = 0;
	public double m12 = 0;
	public double m13 = 0;
	public double m20 = 0;
	public double m21 = 0;
	public double m22 = 0;
	public double m23 = 0;
	public double m30 = 0;
	public double m31 = 0;
	public double m32 = 0;
	public double m33 = 0;
	
	public Matrix4d() {}

	public Matrix4d(double[] elements) {
		this.m00 = elements[0];
		this.m01 = elements[1];
		this.m02 = elements[2];
		this.m03 = elements[3];

		this.m10 = elements[4];
		this.m11 = elements[5];
		this.m12 = elements[6];
		this.m13 = elements[7];

		this.m20 = elements[8];
		this.m21 = elements[9];
		this.m22 = elements[10];
		this.m23 = elements[11];

		this.m30 = elements[12];
		this.m31 = elements[13];
		this.m32 = elements[14];
		this.m33 = elements[15];
	}

	public Matrix4d(Matrix4d other) {
		this.m00 = other.m00;
		this.m01 = other.m01;
		this.m02 = other.m02;
		this.m03 = other.m03;

		this.m10 = other.m10;
		this.m11 = other.m11;
		this.m12 = other.m12;
		this.m13 = other.m13;

		this.m20 = other.m20;
		this.m21 = other.m21;
		this.m22 = other.m22;
		this.m23 = other.m23;

		this.m30 = other.m30;
		this.m31 = other.m31;
		this.m32 = other.m32;
		this.m33 = other.m33;
	}

	private static double determinant3x3(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
		return m00 * (m11 * m22 - m12 * m21) + m01 * (m12 * m20 - m10 * m22) + m02 * (m10 * m21 - m11 * m20);
	}

	public static Matrix4d multiply(Matrix4d m1, Matrix4d m2) {
		return new Matrix4d(new double[] {
			m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20 + m1.m03 * m2.m30,
			m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21 + m1.m03 * m2.m31,
			m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22 + m1.m03 * m2.m32,
			m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03 * m2.m33,

			m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20 + m1.m13 * m2.m30,
			m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21 + m1.m13 * m2.m31,
			m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22 + m1.m13 * m2.m32,
			m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13 * m2.m33,

			m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20 + m1.m23 * m2.m30,
			m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21 + m1.m23 * m2.m31,
			m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22 + m1.m23 * m2.m32,
			m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23 * m2.m33,

			m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20 + m1.m33 * m2.m30,
			m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21 + m1.m33 * m2.m31,
			m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22 + m1.m33 * m2.m32,
			m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23 + m1.m33 * m2.m33,
		});
	}

	public Matrix4d setIdentity() {
		this.m00 = 1;
		this.m01 = 0;
		this.m02 = 0;
		this.m03 = 0;

		this.m10 = 0;
		this.m11 = 1;
		this.m12 = 0;
		this.m13 = 0;

		this.m20 = 0;
		this.m21 = 0;
		this.m22 = 1;
		this.m23 = 0;

		this.m30 = 0;
		this.m31 = 0;
		this.m32 = 0;
		this.m33 = 1;
		
		return this;
	}

	public Matrix4d set(double[] elements) {
		this.m00 = elements[0];
		this.m01 = elements[1];
		this.m02 = elements[2];
		this.m03 = elements[3];

		this.m10 = elements[4];
		this.m11 = elements[5];
		this.m12 = elements[6];
		this.m13 = elements[7];

		this.m20 = elements[8];
		this.m21 = elements[9];
		this.m22 = elements[10];
		this.m23 = elements[11];

		this.m30 = elements[12];
		this.m31 = elements[13];
		this.m32 = elements[14];
		this.m33 = elements[15];

		return this;
	}

	public Matrix4d set(Matrix4d other) {
		this.m00 = other.m00;
		this.m01 = other.m01;
		this.m02 = other.m02;
		this.m03 = other.m03;

		this.m10 = other.m10;
		this.m11 = other.m11;
		this.m12 = other.m12;
		this.m13 = other.m13;

		this.m20 = other.m20;
		this.m21 = other.m21;
		this.m22 = other.m22;
		this.m23 = other.m23;

		this.m30 = other.m30;
		this.m31 = other.m31;
		this.m32 = other.m32;
		this.m33 = other.m33;

		return this;
	}

	public Matrix4d scale(double a) {
		this.m00 *= a;
		this.m01 *= a;
		this.m02 *= a;
		this.m03 *= a;

		this.m10 *= a;
		this.m11 *= a;
		this.m12 *= a;
		this.m13 *= a;

		this.m20 *= a;
		this.m21 *= a;
		this.m22 *= a;
		this.m23 *= a;

		this.m30 *= a;
		this.m31 *= a;
		this.m32 *= a;
		this.m33 *= a;

		return this;
	}

	public Matrix4d multiply(Matrix4d other) {
		return this.set(Matrix4d.multiply(this, other));
	}

	public Matrix4d transpose() {
		double temp = this.m10;
		this.m10 = this.m01;
		this.m01 = temp;

		temp = this.m20;
		this.m20 = this.m02;
		this.m02 = temp;

		temp = this.m30;
		this.m30 = this.m03;
		this.m03 = temp;

		temp = this.m21;
		this.m21 = this.m12;
		this.m12 = temp;

		temp = this.m31;
		this.m31 = this.m13;
		this.m13 = temp;

		temp = this.m32;
		this.m32 = this.m23;
		this.m23 = temp;

		return this;
	}

	public double getDeterminant() {
		return this.m00 * Matrix4d.determinant3x3(this.m11, this.m12, this.m13, this.m21, this.m22, this.m23, this.m31, this.m32, this.m33)
			 - this.m10 * Matrix4d.determinant3x3(this.m01, this.m02, this.m03, this.m21, this.m22, this.m23, this.m31, this.m32, this.m33)
			 + this.m20 * Matrix4d.determinant3x3(this.m01, this.m02, this.m03, this.m11, this.m12, this.m13, this.m31, this.m32, this.m33)
			 - this.m30 * Matrix4d.determinant3x3(this.m01, this.m02, this.m03, this.m11, this.m12, this.m13, this.m21, this.m22, this.m23);
	}

	public Matrix4d invert() {
		double determinant = this.getDeterminant();
		if (determinant == 0) throw new RuntimeException("Cannot invert a matrix whose determinant is 0");

		double t00 = Matrix4d.determinant3x3(this.m11, this.m21, this.m31, this.m12, this.m22, this.m32, this.m13, this.m23, this.m33);
		double t01 = -Matrix4d.determinant3x3(this.m01, this.m21, this.m31, this.m02, this.m22, this.m32, this.m03, this.m23, this.m33);
		double t02 = Matrix4d.determinant3x3(this.m01, this.m11, this.m31, this.m02, this.m12, this.m32, this.m03, this.m13, this.m33);
		double t03 = -Matrix4d.determinant3x3(this.m01, this.m11, this.m21, this.m02, this.m12, this.m22, this.m03, this.m13, this.m23);

		double t10 = -Matrix4d.determinant3x3(this.m10, this.m20, this.m30, this.m12, this.m22, this.m32, this.m13, this.m23, this.m33);
		double t11 = Matrix4d.determinant3x3(this.m00, this.m20, this.m30, this.m02, this.m22, this.m32, this.m03, this.m23, this.m33);
		double t12 = -Matrix4d.determinant3x3(this.m00, this.m10, this.m30, this.m02, this.m12, this.m32, this.m03, this.m13, this.m33);
		double t13 = Matrix4d.determinant3x3(this.m00, this.m10, this.m20, this.m02, this.m12, this.m22, this.m03, this.m13, this.m23);

		double t20 = Matrix4d.determinant3x3(this.m10, this.m20, this.m30, this.m11, this.m21, this.m31, this.m13, this.m23, this.m33);
		double t21 = -Matrix4d.determinant3x3(this.m00, this.m20, this.m30, this.m01, this.m21, this.m31, this.m03, this.m23, this.m33);
		double t22 = Matrix4d.determinant3x3(this.m00, this.m10, this.m30, this.m01, this.m11, this.m31, this.m03, this.m13, this.m33);
		double t23 = -Matrix4d.determinant3x3(this.m00, this.m10, this.m20, this.m01, this.m11, this.m21, this.m03, this.m13, this.m23);

		double t30 = -Matrix4d.determinant3x3(this.m10, this.m20, this.m30, this.m11, this.m21, this.m31, this.m12, this.m22, this.m32);
		double t31 = Matrix4d.determinant3x3(this.m00, this.m20, this.m30, this.m01, this.m21, this.m31, this.m02, this.m22, this.m32);
		double t32 = -Matrix4d.determinant3x3(this.m00, this.m10, this.m30, this.m01, this.m11, this.m31, this.m02, this.m12, this.m32);
		double t33 = Matrix4d.determinant3x3(this.m00, this.m10, this.m20, this.m01, this.m11, this.m21, this.m02, this.m12, this.m22);

		return this.set(new double[] {t00, t01, t02, t03, t10, t11, t12, t13, t20, t21, t22, t23, t30, t31, t32, t33}).scale(1 / determinant);
	}

	public Vec3d getTranslationVector() {
		return new Vec3d(this.m03, this.m13, this.m23);
	}

	public Matrix4d setTranslation(Vec3d translation) {
		this.m03 = translation.x;
		this.m13 = translation.y;
		this.m23 = translation.z;
		return this;
	}

	public Matrix4d setToRotationX(double radians) {
		this.setIdentity();
		double sin = Math.sin(radians);
		double cos = Math.cos(radians);

		this.m11 = cos;
		this.m12 = -sin;
		this.m21 = sin;
		this.m22 = cos;

		return this;
	}

	public Matrix4d setToRotationY(double radians) {
		this.setIdentity();
		double sin = Math.sin(radians);
		double cos = Math.cos(radians);

		this.m00 = cos;
		this.m02 = sin;
		this.m20 = -sin;
		this.m22 = cos;

		return this;
	}

	public Vec3d transform(Vec3d vectorOut) {
		return vectorOut.set(
			this.m00 * vectorOut.x + this.m01 * vectorOut.y + this.m02 * vectorOut.z + m03,
			this.m10 * vectorOut.x + this.m11 * vectorOut.y + this.m12 * vectorOut.z + m13,
			this.m20 * vectorOut.x + this.m21 * vectorOut.y + this.m22 * vectorOut.z + m23
		);
	}

	public Vec3d transformNormal(Vec3d vectorOut) {
		return vectorOut.set(
			this.m00 * vectorOut.x + this.m01 * vectorOut.y + this.m02 * vectorOut.z,
			this.m10 * vectorOut.x + this.m11 * vectorOut.y + this.m12 * vectorOut.z,
			this.m20 * vectorOut.x + this.m21 * vectorOut.y + this.m22 * vectorOut.z
		);
	}
}
