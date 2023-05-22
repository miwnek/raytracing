package scala.base

import akka.actor.{Actor, ActorSystem, Props}

import scala.objects.HittableList
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import scala.objects.Sphere
import scala.base.{Camera, Color, Point3D}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

case class ExecAll()
case class ExecFoo(f: (Int, Int) => Color, x: Int, y: Int)
case class Return(index: Int, result: Color)

class RayActor extends Actor {
  override def receive: Actor.Receive = { case ExecFoo(f, x, y) =>
    val result = f(x, y)
    val index = self.path.name.toInt
    sender() ! Return(index, result)
  }
}

class CameraActor(world: HittableList, width: Int, height: Int) extends Actor {

  val camera: Camera = Camera(world, width)
  val rayActors = (0 until width * height)
    .map(index => context.actorOf(Props(RayActor()), index.toString))

  var pixelColors: Array[Array[Color]] = Array.ofDim(height, width)
  var receivedRes: Int = 0
  val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  // Temporary timing
  var timeS: Long = 0
  var timeE: Long = 0

  override def receive: Actor.Receive = {
    case ExecAll =>
      timeS = System.nanoTime()
      println("Camera active. Sending rays...")

      for (x <- 0 until width; y <- 0 until height)
        rayActors(y * width + x) ! ExecFoo(camera.getSampledColor, x, y)

    case Return(index, result) =>
      receivedRes += 1

      val (x, y) = (index / width, index % width)
      val Color(r, g, b) = result / camera.samplesPerPixel
      var rgb: Int = (r * 255).toInt
      rgb = (rgb << 8) + (g * 255).toInt
      rgb = (rgb << 8) + (b * 255).toInt
      image.setRGB(y, height - 1 - x, rgb)

      pixelColors(index / width)(index % width) = result
      if (receivedRes == width * height) {
        ImageIO.write(image, "jpg", new File("./", "test.jpg"))
        println("Results saved")

        timeE = System.nanoTime()
        println("Time[s]: " + (timeE - timeS) / 1.0e9)

        context.system.terminate()
      }
  }
}

// Minimal working example
@main def main(): Unit = {
  val aspect_ratio: Double = Camera.aspectRatio
  val width: Int = 400
  val height: Int = (width.toDouble / aspect_ratio).toInt
  val world: HittableList = HittableList()
    .add(Sphere(Point3D(0, 0, -1), 0.5))
    .add(Sphere(Point3D(0, -100.5, -1), 100))

  val system = ActorSystem("Rays")
  val camera = system.actorOf(Props(CameraActor(world, width, height)))
  camera ! ExecAll

  Await.ready(system.whenTerminated, Duration(10, TimeUnit.MINUTES))
}
