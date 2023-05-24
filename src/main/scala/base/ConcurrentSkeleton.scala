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

  val pixelColors: Array[Array[Color]] = Array.ofDim(height, width)
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
      val Color(r, g, b) = 
        (result / camera.samplesPerPixel)
        .map(math.sqrt(_))
        .map(clamp(_, 0.0, 0.999))
      val rgb: Int = 
        ((((r * 255).toInt
        << 8) + (g * 255).toInt)
        << 8) + (b * 255).toInt
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