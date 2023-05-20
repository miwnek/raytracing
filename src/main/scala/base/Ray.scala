package scala.base

// import akka.actor._

class Ray (
    val origin: Point3D,
    val direction: Vector3D
) {
    def at(time: Double) = origin + direction * time
    def rayColor(): Color =
        val unit_direction: Vector3D = direction.unitVector
        val t: Double = 0.5 * (unit_direction.y + 1.0)
        val clr = Color(1.0, 1.0, 1.0) * (1.0 - t) + Color(0.5, 0.7, 1.0) * t
        clr * 255.999

    def rayColorString(): String = 
        val color: Color = this.rayColor()
        s"${color.x.toInt} ${color.y.toInt} ${color.z.toInt}\n"
}

// TODO: 
// class RayActor extends Actor with Ray