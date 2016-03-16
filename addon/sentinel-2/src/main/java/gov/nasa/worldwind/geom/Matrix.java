/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

/**
 * @author dcollins
 * @version $Id$
 */
public class Matrix
{
    public static final Matrix IDENTITY = new Matrix(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1,
        true);

    // Row 1
    public final double m11;
    public final double m12;
    public final double m13;
    public final double m14;
    // Row 2
    public final double m21;
    public final double m22;
    public final double m23;
    public final double m24;
    // Row 3
    public final double m31;
    public final double m32;
    public final double m33;
    public final double m34;
    // Row 4
    public final double m41;
    public final double m42;
    public final double m43;
    public final double m44;

    protected static final double EPSILON = 1.0e-6;

    // 16 values in a 4x4 matrix.
    private static final int NUM_ELEMENTS = 16;
    // True when this matrix represents a 3D transform.
    private final boolean isOrthonormalTransform;
    // Cached computations.
    private int hashCode;

    public Matrix(double value)
    {
        // 'value' is placed in the diagonal.
        this(
            value, 0, 0, 0,
            0, value, 0, 0,
            0, 0, value, 0,
            0, 0, 0, value);
    }

    public Matrix(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44)
    {
        this(
            m11, m12, m13, m14,
            m21, m22, m23, m24,
            m31, m32, m33, m34,
            m41, m42, m43, m44,
            false);
    }

    Matrix(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44,
        boolean isOrthonormalTransform)
    {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
        this.isOrthonormalTransform = isOrthonormalTransform;
    }

    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Matrix that = (Matrix) obj;
        return (this.m11 == that.m11) && (this.m12 == that.m12) && (this.m13 == that.m13) && (this.m14 == that.m14)
            && (this.m21 == that.m21) && (this.m22 == that.m22) && (this.m23 == that.m23) && (this.m24 == that.m24)
            && (this.m31 == that.m31) && (this.m32 == that.m32) && (this.m33 == that.m33) && (this.m34 == that.m34)
            && (this.m41 == that.m41) && (this.m42 == that.m42) && (this.m43 == that.m43) && (this.m44 == that.m44);
    }

    public final int hashCode()
    {
        if (this.hashCode == 0)
        {
            int result;
            long tmp;
            tmp = Double.doubleToLongBits(this.m11);
            result = (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m12);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m13);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m14);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m21);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m22);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m23);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m24);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m31);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m32);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m33);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m34);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m41);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m42);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m43);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m44);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            this.hashCode = result;
        }
        return this.hashCode;
    }

    public static Matrix fromArray(double[] compArray, int offset, boolean rowMajor)
    {
        if (compArray == null)
        {
            throw new IllegalArgumentException("Array Is Null");
        }
        if ((compArray.length - offset) < NUM_ELEMENTS)
        {
            throw new IllegalArgumentException("Array Invalid Length");
        }

        if (rowMajor)
        {
            //noinspection PointlessArithmeticExpression
            return new Matrix(
                // Row 1
                compArray[0 + offset],
                compArray[1 + offset],
                compArray[2 + offset],
                compArray[3 + offset],
                // Row 2
                compArray[4 + offset],
                compArray[5 + offset],
                compArray[6 + offset],
                compArray[7 + offset],
                // Row 3
                compArray[8 + offset],
                compArray[9 + offset],
                compArray[10 + offset],
                compArray[11 + offset],
                // Row 4
                compArray[12 + offset],
                compArray[13 + offset],
                compArray[14 + offset],
                compArray[15 + offset]);
        }
        else
        {
            //noinspection PointlessArithmeticExpression
            return new Matrix(
                // Row 1
                compArray[0 + offset],
                compArray[4 + offset],
                compArray[8 + offset],
                compArray[12 + offset],
                // Row 2
                compArray[1 + offset],
                compArray[5 + offset],
                compArray[9 + offset],
                compArray[13 + offset],
                // Row 3
                compArray[2 + offset],
                compArray[6 + offset],
                compArray[10 + offset],
                compArray[14 + offset],
                // Row 4
                compArray[3 + offset],
                compArray[7 + offset],
                compArray[11 + offset],
                compArray[15 + offset]);
        }
    }

    public final double[] toArray(double[] compArray, int offset, boolean rowMajor)
    {
        if (compArray == null)
        {
            throw new IllegalArgumentException("Array Is Null");
        }
        if ((compArray.length - offset) < NUM_ELEMENTS)
        {
            throw new IllegalArgumentException("Array Invalid Length");
        }

        if (rowMajor)
        {
            // Row 1
            //noinspection PointlessArithmeticExpression
            compArray[0 + offset] = this.m11;
            compArray[1 + offset] = this.m12;
            compArray[2 + offset] = this.m13;
            compArray[3 + offset] = this.m14;
            // Row 2
            compArray[4 + offset] = this.m21;
            compArray[5 + offset] = this.m22;
            compArray[6 + offset] = this.m23;
            compArray[7 + offset] = this.m24;
            // Row 3
            compArray[8 + offset] = this.m31;
            compArray[9 + offset] = this.m32;
            compArray[10 + offset] = this.m33;
            compArray[11 + offset] = this.m34;
            // Row 4
            compArray[12 + offset] = this.m41;
            compArray[13 + offset] = this.m42;
            compArray[14 + offset] = this.m43;
            compArray[15 + offset] = this.m44;
        }
        else
        {
            // Row 1
            //noinspection PointlessArithmeticExpression
            compArray[0 + offset] = this.m11;
            compArray[4 + offset] = this.m12;
            compArray[8 + offset] = this.m13;
            compArray[12 + offset] = this.m14;
            // Row 2
            compArray[1 + offset] = this.m21;
            compArray[5 + offset] = this.m22;
            compArray[9 + offset] = this.m23;
            compArray[13 + offset] = this.m24;
            // Row 3
            compArray[2 + offset] = this.m31;
            compArray[6 + offset] = this.m32;
            compArray[10 + offset] = this.m33;
            compArray[14 + offset] = this.m34;
            // Row 4
            compArray[3 + offset] = this.m41;
            compArray[7 + offset] = this.m42;
            compArray[11 + offset] = this.m43;
            compArray[15 + offset] = this.m44;
        }

        return compArray;
    }

    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.m11).append(", ").append(this.m12).append(", ").append(this.m13).append(", ").append(this.m14);
        sb.append(", \r\n");
        sb.append(this.m21).append(", ").append(this.m22).append(", ").append(this.m23).append(", ").append(this.m24);
        sb.append(", \r\n");
        sb.append(this.m31).append(", ").append(this.m32).append(", ").append(this.m33).append(", ").append(this.m34);
        sb.append(", \r\n");
        sb.append(this.m41).append(", ").append(this.m42).append(", ").append(this.m43).append(", ").append(this.m44);
        sb.append(")");
        return sb.toString();
    }

    public final double getM11()
    {
        return this.m11;
    }

    public final double getM12()
    {
        return this.m12;
    }

    public final double getM13()
    {
        return this.m13;
    }

    public final double getM14()
    {
        return this.m14;
    }

    public final double getM21()
    {
        return this.m21;
    }

    public final double getM22()
    {
        return this.m22;
    }

    public final double getM23()
    {
        return this.m23;
    }

    public final double getM24()
    {
        return this.m24;
    }

    public final double getM31()
    {
        return this.m31;
    }

    public final double getM32()
    {
        return this.m32;
    }

    public final double getM33()
    {
        return this.m33;
    }

    public final double getM34()
    {
        return this.m34;
    }

    public final double getM41()
    {
        return this.m41;
    }

    public final double getM42()
    {
        return this.m42;
    }

    public final double getM43()
    {
        return this.m43;
    }

    public final double getM44()
    {
        return this.m44;
    }

    public final double m11()
    {
        return this.m11;
    }

    public final double m12()
    {
        return this.m12;
    }

    public final double m13()
    {
        return this.m13;
    }

    public final double m14()
    {
        return this.m14;
    }

    public final double m21()
    {
        return this.m21;
    }

    public final double m22()
    {
        return this.m22;
    }

    public final double m23()
    {
        return this.m23;
    }

    public final double m24()
    {
        return this.m24;
    }

    public final double m31()
    {
        return this.m31;
    }

    public final double m32()
    {
        return this.m32;
    }

    public final double m33()
    {
        return this.m33;
    }

    public final double m34()
    {
        return this.m34;
    }

    public final double m41()
    {
        return this.m41;
    }

    public final double m42()
    {
        return this.m42;
    }

    public final double m43()
    {
        return this.m43;
    }

    public final double m44()
    {
        return this.m44;
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    /**
     * Returns a Cartesian transform <code>Matrix</code> that maps a local orientation to model coordinates. The
     * orientation is specified by an array of three <code>axes</code>. The <code>axes</code> array must contain three
     * non-null vectors, which are interpreted in the following order: x-axis, y-axis, z-axis. This ensures that the
     * axes in the returned <code>Matrix</code> have unit length and are orthogonal to each other.
     *
     * @param axes an array must of three non-null vectors defining a local orientation in the following order: x-axis,
     *             y-axis, z-axis.
     *
     * @return a <code>Matrix</code> that a transforms local coordinates to world coordinates.
     *
     * @throws IllegalArgumentException if <code>axes</code> is <code>null</code>, if <code>axes</code> contains less
     *                                  than three elements, or if any of the first three elements in <code>axes</code>
     *                                  is <code>null</code>.
     */
    public static Matrix fromAxes(Vec4[] axes)
    {
        if (axes == null)
        {
            throw new IllegalArgumentException("Axes Is Null");
        }

        if (axes.length < 3)
        {
            throw new IllegalArgumentException("Array Invalid Length");
        }

        if (axes[0] == null || axes[1] == null || axes[2] == null)
        {
            throw new IllegalArgumentException("Axes Is Null");
        }

        Vec4 s = axes[0].normalize3();
        Vec4 f = s.cross3(axes[1]).normalize3();
        Vec4 u = f.cross3(s).normalize3();

        return new Matrix(
            s.x, u.x, f.x, 0.0,
            s.y, u.y, f.y, 0.0,
            s.z, u.z, f.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
            true);
    }

    public static Matrix fromAxisAngle(Angle angle, Vec4 axis)
    {
        if (angle == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }
        if (axis == null)
        {
            throw new IllegalArgumentException("Vec4 Is Null");
        }

        return fromAxisAngle(angle, axis.x, axis.y, axis.z, true);
    }

    public static Matrix fromAxisAngle(Angle angle, double axisX, double axisY, double axisZ)
    {
        if (angle == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }
        return fromAxisAngle(angle, axisX, axisY, axisZ, true);
    }

    private static Matrix fromAxisAngle(Angle angle, double axisX, double axisY, double axisZ, boolean normalize)
    {
        if (angle == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }

        if (normalize)
        {
            double length = Math.sqrt((axisX * axisX) + (axisY * axisY) + (axisZ * axisZ));
            if (!isZero(length) && (length != 1.0))
            {
                axisX /= length;
                axisY /= length;
                axisZ /= length;
            }
        }

        double c = angle.cos();
        double s = angle.sin();
        double one_minus_c = 1.0 - c;
        return new Matrix(
            // Row 1
            c + (one_minus_c * axisX * axisX),
            (one_minus_c * axisX * axisY) - (s * axisZ),
            (one_minus_c * axisX * axisZ) + (s * axisY),
            0.0,
            // Row 2
            (one_minus_c * axisX * axisY) + (s * axisZ),
            c + (one_minus_c * axisY * axisY),
            (one_minus_c * axisY * axisZ) - (s * axisX),
            0.0,
            // Row 3
            (one_minus_c * axisX * axisZ) - (s * axisY),
            (one_minus_c * axisY * axisZ) + (s * axisX),
            c + (one_minus_c * axisZ * axisZ),
            0.0,
            // Row 4
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromQuaternion(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            throw new IllegalArgumentException("Quaternion Is Null");
        }

        return fromQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w, true);
    }

    private static Matrix fromQuaternion(double x, double y, double z, double w, boolean normalize)
    {
        if (normalize)
        {
            double length = Math.sqrt((x * x) + (y * y) + (z * z) + (w * w));
            if (!isZero(length) && (length != 1.0))
            {
                x /= length;
                y /= length;
                z /= length;
                w /= length;
            }
        }

        return new Matrix(
            // Row 1
            1.0 - (2.0 * y * y) - (2.0 * z * z),
            (2.0 * x * y) - (2.0 * z * w),
            (2.0 * x * z) + (2.0 * y * w),
            0.0,
            // Row 2
            (2.0 * x * y) + (2.0 * z * w),
            1.0 - (2.0 * x * x) - (2.0 * z * z),
            (2.0 * y * z) - (2.0 * x * w),
            0.0,
            // Row 3
            (2.0 * x * z) - (2.0 * y * w),
            (2.0 * y * z) + (2.0 * x * w),
            1.0 - (2.0 * x * x) - (2.0 * y * y),
            0.0,
            // Row 4
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationXYZ(Angle xRotation, Angle yRotation, Angle zRotation)
    {
        if ((xRotation == null) || (yRotation == null) || (zRotation == null))
        {
            throw new IllegalArgumentException("Angle Is Null");
        }

        double cx = xRotation.cos();
        double cy = yRotation.cos();
        double cz = zRotation.cos();
        double sx = xRotation.sin();
        double sy = yRotation.sin();
        double sz = zRotation.sin();
        return new Matrix(
            cy * cz, -cy * sz, sy, 0.0,
            (sx * sy * cz) + (cx * sz), -(sx * sy * sz) + (cx * cz), -sx * cy, 0.0,
            -(cx * sy * cz) + (sx * sz), (cx * sy * sz) + (sx * cz), cx * cy, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationX(Angle angle)
    {
        if (angle == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }

        double c = angle.cos();
        double s = angle.sin();
        return new Matrix(
            1.0, 0.0, 0.0, 0.0,
            0.0, c, -s, 0.0,
            0.0, s, c, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationY(Angle angle)
    {
        if (angle == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }

        double c = angle.cos();
        double s = angle.sin();
        return new Matrix(
            c, 0.0, s, 0.0,
            0.0, 1.0, 0.0, 0.0,
            -s, 0.0, c, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationZ(Angle angle)
    {
        if (angle == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }

        double c = angle.cos();
        double s = angle.sin();
        return new Matrix(
            c, -s, 0.0, 0.0,
            s, c, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromScale(double scale)
    {
        return fromScale(scale, scale, scale);
    }

    public static Matrix fromScale(Vec4 scale)
    {
        if (scale == null)
        {
            throw new IllegalArgumentException("Vec4 Is Null");
        }

        return fromScale(scale.x, scale.y, scale.z);
    }

    public static Matrix fromScale(double scaleX, double scaleY, double scaleZ)
    {
        return new Matrix(
            scaleX, 0.0, 0.0, 0.0,
            0.0, scaleY, 0.0, 0.0,
            0.0, 0.0, scaleZ, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Scale matrices are non-orthogonal, 3D transforms.
            false);
    }

    public static Matrix fromTranslation(Vec4 translation)
    {
        if (translation == null)
        {
            throw new IllegalArgumentException("Vec4 Is Null");
        }

        return fromTranslation(translation.x, translation.y, translation.z);
    }

    public static Matrix fromTranslation(double x, double y, double z)
    {
        return new Matrix(
            1.0, 0.0, 0.0, x,
            0.0, 1.0, 0.0, y,
            0.0, 0.0, 1.0, z,
            0.0, 0.0, 0.0, 1.0,
            // Translation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromSkew(Angle theta, Angle phi)
    {
        // from http://faculty.juniata.edu/rhodes/graphics/projectionmat.htm

        double cotTheta = 1.0e6;
        double cotPhi = 1.0e6;

        if (theta.getRadians() < EPSILON && phi.getRadians() < EPSILON)
        {
            cotTheta = 0;
            cotPhi = 0;
        }
        else
        {
            if (Math.abs(Math.tan(theta.getRadians())) > EPSILON)
                cotTheta = 1 / Math.tan(theta.getRadians());
            if (Math.abs(Math.tan(phi.getRadians())) > EPSILON)
                cotPhi = 1 / Math.tan(phi.getRadians());
        }

        return new Matrix(
            1.0, 0.0, -cotTheta, 0,
            0.0, 1.0, -cotPhi, 0,
            0.0, 0.0, 1.0, 0,
            0.0, 0.0, 0.0, 1.0,
            false);
    }

    /**
     * Returns a Cartesian transform <code>Matrix</code> that maps a local origin and orientation to model coordinates.
     * The transform is specified by a local <code>origin</code> and an array of three <code>axes</code>. The
     * <code>axes</code> array must contain three non-null vectors, which are interpreted in the following order:
     * x-axis, y-axis, z-axis. This ensures that the axes in the returned <code>Matrix</code> have unit length and are
     * orthogonal to each other.
     *
     * @param origin the origin of the local coordinate system.
     * @param axes   an array must of three non-null vectors defining a local orientation in the following order:
     *               x-axis, y-axis, z-axis.
     *
     * @return a <code>Matrix</code> that transforms local coordinates to world coordinates.
     *
     * @throws IllegalArgumentException if <code>origin</code> is <code>null</code>, if <code>axes</code> is
     *                                  <code>null</code>, if <code>axes</code> contains less than three elements, or if
     *                                  any of the first three elements in <code>axes</code> is <code>null</code>.
     */
    public static Matrix fromLocalOrientation(Vec4 origin, Vec4[] axes)
    {
        if (origin == null)
        {
            throw new IllegalArgumentException("Origin Is Null");
        }

        if (axes == null)
        {
            throw new IllegalArgumentException("Axes Is Null");
        }

        if (axes.length < 3)
        {
            throw new IllegalArgumentException("Array Invalid Length");
        }

        if (axes[0] == null || axes[1] == null || axes[2] == null)
        {
            throw new IllegalArgumentException("Axes Is Null");
        }

        return fromTranslation(origin).multiply(fromAxes(axes));
    }

    /**
     * Returns a viewing matrix in model coordinates defined by the specified View eye point, reference point indicating
     * the center of the scene, and up vector. The eye point, center point, and up vector are in model coordinates. The
     * returned viewing matrix maps the reference center point to the negative Z axis, and the eye point to the origin,
     * and the up vector to the positive Y axis. When this matrix is used to define an OGL viewing transform along with
     * a typical projection matrix such as {@link #fromPerspective(Angle, double, double, double, double)} , this maps
     * the center of the scene to the center of the viewport, and maps the up vector to the viewoport's positive Y axis
     * (the up vector points up in the viewport). The eye point and reference center point must not be coincident, and
     * the up vector must not be parallel to the line of sight (the vector from the eye point to the reference center
     * point).
     *
     * @param eye    the eye point, in model coordinates.
     * @param center the scene's reference center point, in model coordinates.
     * @param up     the direction of the up vector, in model coordinates.
     *
     * @return a viewing matrix in model coordinates defined by the specified eye point, reference center point, and up
     *         vector.
     *
     * @throws IllegalArgumentException if any of the eye point, reference center point, or up vector are null, if the
     *                                  eye point and reference center point are coincident, or if the up vector and the
     *                                  line of sight are parallel.
     */
    public static Matrix fromViewLookAt(Vec4 eye, Vec4 center, Vec4 up)
    {
        if (eye == null || center == null || up == null)
        {
            throw new IllegalArgumentException("Vec4 Is Null");
        }

        if (eye.distanceTo3(center) <= EPSILON)
        {
            throw new IllegalArgumentException("Eye And Center Invalid");
        }

        Vec4 forward = center.subtract3(eye);
        Vec4 f = forward.normalize3();

        Vec4 s = f.cross3(up);
        s = s.normalize3();

        if (s.getLength3() <= EPSILON)
        {
            throw new IllegalArgumentException("Up And Line Of Sight Invalid");
        }

        Vec4 u = s.cross3(f);
        u = u.normalize3();

        Matrix mAxes = new Matrix(
            s.x, s.y, s.z, 0.0,
            u.x, u.y, u.z, 0.0,
            -f.x, -f.y, -f.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
            true);
        Matrix mEye = Matrix.fromTranslation(
            -eye.x, -eye.y, -eye.z);
        return mAxes.multiply(mEye);
    }

    /**
     * Returns a local origin transform matrix in model coordinates defined by the specified eye point, reference point
     * indicating the center of the local scene, and up vector. The eye point, center point, and up vector are in model
     * coordinates. The returned viewing matrix maps the the positive Z axis to the reference center point, the origin
     * to the eye point, and the positive Y axis to the up vector. The eye point and reference center point must not be
     * coincident, and the up vector must not be parallel to the line of sight (the vector from the eye point to the
     * reference center point).
     *
     * @param eye    the eye point, in model coordinates.
     * @param center the scene's reference center point, in model coordinates.
     * @param up     the direction of the up vector, in model coordinates.
     *
     * @return a viewing matrix in model coordinates defined by the specified eye point, reference center point, and up
     *         vector.
     *
     * @throws IllegalArgumentException if any of the eye point, reference center point, or up vector are null, if the
     *                                  eye point and reference center point are coincident, or if the up vector and the
     *                                  line of sight are parallel.
     */
    public static Matrix fromModelLookAt(Vec4 eye, Vec4 center, Vec4 up)
    {
        if (eye == null || center == null || up == null)
        {
            throw new IllegalArgumentException("Vec4 Is Null");
        }

        if (eye.distanceTo3(center) <= EPSILON)
        {
            throw new IllegalArgumentException("Eye And Center Invalid");
        }

        Vec4 forward = center.subtract3(eye);
        Vec4 f = forward.normalize3();

        Vec4 s = up.cross3(f);
        s = s.normalize3();

        if (s.getLength3() <= EPSILON)
        {
            throw new IllegalArgumentException("Up And Line Of Sight Invalid");
        }

        Vec4 u = f.cross3(s);
        u = u.normalize3();

        Matrix mAxes = new Matrix(
            s.x, u.x, f.x, 0.0,
            s.y, u.y, f.y, 0.0,
            s.z, u.z, f.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
            true);
        Matrix mEye = Matrix.fromTranslation(
            eye.x, eye.y, eye.z);
        return mEye.multiply(mAxes);
    }

    public static Matrix fromPerspective(Angle horizontalFieldOfView, double viewportWidth, double viewportHeight,
        double near, double far)
    {
        if (horizontalFieldOfView == null)
        {
            throw new IllegalArgumentException("Angle Is Null");
        }

        double fovX = horizontalFieldOfView.degrees;
        if (fovX <= 0.0 || fovX > 180.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (viewportWidth <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (viewportHeight <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (near <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (far <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (far <= near)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }

        double f = 1.0 / horizontalFieldOfView.tanHalfAngle();
        // We are using *horizontal* field-of-view here. This results in a different matrix than documented in sources
        // using vertical field-of-view.
        return new Matrix(
            f, 0.0, 0.0, 0.0,
            0.0, (f * viewportWidth) / viewportHeight, 0.0, 0.0,
            0.0, 0.0, -(far + near) / (far - near), -(2.0 * far * near) / (far - near),
            0.0, 0.0, -1.0, 0.0);
    }

    public static Matrix fromPerspective(double width, double height, double near, double far)
    {
        if (width <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (height <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (near <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (far <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (far <= near)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }

        return new Matrix(
            2.0 / width, 0.0, 0.0, 0.0,
            0.0, (2.0 * near) / height, 0.0, 0.0,
            0.0, 0.0, -(far + near) / (far - near), -(2.0 * far * near) / (far - near),
            0.0, 0.0, -1.0, 0.0);
    }

    public static Matrix fromOrthographic(double width, double height, double near, double far)
    {
        if (width <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (height <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (near <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (far <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (far <= near)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }

        return new Matrix(
            2.0 / width, 0.0, 0.0, 0.0,
            0.0, 2.0 / height, 0.0, 0.0,
            0.0, 0.0, -2.0 / (far - near), -(far + near) / (far - near),
            0.0, 0.0, 0.0, 1.0);
    }

    public static Matrix fromOrthographic2D(double width, double height)
    {
        if (width <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }
        if (height <= 0.0)
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }

        return new Matrix(
            2.0 / width, 0.0, 0.0, 0.0,
            0.0, 2.0 / height, 0.0, 0.0,
            0.0, 0.0, -1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);
    }

    /**
     * Computes a symmetric covariance Matrix from the x, y, z coordinates of the specified points Iterable. This
     * returns null if the points Iterable is empty, or if all of the points are null.
     * <p/>
     * The returned covariance matrix represents the correlation between each pair of x-, y-, and z-coordinates as
     * they're distributed about the point Iterable's arithmetic mean. Its layout is as follows:
     * <p/>
     * <code> C(x, x)  C(x, y)  C(x, z) <br/> C(x, y)  C(y, y)  C(y, z) <br/> C(x, z)  C(y, z)  C(z, z) </code>
     * <p/>
     * C(i, j) is the covariance of coordinates i and j, where i or j are a coordinate's dispersion about its mean
     * value. If any entry is zero, then there's no correlation between the two coordinates defining that entry. If the
     * returned matrix is diagonal, then all three coordinates are uncorrelated, and the specified point Iterable is
     * distributed evenly about its mean point.
     *
     * @param points the Iterable of points for which to compute a Covariance matrix.
     *
     * @return the covariance matrix for the iterable of 3D points.
     *
     * @throws IllegalArgumentException if the points Iterable is null.
     */
    public static Matrix fromCovarianceOfVertices(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            throw new IllegalArgumentException("Iterable Is Null");
        }

        Vec4 mean = Vec4.computeAveragePoint(points);
        if (mean == null)
            return null;

        int count = 0;
        double c11 = 0d;
        double c22 = 0d;
        double c33 = 0d;
        double c12 = 0d;
        double c13 = 0d;
        double c23 = 0d;

        for (Vec4 vec : points)
        {
            if (vec == null)
                continue;

            count++;
            c11 += (vec.x - mean.x) * (vec.x - mean.x);
            c22 += (vec.y - mean.y) * (vec.y - mean.y);
            c33 += (vec.z - mean.z) * (vec.z - mean.z);
            c12 += (vec.x - mean.x) * (vec.y - mean.y); // c12 = c21
            c13 += (vec.x - mean.x) * (vec.z - mean.z); // c13 = c31
            c23 += (vec.y - mean.y) * (vec.z - mean.z); // c23 = c32
        }

        if (count == 0)
            return null;

        return new Matrix(
            c11 / (double) count, c12 / (double) count, c13 / (double) count, 0d,
            c12 / (double) count, c22 / (double) count, c23 / (double) count, 0d,
            c13 / (double) count, c23 / (double) count, c33 / (double) count, 0d,
            0d, 0d, 0d, 0d);
    }

   

    /**
     * Computes the eigensystem of the specified symmetric Matrix's upper 3x3 matrix. If the Matrix's upper 3x3 matrix
     * is not symmetric, this throws an IllegalArgumentException. This writes the eigensystem parameters to the
     * specified arrays <code>outEigenValues</code> and <code>outEigenVectors</code>, placing the eigenvalues in the
     * entries of array <code>outEigenValues</code>, and the corresponding eigenvectors in the entires of array
     * <code>outEigenVectors</code>. These arrays must be non-null, and have length three or greater.
     *
     * @param matrix          the symmetric Matrix for which to compute an eigensystem.
     * @param outEigenvalues  the array which receives the three output eigenvalues.
     * @param outEigenvectors the array which receives the three output eigenvectors.
     *
     * @throws IllegalArgumentException if the Matrix is null or is not symmetric, if the output eigenvalue array is
     *                                  null or has length less than 3, or if the output eigenvector is null or has
     *                                  length less than 3.
     */
    public static void computeEigensystemFromSymmetricMatrix3(Matrix matrix, double[] outEigenvalues,
        Vec4[] outEigenvectors)
    {
        if (matrix == null)
        {
            throw new IllegalArgumentException("Matrix Is Null");
        }

        if (matrix.m12 != matrix.m21 || matrix.m13 != matrix.m31 || matrix.m23 != matrix.m32)
        {
            throw new IllegalArgumentException("Matrix Not Symmetric");
        }

        // Take from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition" by Eric Lengyel,
        // Listing 14.6 (pages 441-444).

        final double EPSILON = 1.0e-10;
        final int MAX_SWEEPS = 32;

        // Since the Matrix is symmetric, m12=m21, m13=m31, and m23=m32. Therefore we can ignore the values m21, m31,
        // and m32.
        double m11 = matrix.m11;
        double m12 = matrix.m12;
        double m13 = matrix.m13;
        double m22 = matrix.m22;
        double m23 = matrix.m23;
        double m33 = matrix.m33;

        double[][] r = new double[3][3];
        r[0][0] = r[1][1] = r[2][2] = 1d;

        for (int a = 0; a < MAX_SWEEPS; a++)
        {
            // Exit if off-diagonal entries small enough
            if ((Math.abs(m12) < EPSILON) && (Math.abs(m13) < EPSILON) && (Math.abs(m23) < EPSILON))
                break;

            // Annihilate (1,2) entry
            if (m12 != 0d)
            {
                double u = (m22 - m11) * 0.5 / m12;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m11 -= t * m12;
                m22 += t * m12;
                m12 = 0d;

                double temp = c * m13 - s * m23;
                m23 = s * m13 + c * m23;
                m13 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][0] - s * r[i][1];
                    r[i][1] = s * r[i][0] + c * r[i][1];
                    r[i][0] = temp;
                }
            }

            // Annihilate (1,3) entry
            if (m13 != 0d)
            {
                double u = (m33 - m11) * 0.5 / m13;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m11 -= t * m13;
                m33 += t * m13;
                m13 = 0d;

                double temp = c * m12 - s * m23;
                m23 = s * m12 + c * m23;
                m12 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][0] - s * r[i][2];
                    r[i][2] = s * r[i][0] + c * r[i][2];
                    r[i][0] = temp;
                }
            }

            // Annihilate (2,3) entry
            if (m23 != 0d)
            {
                double u = (m33 - m22) * 0.5 / m23;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m22 -= t * m23;
                m33 += t * m23;
                m23 = 0d;

                double temp = c * m12 - s * m13;
                m13 = s * m12 + c * m13;
                m12 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][1] - s * r[i][2];
                    r[i][2] = s * r[i][1] + c * r[i][2];
                    r[i][1] = temp;
                }
            }
        }

        outEigenvalues[0] = m11;
        outEigenvalues[1] = m22;
        outEigenvalues[2] = m33;

        outEigenvectors[0] = new Vec4(r[0][0], r[1][0], r[2][0]);
        outEigenvectors[1] = new Vec4(r[0][1], r[1][1], r[2][1]);
        outEigenvectors[2] = new Vec4(r[0][2], r[1][2], r[2][2]);
    }

    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //

    public final Matrix add(Matrix matrix)
    {
        if (matrix == null)
        {
            throw new IllegalArgumentException("Matrix Is Null");
        }

        return new Matrix(
            this.m11 + matrix.m11, this.m12 + matrix.m12, this.m13 + matrix.m13, this.m14 + matrix.m14,
            this.m21 + matrix.m21, this.m22 + matrix.m22, this.m23 + matrix.m23, this.m24 + matrix.m24,
            this.m31 + matrix.m31, this.m32 + matrix.m32, this.m33 + matrix.m33, this.m34 + matrix.m34,
            this.m41 + matrix.m41, this.m42 + matrix.m42, this.m43 + matrix.m43, this.m44 + matrix.m44);
    }

    public final Matrix subtract(Matrix matrix)
    {
        if (matrix == null)
        {
            throw new IllegalArgumentException("Matrix Is Null");
        }

        return new Matrix(
            this.m11 - matrix.m11, this.m12 - matrix.m12, this.m13 - matrix.m13, this.m14 - matrix.m14,
            this.m21 - matrix.m21, this.m22 - matrix.m22, this.m23 - matrix.m23, this.m24 - matrix.m24,
            this.m31 - matrix.m31, this.m32 - matrix.m32, this.m33 - matrix.m33, this.m34 - matrix.m34,
            this.m41 - matrix.m41, this.m42 - matrix.m42, this.m43 - matrix.m43, this.m44 - matrix.m44);
    }

    public final Matrix multiplyComponents(double value)
    {
        return new Matrix(
            this.m11 * value, this.m12 * value, this.m13 * value, this.m14 * value,
            this.m21 * value, this.m22 * value, this.m23 * value, this.m24 * value,
            this.m31 * value, this.m32 * value, this.m33 * value, this.m34 * value,
            this.m41 * value, this.m42 * value, this.m43 * value, this.m44 * value);
    }

    public final Matrix multiply(Matrix matrix)
    {
        if (matrix == null)
        {
            throw new IllegalArgumentException("Matrix Is Null");
        }

        return new Matrix(
            // Row 1
            (this.m11 * matrix.m11) + (this.m12 * matrix.m21) + (this.m13 * matrix.m31) + (this.m14 * matrix.m41),
            (this.m11 * matrix.m12) + (this.m12 * matrix.m22) + (this.m13 * matrix.m32) + (this.m14 * matrix.m42),
            (this.m11 * matrix.m13) + (this.m12 * matrix.m23) + (this.m13 * matrix.m33) + (this.m14 * matrix.m43),
            (this.m11 * matrix.m14) + (this.m12 * matrix.m24) + (this.m13 * matrix.m34) + (this.m14 * matrix.m44),
            // Row 2
            (this.m21 * matrix.m11) + (this.m22 * matrix.m21) + (this.m23 * matrix.m31) + (this.m24 * matrix.m41),
            (this.m21 * matrix.m12) + (this.m22 * matrix.m22) + (this.m23 * matrix.m32) + (this.m24 * matrix.m42),
            (this.m21 * matrix.m13) + (this.m22 * matrix.m23) + (this.m23 * matrix.m33) + (this.m24 * matrix.m43),
            (this.m21 * matrix.m14) + (this.m22 * matrix.m24) + (this.m23 * matrix.m34) + (this.m24 * matrix.m44),
            // Row 3
            (this.m31 * matrix.m11) + (this.m32 * matrix.m21) + (this.m33 * matrix.m31) + (this.m34 * matrix.m41),
            (this.m31 * matrix.m12) + (this.m32 * matrix.m22) + (this.m33 * matrix.m32) + (this.m34 * matrix.m42),
            (this.m31 * matrix.m13) + (this.m32 * matrix.m23) + (this.m33 * matrix.m33) + (this.m34 * matrix.m43),
            (this.m31 * matrix.m14) + (this.m32 * matrix.m24) + (this.m33 * matrix.m34) + (this.m34 * matrix.m44),
            // Row 4
            (this.m41 * matrix.m11) + (this.m42 * matrix.m21) + (this.m43 * matrix.m31) + (this.m44 * matrix.m41),
            (this.m41 * matrix.m12) + (this.m42 * matrix.m22) + (this.m43 * matrix.m32) + (this.m44 * matrix.m42),
            (this.m41 * matrix.m13) + (this.m42 * matrix.m23) + (this.m43 * matrix.m33) + (this.m44 * matrix.m43),
            (this.m41 * matrix.m14) + (this.m42 * matrix.m24) + (this.m43 * matrix.m34) + (this.m44 * matrix.m44),
            // Product of orthonormal 3D transformHACK matrices is also an orthonormal 3D transformHACK.
            this.isOrthonormalTransform && matrix.isOrthonormalTransform);
    }

    public final Matrix divideComponents(double value)
    {
        if (isZero(value))
        {
            throw new IllegalArgumentException("Argument Out Of Range");
        }

        return new Matrix(
            this.m11 / value, this.m12 / value, this.m13 / value, this.m14 / value,
            this.m21 / value, this.m22 / value, this.m23 / value, this.m24 / value,
            this.m31 / value, this.m32 / value, this.m33 / value, this.m34 / value,
            this.m41 / value, this.m42 / value, this.m43 / value, this.m44 / value);
    }

    public final Matrix divideComponents(Matrix matrix)
    {
        if (matrix == null)
        {
            throw new IllegalArgumentException("Matrix Is Null");
        }

        return new Matrix(
            this.m11 / matrix.m11, this.m12 / matrix.m12, this.m13 / matrix.m13, this.m14 / matrix.m14,
            this.m21 / matrix.m21, this.m22 / matrix.m22, this.m23 / matrix.m23, this.m24 / matrix.m24,
            this.m31 / matrix.m31, this.m32 / matrix.m32, this.m33 / matrix.m33, this.m34 / matrix.m34,
            this.m41 / matrix.m41, this.m42 / matrix.m42, this.m43 / matrix.m43, this.m44 / matrix.m44);
    }

    public final Matrix negate()
    {
        return new Matrix(
            0.0 - this.m11, 0.0 - this.m12, 0.0 - this.m13, 0.0 - this.m14,
            0.0 - this.m21, 0.0 - this.m22, 0.0 - this.m23, 0.0 - this.m24,
            0.0 - this.m31, 0.0 - this.m32, 0.0 - this.m33, 0.0 - this.m34,
            0.0 - this.m41, 0.0 - this.m42, 0.0 - this.m43, 0.0 - this.m44,
            // Negative of orthonormal 3D transformHACK matrix is also an orthonormal 3D transformHACK.
            this.isOrthonormalTransform);
    }

    public final Vec4 transformBy3(Matrix matrix, double x, double y, double z)
    {
        if (matrix == null)
        {
            throw new IllegalArgumentException("Matrix Is Null");
        }

        return new Vec4(
            (matrix.m11 * x) + (matrix.m12 * y) + (matrix.m13 * z),
            (matrix.m21 * x) + (matrix.m22 * y) + (matrix.m23 * z),
            (matrix.m31 * x) + (matrix.m32 * y) + (matrix.m33 * z));
    }

    // ============== Matrix Arithmetic Functions ======================= //
    // ============== Matrix Arithmetic Functions ======================= //
    // ============== Matrix Arithmetic Functions ======================= //

    public final double getDeterminant()
    {
        double result = 0.0;
        // Columns 2, 3, 4.
        result += this.m11 *
            (this.m22 * (this.m33 * this.m44 - this.m43 * this.m34)
                - this.m23 * (this.m32 * this.m44 - this.m42 * this.m34)
                + this.m24 * (this.m32 * this.m43 - this.m42 * this.m33));
        // Columns 1, 3, 4.
        result -= this.m12 *
            (this.m21 * (this.m33 * this.m44 - this.m43 * this.m34)
                - this.m23 * (this.m31 * this.m44 - this.m41 * this.m34)
                + this.m24 * (this.m31 * this.m43 - this.m41 * this.m33));
        // Columns 1, 2, 4.
        result += this.m13 *
            (this.m21 * (this.m32 * this.m44 - this.m42 * this.m34)
                - this.m22 * (this.m31 * this.m44 - this.m41 * this.m34)
                + this.m24 * (this.m31 * this.m42 - this.m41 * this.m32));
        // Columns 1, 2, 3.
        result -= this.m14 *
            (this.m21 * (this.m32 * this.m43 - this.m42 - this.m33)
                - this.m22 * (this.m31 * this.m43 - this.m41 * this.m33)
                + this.m23 * (this.m31 * this.m42 - this.m41 * this.m32));
        return result;
    }

    public final Matrix getTranspose()
    {
        // Swap rows with columns.
        return new Matrix(
            this.m11, this.m21, this.m31, this.m41,
            this.m12, this.m22, this.m32, this.m42,
            this.m13, this.m23, this.m33, this.m43,
            this.m14, this.m24, this.m34, this.m44,
            // Transpose of orthonormal 3D transformHACK matrix is not an orthonormal 3D transformHACK matrix.
            false);
    }

    public final double getTrace()
    {
        return this.m11 + this.m22 + this.m33 + this.m44;
    }

    public final Matrix getInverse()
    {
        if (this.isOrthonormalTransform)
            return computeTransformInverse(this);
        else
            return computeGeneralInverse(this);
    }

    private static Matrix computeGeneralInverse(Matrix a)
    {
        double cf_11 = a.m22 * (a.m33 * a.m44 - a.m43 * a.m34)
            - a.m23 * (a.m32 * a.m44 - a.m42 * a.m34)
            + a.m24 * (a.m32 * a.m43 - a.m42 * a.m33);
        double cf_12 = -(a.m21 * (a.m33 * a.m44 - a.m43 * a.m34)
            - a.m23 * (a.m31 * a.m44 - a.m41 * a.m34)
            + a.m24 * (a.m31 * a.m43 - a.m41 * a.m33));
        double cf_13 = a.m21 * (a.m32 * a.m44 - a.m42 * a.m34)
            - a.m22 * (a.m31 * a.m44 - a.m41 * a.m34)
            + a.m24 * (a.m31 * a.m42 - a.m41 * a.m32);
        double cf_14 = -(a.m21 * (a.m32 * a.m43 - a.m42 - a.m33)
            - a.m22 * (a.m31 * a.m43 - a.m41 * a.m33)
            + a.m23 * (a.m31 * a.m42 - a.m41 * a.m32));
        double cf_21 = a.m12 * (a.m33 * a.m44 - a.m43 - a.m34)
            - a.m13 * (a.m32 * a.m44 - a.m42 * a.m34)
            + a.m14 * (a.m32 * a.m43 - a.m42 * a.m33);
        double cf_22 = -(a.m11 * (a.m33 * a.m44 - a.m43 * a.m34)
            - a.m13 * (a.m31 * a.m44 - a.m41 * a.m34)
            + a.m14 * (a.m31 * a.m43 - a.m41 * a.m33));
        double cf_23 = a.m11 * (a.m32 * a.m44 - a.m42 * a.m34)
            - a.m12 * (a.m31 * a.m44 - a.m41 * a.m34)
            + a.m14 * (a.m31 * a.m42 - a.m41 * a.m32);
        double cf_24 = -(a.m11 * (a.m32 * a.m43 - a.m42 * a.m33)
            - a.m12 * (a.m31 * a.m43 - a.m41 * a.m33)
            + a.m13 * (a.m31 * a.m42 - a.m41 * a.m32));
        double cf_31 = a.m12 * (a.m23 * a.m44 - a.m43 * a.m24)
            - a.m13 * (a.m22 * a.m44 - a.m42 * a.m24)
            + a.m14 * (a.m22 * a.m43 - a.m42 * a.m23);
        double cf_32 = -(a.m11 * (a.m23 * a.m44 - a.m43 * a.m24)
            - a.m13 * (a.m21 * a.m44 - a.m41 * a.m24)
            + a.m14 * (a.m24 * a.m43 - a.m41 * a.m23));
        double cf_33 = a.m11 * (a.m22 * a.m44 - a.m42 * a.m24)
            - a.m12 * (a.m21 * a.m44 - a.m41 * a.m24)
            + a.m14 * (a.m21 * a.m42 - a.m41 * a.m22);
        double cf_34 = -(a.m11 * (a.m22 * a.m33 - a.m32 * a.m23)
            - a.m12 * (a.m21 * a.m33 - a.m31 * a.m23)
            + a.m13 * (a.m21 * a.m32 - a.m31 * a.m22));
        double cf_41 = a.m12 * (a.m23 * a.m34 - a.m33 * a.m24)
            - a.m13 * (a.m22 * a.m34 - a.m32 * a.m24)
            + a.m14 * (a.m22 * a.m33 - a.m32 * a.m23);
        double cf_42 = -(a.m11 * (a.m23 * a.m34 - a.m33 * a.m24)
            - a.m13 * (a.m21 * a.m34 - a.m31 * a.m24)
            + a.m14 * (a.m21 * a.m33 - a.m31 * a.m23));
        double cf_43 = a.m11 * (a.m22 * a.m34 - a.m32 * a.m24)
            - a.m12 * (a.m21 * a.m34 - a.m31 * a.m24)
            + a.m14 * (a.m21 * a.m32 - a.m31 * a.m22);
        double cf_44 = -(a.m11 * (a.m22 * a.m33 - a.m32 * a.m23)
            - a.m12 * (a.m21 * a.m33 - a.m31 * a.m23)
            + a.m13 * (a.m21 * a.m32 - a.m31 * a.m22));
        double det = (a.m11 * cf_11) + (a.m12 * cf_12) + (a.m13 * cf_13) + (a.m14 * cf_14);

        if (isZero(det))
            return null;
        return new Matrix(
            cf_11 / det, cf_21 / det, cf_31 / det, cf_41 / det,
            cf_12 / det, cf_22 / det, cf_32 / det, cf_42 / det,
            cf_13 / det, cf_23 / det, cf_33 / det, cf_43 / det,
            cf_14 / det, cf_24 / det, cf_34 / det, cf_44 / det);
    }

    private static Matrix computeTransformInverse(Matrix a)
    {
        // 'a' is assumed to contain a 3D transformation matrix.
        // Upper-3x3 is inverted, translation is transformed by inverted-upper-3x3 and negated.
        return new Matrix(
            a.m11, a.m21, a.m31, 0.0 - (a.m11 * a.m14) - (a.m21 * a.m24) - (a.m31 * a.m34),
            a.m12, a.m22, a.m32, 0.0 - (a.m12 * a.m14) - (a.m22 * a.m24) - (a.m32 * a.m34),
            a.m13, a.m23, a.m33, 0.0 - (a.m13 * a.m14) - (a.m23 * a.m24) - (a.m33 * a.m34),
            0.0, 0.0, 0.0, 1.0,
            // Inverse of an orthogonal, 3D transformHACK matrix is not an orthogonal 3D transformHACK.
            false);
    }

    // ============== Accessor Functions ======================= //
    // ============== Accessor Functions ======================= //
    // ============== Accessor Functions ======================= //

    public final Angle getRotationX()
    {
        double yRadians = Math.asin(this.m13);
        double cosY = Math.cos(yRadians);
        if (isZero(cosY))
            return null;

        double xRadians;
        // No Gimball lock.
        if (Math.abs(cosY) > 0.005)
        {
            xRadians = Math.atan2(-this.m23 / cosY, this.m33 / cosY);
        }
        // Gimball lock has occurred. Rotation around X axis becomes rotation around Z axis.
        else
        {
            xRadians = 0;
        }

        if (Double.isNaN(xRadians))
            return null;

        return Angle.fromRadians(xRadians);
    }

    public final Angle getRotationY()
    {
        double yRadians = Math.asin(this.m13);
        if (Double.isNaN(yRadians))
            return null;

        return Angle.fromRadians(yRadians);
    }

    public final Angle getRotationZ()
    {
        double yRadians = Math.asin(this.m13);
        double cosY = Math.cos(yRadians);
        if (isZero(cosY))
            return null;

        double zRadians;
        // No Gimball lock.
        if (Math.abs(cosY) > 0.005)
        {
            zRadians = Math.atan2(-this.m12 / cosY, this.m11 / cosY);
        }
        // Gimball lock has occurred. Rotation around X axis becomes rotation around Z axis.
        else
        {
            zRadians = Math.atan2(this.m21, this.m22);
        }

        if (Double.isNaN(zRadians))
            return null;

        return Angle.fromRadians(zRadians);
    }


    public final Vec4 getTranslation()
    {
        return new Vec4(this.m14, this.m24, this.m34);
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    private static final Double POSITIVE_ZERO = +0.0d;

    private static final Double NEGATIVE_ZERO = -0.0d;

    private static boolean isZero(double value)
    {
        return (POSITIVE_ZERO.compareTo(value) == 0)
            || (NEGATIVE_ZERO.compareTo(value) == 0);
    }
}
