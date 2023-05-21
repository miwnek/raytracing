package scala.base

// import akka.actor._
import scala.objects._

@main def run(args: String*): Unit = {

  // Just a sample, doesn't really make sense.
  // Everything here and in the Camera.scala file is temporary
  val aspect_ratio: Double = Camera.aspectRatio
  val width: Int = 400
  val height: Int = (width.toDouble / aspect_ratio).toInt

  val world: HittableList = HittableList()
    .add(Sphere( Point3D(0, 0, -1), 0.5 ))
    .add(Sphere( Point3D(0, -100.5, -1), 100 ))

  val camera = Camera(world, width)

  val colorsIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield camera.getSampledColor(i, j)

  val colors: List[Color] = colorsIterable.toList

  camera.writeScene(colors)
}

