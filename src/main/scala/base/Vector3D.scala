package scala.base

import scala.math.sqrt
import scala.math.pow

final case class Vector3D(val x: Double, val y: Double, val z: Double) {
    lazy val sum: Double = this.x + this.y + this.z
    lazy val length_squared: Double =  (this ** 2).sum
    lazy val length: Double = sqrt( length_squared )
    lazy val unitVector: Vector3D = this / this.length

    def +(other: Vector3D): Vector3D = Vector3D(this.x + other.x, this.y + other.y, this.z + other.z)
    def -(other: Vector3D): Vector3D = Vector3D(this.x - other.x, this.y - other.y, this.z - other.z)
    def *(scalar: Double): Vector3D = this map (_ * scalar)
    def /(scalar: Double): Vector3D = this map (_ / scalar)
    def **(scalar: Double): Vector3D = this map (pow(_, scalar))

    // dot product
    def dot(other: Vector3D): Double = 
        this.x * other.x + this.y * other.y + this.z * other.z
    
    def o(other: Vector3D): Double = this dot other

    //cross product
    def cross(other: Vector3D): Vector3D =
        Vector3D(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        )

    def x(other: Vector3D): Vector3D = this cross other


    def map(foo: Double => Double) = 
        Vector3D(foo(this.x), foo(this.y), foo(this.z))

    val toStringRGB: String = {
        val newX: Int = (255.999 * this.x.toDouble).toInt
        val newY: Int = (255.999 * this.y.toDouble).toInt
        val newZ: Int = (255.999 * this.z.toDouble).toInt
        s"$newX $newY $newZ\n"
    }
}

type Point3D = Vector3D
type Color = Vector3D

object Point3D {
    def apply(x: Double, y: Double, z: Double) = Vector3D(x, y, z)
}

object Color {
    def apply(r: Double, g: Double, b: Double) = Vector3D(r, g, b)
}