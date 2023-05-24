package scala.objects

import scala.base.{Color, Ray, Vector3D}
import scala.math.{abs, sqrt, min, max, pow}
import scala.compiletime.ops.double
import scala.base.randomDouble
import scala.base.Color
import scala.base.Ray
import scala.base.Vector3D
import scala.base.Vector3D.randomInUnitSphere

case class ScatterRecord(attenuation: Color, scattered: Ray)

trait Material {
  def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord]
}

class Lambertian(val albedo: Color) extends Material {
  override def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord] =
    val scatterDirection: Vector3D = record.normal + Vector3D.randomUnitVector()
    val newSD: Vector3D =
      if scatterDirection.nearZero then record.normal else scatterDirection
    return Some(ScatterRecord(albedo, Ray(record.point, scatterDirection)))
}

class Dielectric(val indexOfRefraction: Double) extends Material {
  override def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord] = {
    val attenuation = Color(1.0, 1.0, 1.0)
    val refractionRatio =
      if record.frontFace then 1.0 / indexOfRefraction else indexOfRefraction

    val unitDirection: Vector3D = rayIn.direction.unitVector

    val cosTheta = min(-unitDirection o record.normal, 1.0)
    val sinTheta = sqrt(1.0 - cosTheta * cosTheta)

    val cantRefract = refractionRatio * sinTheta > 1.0
    val direction: Vector3D =
      if cantRefract || this.reflectance(
          cosTheta,
          refractionRatio
        ) > randomDouble(0, 1)
      then unitDirection.reflect(record.normal)
      else unitDirection.refract(record.normal, refractionRatio)

    val scattered = Ray(record.point, direction)

    return Some(ScatterRecord(attenuation, scattered))
  }

  def reflectance(cos: Double, refIdx: Double) = {
    val r0 = pow((1 - refIdx) / (1 + refIdx), 2)
    r0 + (1 - r0) * pow(1 - cos, 5)
  }
}

class Metal(val albedo: Color, fuzziness: Double) extends Material {
  val fuzz: Double = if fuzziness < 1 then fuzziness else 1
  override def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord] =
    val reflected: Vector3D = (rayIn.direction.unitVector) reflect (record.normal)
    val scattered: Ray = Ray(record.point, reflected + randomInUnitSphere() * fuzz)
    if (scattered.direction dot record.normal) > 0 then Some(ScatterRecord(albedo, scattered))
    else None

}
