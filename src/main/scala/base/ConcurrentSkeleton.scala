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

case object TakePhoto
case class ExecFoo(f: (Int, Int) => Color, x: Int, y: Int)
case class Return(index: Int, result: Color)

class RayActor extends Actor {
  var result: Color = Color(0, 0, 0)
  val index: Int = self.path.name.toInt
  override def receive: Actor.Receive = { case ExecFoo(f, x, y) =>
    result = f(x, y)
    sender() ! Return(index, result)
  }
}

class CameraActor(world: HittableList, width: Int, height: Int) extends Actor {

  val camera: Camera = Camera(world, width)
  val rayActors = (0 until width * height)
    .map(index => context.actorOf(Props(RayActor()), index.toString))

  val pixelColors: Array[Array[Color]] = Array.ofDim(height, width)
  var receivedRes: Int = 0
  var totalTime: Long = 0

  val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  override def receive: Actor.Receive = {
    case TakePhoto =>
      for (x <- 0 until width; y <- 0 until height)
        rayActors(y * width + x) ! ExecFoo(camera.getSampledColor, x, y)

    case Return(index, result) =>
      receivedRes += 1
      val (x, y) = (index / width, index % width)
      pixelColors(x)(y) = result / camera.samplesPerPixel

      if (receivedRes == width * height) {
        for (x <- 0 until width; y <- 0 until height) {
          val Color(r, g, b) = pixelColors(y)(x)
          val rgb: Int = ((((r * 255).toInt << 8) + (g * 255).toInt) << 8) + (b * 255).toInt
          image.setRGB(x, height - 1 - y, rgb)
        }

        ImageIO.write(image, "jpg", new File("./", "test.jpg"))
        context.system.terminate()
      }
  }
}
