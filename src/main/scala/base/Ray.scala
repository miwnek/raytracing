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
                clr
            case HitRecord(point, frontFace, normal, t) => 
                ((normal + Color(1, 1, 1)) * 0.5)

}


// TODO: 
// Ray -> trait
// class RayActor extends Actor with Ray