package scala.base

import scala.math.{sqrt, pow, min, max, abs}
import java.beans.VetoableChangeSupport

final case class Vector3D(val x: Double, val y: Double, val z: Double) {
  lazy val sum: Double = this.x + this.y + this.z
  lazy val length_squared: Double = (this ** 2).sum
  lazy val length: Double = sqrt(length_squared)
  lazy val unitVector: Vector3D = this / this.length
  lazy val nearZero: Boolean = (math.abs(x) < Vector3D.epsilon)
    && (math.abs(x) < Vector3D.epsilon)
    && (math.abs(x) < Vector3D.epsilon)

  def +(other: Vector3D): Vector3D =
    Vector3D(this.x + other.x, this.y + other.y, this.z + other.z)
  def -(other: Vector3D): Vector3D =
    Vector3D(this.x - other.x, this.y - other.y, this.z - other.z)
  def *(scalar: Double): Vector3D = this map (_ * scalar)
  def *(other: Vector3D): Vector3D =
    Vector3D(this.x * other.x, this.y * other.y, this.z * other.z)
  def /(scalar: Double): Vector3D = this map (_ / scalar)
  def **(scalar: Double): Vector3D = this map (pow(_, scalar))
  def unary_- : Vector3D = Vector3D(-this.x, -this.y, -this.z)

  // dot product
  def dot(other: Vector3D): Double =
    this.x * other.x + this.y * other.y + this.z * other.z

  def o(other: Vector3D): Double = this dot other

  // cross product
  def cross(other: Vector3D): Vector3D =
    Vector3D(
      this.y * other.z - this.z * other.y,
      this.z * other.x - this.x * other.z,
      this.x * other.y - this.y * other.x
    )

  def x(other: Vector3D): Vector3D = this cross other

  def reflect(other: Vector3D): Vector3D =
    this - ((other * (this dot other)) * 2)

  def refract(other: Vector3D, refractionRatio: Double): Vector3D = {
    val cosTheta = min(-this o other, 1.0)
    val rayOutPerp = (this + other * cosTheta) * refractionRatio
    val rayOutParl = other * (-sqrt(abs(1.0 - rayOutPerp.length_squared)))

    return rayOutParl + rayOutPerp
  }

  def map(foo: Double => Double) =
    Vector3D(foo(this.x), foo(this.y), foo(this.z))

  def color256(samplesPerPixel: Int): (Int, Int, Int) =
    val scale: Double = 1.0 / samplesPerPixel
    val color: Color = (this * scale)
      .map(math.sqrt(_))
      .map(clamp(_, 0.0, 0.999))
    val (r, g, b) = (color.x, color.y, color.z)
    ((256 * r).toInt, (256 * g).toInt, (256 * b).toInt)
}
object Vector3D {
  private val epsilon: Double = 1e-8
  def random() = Vector3D(randomFraction(), randomFraction(), randomFraction())
  def random(min: Double, max: Double) = Vector3D(
    randomDouble(min, max),
    randomDouble(min, max),
    randomDouble(min, max)
  )
  def randomInUnitSphere(): Vector3D =
    @scala.annotation.tailrec
    def helper(p: Vector3D): Vector3D =
      if p.length_squared < 1 then p
      else helper(Vector3D.random(-1, 1))
    helper(Vector3D.random(-1, 1))

  def randomUnitVector(): Vector3D =
    randomInUnitSphere().unitVector

  def randomInHemisphere(normal: Vector3D): Vector3D =
    val inUnitSphere: Vector3D = Vector3D.randomInUnitSphere()
    if (inUnitSphere o normal) > 0.0 then inUnitSphere
    else -inUnitSphere

  def randomInUnitDisk(): Vector3D = 
    @scala.annotation.tailrec
    def helper(p: Vector3D): Vector3D =
      if p.length_squared < 1 then p
      else helper(Vector3D(randomDouble(-1, 1), randomDouble(-1, 1), 0))
    helper(Vector3D(randomDouble(-1, 1), randomDouble(-1, 1), 0))
}

type Point3D = Vector3D
type Color = Vector3D

object Point3D {
  def apply(x: Double, y: Double, z: Double) = Vector3D(x, y, z)
}

object Color {
  def apply(r: Double, g: Double, b: Double) = Vector3D(r, g, b)
}
