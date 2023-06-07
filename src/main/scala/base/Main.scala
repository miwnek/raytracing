package scala.base

// import akka.actor._
import scala.objects._
import akka.actor.{ActorSystem, Props}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.collection.immutable.Vector3

private val aspectRatio: Double = Camera.aspectRatio
private val width: Int = 800
private val height: Int = (width.toDouble / aspectRatio).toInt
private val R = math.cos(math.Pi / 4)

private val aperture = 0.1
private val focusDist = 10.0

private val vfov: Double = 20.0
private val lookFrom = Point3D(13,2,3)
private val lookAt = Point3D(0,0,0)
private val vup = Vector3D(0,1,0)
private val spheresByTwo = 8

@main def runSequential(): Unit = {
  val timeS = System.nanoTime()
  val newWorld = randomScene(8)
  val camera = Camera(newWorld, width, vfov, lookFrom, lookAt, vup, aperture, focusDist)

  val colorsIterable = for {
    i <- 0 until width
    j <- 0 until height
  } yield camera.getSampledColor(i, j)

  val colors: List[Color] = colorsIterable.toList
  val timeE = System.nanoTime()
  camera.writeScene(colors)
  println("Time [s]: " + (timeE - timeS) / 1.0e9)
}

@main def runConcurrent() = {
  val timeS = System.nanoTime()
  val actorSystem: ActorSystem = ActorSystem("Rays")

  val newWorld = randomScene(8)
  val cameraMan = actorSystem.actorOf(Props(CameraActor(newWorld, width, vfov, 
                                        lookFrom, lookAt, vup, aperture, focusDist)))
  cameraMan ! TakePhoto

  Await.ready(actorSystem.whenTerminated, Duration(30, TimeUnit.MINUTES))
  val timeE = System.nanoTime()
  println("Time [s]: " + (timeE - timeS) / 1.0e9)
}

def randomScene(halfSphereNum: Int): HittableList = 
  val groundMaterial = Lambertian(Color(0.5, 0.5, 0.5))
  var world = HittableList().add( Sphere( Point3D(0, -1000, 0), 1000, groundMaterial ) )
  for {
    a <- - halfSphereNum to halfSphereNum + 1
    b <- - halfSphereNum to halfSphereNum + 1
  } do {
    val chooseMat = randomFraction()
    val center = Point3D(a + 1.1*randomFraction(), 0.2, b + 1.1*randomFraction())

    if (center - Point3D(4, 0.2, 0)).length > 1.1 then
      if chooseMat < 0.8 then
        val albedo = Vector3D.random() * Vector3D.random()
        val material = Lambertian(albedo)
        world = world.add( Sphere( center, 0.2, material ) )
      else if chooseMat < 0.95 then
        val albedo = Vector3D.random(0.5, 1)
        val fuzz = randomDouble(0, 0.5)
        val material = Metal(albedo, fuzz)
        world = world.add( Sphere( center, 0.2, material ) )
      else 
        val material = Dielectric(1.5)
        world = world.add( Sphere(center, 0.2, material) )
    }

    val m1 = Dielectric(1.5)
    world = world.add(Sphere(Point3D(0, 1, 0), 1.0, m1))

    val m2 = Lambertian(Color(0.4, 0.2, 0.1))
    world = world.add(Sphere(Point3D(-4, 1, 0), 1.0, m2))

    val m3 = Metal(Color(0.7, 0.6, 0.5), 0.0)
    world = world.add(Sphere(Point3D(4, 1, 0), 1.0, m3))

    world
  
  
