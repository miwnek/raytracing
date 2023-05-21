package scala.base

// import akka.actor._
import scala.objects._

class Ray (
    val origin: Point3D,
    val direction: Vector3D
) {
    def at(time: Double): Vector3D = origin + direction * time
    def rayColor(world: HittableList): Color = 
        world hitBy(this, 0, infinity) match
            case Missed => 
                val unitDirection: Vector3D = this.direction.unitVector
                val t: Double = 0.5 * (unitDirection.y + 1.0)
                val clr = Color(1.0, 1.0, 1.0) * (1.0 - t) + Color(0.5, 0.7, 1.0) * t
                clr * 255.999
            case HitRecord(point, frontFace, normal, t) => 
                ((normal + Color(1, 1, 1)) * 0.5) * 255.999

    def rayColorString(world: HittableList): String = 
        val color: Color = this.rayColor(world)
        s"${color.x.toInt} ${color.y.toInt} ${color.z.toInt}\n"
}


// TODO: 
// class RayActor extends Actor with Ray