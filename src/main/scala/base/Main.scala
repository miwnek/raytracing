package scala.base

// import akka.actor._
import scala.objects._
import akka.actor.{ActorSystem, Props}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

private val aspectRatio: Double = Camera.aspectRatio
private val width: Int = 400
private val height: Int = (width.toDouble / aspectRatio).toInt

private val materialGround: Material = Lambertian(Color(0.8, 0.8, 0.0))
private val materialCenter: Material = Lambertian(Color(0.7, 0.3, 0.3))
private val materialLeft  : Material = Metal(Color(0.8, 0.8, 0.8))
private val materialRight : Material = Metal(Color(0.8, 0.6, 0.2))


private val world: HittableList = HittableList()
    .add(Sphere(Point3D( 0.0, -100.5, -1.0),    100.0, materialGround))
    .add(Sphere(Point3D( 0.0,    0.0, -1.0),      0.5, materialCenter))
    .add(Sphere(Point3D(-1.0,    0.0, -1.0),      0.5, materialLeft))
    .add(Sphere(Point3D( 1.0,    0.0, -1.0),      0.5, materialRight))

@main def runNormal(): Unit = {
  val camera = Camera(world, width)

  // TODO: move progress bar here from Camera
  val colorsIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield camera.getSampledColor(i, j)

  val colors: List[Color] = colorsIterable.toList

  camera.writeScene(colors)
}


@main def runConcurrent() = {
  val system: ActorSystem = ActorSystem("Rays")
  val camera = system.actorOf(Props(CameraActor(world, width, height)))
  camera ! ExecAll

  Await.ready(system.whenTerminated, Duration(10, TimeUnit.MINUTES))
}
