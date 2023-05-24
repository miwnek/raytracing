package scala.base

// import akka.actor._
import scala.objects._
import scala.objects.HitRecord
import scala.objects.ScatterRecord

class Ray (
    val origin: Point3D,
    val direction: Vector3D
) {
    def at(time: Double): Vector3D = origin + direction * time

    def rayColor(world: HittableList, depth: Int): Color = 
        if depth <= 0 then Color(0, 0, 0)
        else
            val result: HitResult = world hitBy(this, 0.001, infinity) 
            result match
                case Missed => 
                    val unitDirection: Vector3D = this.direction.unitVector
                    val t: Double = 0.5 * (unitDirection.y + 1.0)
                    val clr = Color(1.0, 1.0, 1.0) * (1.0 - t) + Color(0.5, 0.7, 1.0) * t
                    clr
                case HitRecord(point, frontFace, normal, t, material) => 
                    material.scatter(this, result.asInstanceOf[HitRecord]) match
                        case Some(ScatterRecord(attenuation, scattered)) =>
                            val (clr, att): (Color, Color) = scattered.rayColorTail(world, depth - 1, attenuation)
                            clr * att
                        case None =>
                            Color(0, 0, 0)

    @scala.annotation.tailrec
    private final def rayColorTail(world: HittableList, depth: Int, acc: Color): (Color, Color) = 
        if depth <= 0 then (Color(0, 0, 0), acc)
        else
            val result: HitResult = world hitBy(this, 0.001, infinity) 
            result match
                case Missed => 
                    val unitDirection: Vector3D = this.direction.unitVector
                    val t: Double = 0.5 * (unitDirection.y + 1.0)
                    val clr = Color(1.0, 1.0, 1.0) * (1.0 - t) + Color(0.5, 0.7, 1.0) * t
                    (clr, acc)
                case HitRecord(point, _, normal, t, material) => 
                    material.scatter(this, result.asInstanceOf[HitRecord]) match
                        case Some(ScatterRecord(attenuation, scattered)) =>
                            scattered.rayColorTail(world, depth - 1, acc * attenuation)
                        case None => 
                            (Color(0, 0, 0), acc)
}