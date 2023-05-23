package scala.objects

import scala.base.Color
import scala.base.Ray
import scala.base.Vector3D

trait Material {
    def scatter(rayIn: Ray, record: HitRecord, attenuation: Color, scattered: Ray): Option[Color]
}

class Lambertian(val albedo: Color) extends Material {
    override def scatter(rayIn: Ray, record: HitRecord, attenuation: Color, scattered: Ray): Option[Color] =
        val scatterDirection: Vector3D = record.normal + Vector3D.randomUnitVector()
        val scattered: Ray = Ray(record.point, scatterDirection)
        return Some(this.albedo)

}