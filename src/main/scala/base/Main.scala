package scala.base

// import akka.actor._
import scala.objects._
import akka.actor.{ActorSystem, Props}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

private val aspectRatio: Double = Camera.aspectRatio
private val width: Int = 1080
private val height: Int = (width.toDouble / aspectRatio).toInt

private val materialGround: Material = Lambertian(Color(0.8, 0.8, 0.0))
private val materialCenter: Material = Lambertian(Color(0.7, 0.3, 0.3))
private val materialLeft: Material = Metal(Color(0.8, 0.8, 0.8), 0.3)
private val materialRight: Material = Dielectric(1.5)

private val world: HittableList = HittableList()
  .add(Sphere(Point3D(0.0, -100.5, -1.0), 100.0, materialGround))
  .add(Sphere(Point3D(0.0, 0.0, -1.0), 0.5, materialCenter))
  .add(Sphere(Point3D(-1.0, 0.0, -1.0), 0.5, materialLeft))
  .add(Sphere(Point3D(1.0, 0.0, -1.0), 0.5, materialRight))

@main def runNormal(): Unit = {
  val timeS = System.nanoTime()
  val camera = Camera(world, width)

  // TODO: move progress bar here from Camera
  val colorsIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield camera.getSampledColor(i, j)

  val colors: List[Color] = colorsIterable.toList
  val timeE = System.nanoTime()

  println("Time [s]: " + (timeE - timeS) / 1.0e9)

  // camera.writeScene(colors)
}

@main def runConcurrent() = {
  val timeS = System.nanoTime()
  val actorSystem: ActorSystem = ActorSystem("Rays")
  val cameraMan = actorSystem.actorOf(Props(CameraActor(world, width, height)))
  cameraMan ! TakePhoto

  Await.ready(actorSystem.whenTerminated, Duration(10, TimeUnit.MINUTES))
  val timeE = System.nanoTime()
  println("Time [s]: " + (timeE - timeS) / 1.0e9)
}
