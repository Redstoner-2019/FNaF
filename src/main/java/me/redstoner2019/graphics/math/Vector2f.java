package me.redstoner2019.graphics.math;

import java.nio.FloatBuffer;

/**
 * This class represents a (x,y)-Vector. GLSL equivalent to vec2.
 *
 * @author Heiko Brumme
 */
public class Vector2f {

    public float x;
    public float y;

    /**
     * Creates a default 2-tuple vector with all values set to 0.
     */
    public Vector2f() {
        this.x = 0f;
        this.y = 0f;
    }

    /**
     * Creates a 2-tuple vector with specified values.
     *
     * @param x x value
     * @param y y value
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates the squared length of the vector.
     *
     * @return Squared length of this vector
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Calculates the length of the vector.
     *
     * @return Length of this vector
     */
    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes the vector.
     *
     * @return Normalized vector
     */
    public Vector2f normalize() {
        float length = length();
        return divide(length);
    }

    /**
     * Adds this vector to another vector.
     *
     * @param other The other vector
     *
     * @return Sum of this + other
     */
    public Vector2f add(Vector2f other) {
        float x = this.x + other.x;
        float y = this.y + other.y;
        return new Vector2f(x, y);
    }

    /**
     * Negates this vector.
     *
     * @return Negated vector
     */
    public Vector2f negate() {
        return scale(-1f);
    }

    /**
     * Subtracts this vector from another vector.
     *
     * @param other The other vector
     *
     * @return Difference of this - other
     */
    public Vector2f subtract(Vector2f other) {
        return this.add(other.negate());
    }

    /**
     * Multiplies a vector by a scalar.
     *
     * @param scalar Scalar to multiply
     *
     * @return Scalar product of this * scalar
     */
    public Vector2f scale(float scalar) {
        float x = this.x * scalar;
        float y = this.y * scalar;
        return new Vector2f(x, y);
    }

    /**
     * Divides a vector by a scalar.
     *
     * @param scalar Scalar to multiply
     *
     * @return Scalar quotient of this / scalar
     */
    public Vector2f divide(float scalar) {
        return scale(1f / scalar);
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other The other vector
     *
     * @return Dot product of this * other
     */
    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Calculates a linear interpolation between this vector with another
     * vector.
     *
     * @param other The other vector
     * @param alpha The alpha value, must be between 0.0 and 1.0
     *
     * @return Linear interpolated vector
     */
    public Vector2f lerp(Vector2f other, float alpha) {
        return this.scale(1f - alpha).add(other.scale(alpha));
    }

    /**
     * Stores the vector in a given Buffer.
     *
     * @param buffer The buffer to store the vector data
     */
    public void toBuffer(FloatBuffer buffer) {
        buffer.put(x).put(y);
        buffer.flip();
    }

}
