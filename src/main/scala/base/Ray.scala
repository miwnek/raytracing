package scala.base

// import akka.actor._
import scala.objects._

class Ray (
    val origin: Point3D,
    val direction: Vector3D
) {
    def at(time: Double): Vector3D = origin + direction * time

    // TODO: try to replace with tail recursion (* 0.5 -> acc= 0.5 ?)
    def rayColor(world: HittableList, depth: Int): Color = 
        if depth <= 0 then Color(0, 0, 0)
        else
            world hitBy(this, 0.001, infinity) match
                case Missed => 
                    val unitDirection: Vector3D = this.direction.unitVector
                    val t: Double = 0.5 * (unitDirection.y + 1.0)
                    val clr = Color(1.0, 1.0, 1.0) * (1.0 - t) + Color(0.5, 0.7, 1.0) * t
                    clr
                case HitRecord(point, frontFace, normal, t) => 
                    val target: Point3D = point + normal + Vector3D.randomInUnitSphere()
                    Ray(point, target - point).rayColor(world, depth - 1) * 0.5

}


// TODO: 
// Ray -> trait
// class RayActor extends Actor with Ray