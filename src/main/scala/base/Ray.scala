package scala.base

// import akka.actor._
import scala.objects._

class Ray (
    val origin: Point3D,
    val direction: Vector3D
) {
    def at(time: Double): Vector3D = origin + direction * time

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
                    val (clr: Color, pwr: Int) = Ray(point, target - point).rayColorTail(world, depth - 1, 1)
                    clr * (math.pow(0.5, pwr))

    @scala.annotation.tailrec
    private final def rayColorTail(world: HittableList, depth: Int, power: Int): (Color, Int) = 
        if depth <= 0 then (Color(0, 0, 0), power)
        else
            world hitBy(this, 0.001, infinity) match
                case Missed => 
                    val unitDirection: Vector3D = this.direction.unitVector
                    val t: Double = 0.5 * (unitDirection.y + 1.0)
                    val clr = Color(1.0, 1.0, 1.0) * (1.0 - t) + Color(0.5, 0.7, 1.0) * t
                    (clr, power)
                case HitRecord(point, frontFace, normal, t) => 
                    val target: Point3D = point + normal + Vector3D.randomInUnitSphere()
                    Ray(point, target - point).rayColorTail(world, depth - 1, power + 1)

}