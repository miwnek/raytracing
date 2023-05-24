package scala.objects

import scala.base.Color
import scala.base.Ray
import scala.base.Vector3D

case class ScatterRecord(attenuation: Color, scattered: Ray)

trait Material {
    def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord]
}

class Lambertian(val albedo: Color) extends Material {
    override def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord] =
        val scatterDirection: Vector3D = record.normal + Vector3D.randomUnitVector()
        val newSD: Vector3D = if scatterDirection.nearZero then record.normal else scatterDirection
        return Some( ScatterRecord(
            albedo,
            Ray(record.point, scatterDirection))
        )
}

class Metal(val albedo: Color) extends Material {
    override def scatter(rayIn: Ray, record: HitRecord): Option[ScatterRecord] =
        val reflected: Vector3D = (rayIn.direction.unitVector) reflect (record.normal)
        val scattered: Ray = Ray(record.point, reflected)
        if (scattered.direction dot record.normal) > 0 then
            Some( ScatterRecord( albedo, scattered))
        else
            None
        
}