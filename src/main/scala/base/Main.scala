package scala.base

// import akka.actor._
import scala.objects._

val infinity: Double = Double.MaxValue
val pi: Double = math.Pi
val degreesToRadians: Double => Double = deg => deg  * pi / 180.0

@main def run(args: String*): Unit = {

  // Just a sample, doesn't really make sense.
  // Everything here and in the Scene.scala file is temporary
  val aspect_ratio: Double = Renderer.aspectRatio
  val width: Int = 400
  val height: Int = (width.toDouble / aspect_ratio).toInt

  val world: HittableList = HittableList()
    .add(Sphere( Point3D(0, 0, -1), 0.5 ))
    .add(Sphere( Point3D(0, -100.5, -1), 100 ))

  val raysIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield {
    val u: Double = i.toDouble / (width - 1)
    val v: Double = j.toDouble / (height - 1)
    Ray(Renderer.origin, Renderer.lowerLeftCorner + Renderer.horizontal * u + Renderer.vertical * v - Renderer.origin)
  }

  val rays: List[Ray] = raysIterable.toList

  val renderer = Renderer(rays, world, width, height)

  renderer.writeScene()
}

