package scala.base

// import akka.actor._
import scala.objects._
import akka.actor.{ActorSystem, Props}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.collection.immutable.Vector3

private val aspectRatio: Double = Camera.aspectRatio
private val width: Int = 200
private val height: Int = (width.toDouble / aspectRatio).toInt
private val R = math.cos(math.Pi / 4)

private val materialGround: Material = Lambertian(Color(0.8, 0.8, 0.0))
private val materialCenter: Material = Lambertian(Color(0.1, 0.2, 0.5))
private val materialLeft: Material = Dielectric(1.5)
private val materialRight: Material = Metal(Color(0.8, 0.6, 0.2), 0.0)

private val aperture = 0.1
private val focusDist = 10.0

private val world: HittableList = HittableList()
  .add(Sphere(Point3D(0.0, -100.5, -1.0), 100.0, materialGround))
  .add(Sphere(Point3D(0.0,    0.0, -1.0),   0.5, materialCenter))
  .add(Sphere(Point3D(-1.0,   0.0, -1.0),   0.5, materialLeft))
  .add(Sphere(Point3D(-1.0,   0.0, -1.0), -0.45, materialLeft))
  .add(Sphere(Point3D( 1.0,   0.0, -1.0),   0.5, materialRight))

private val vfov: Double = 20.0
private val lookFrom = Point3D(-2, 2, 1)
private val lookAt = Point3D(0, 0, -1)
private val vup = Vector3D(0, 1, 0)

@main def runSequential(): Unit = {
  val timeS = System.nanoTime()
  // val camera = Camera(world, width, vfov, lookFrom, lookAt, vup, aperture, focusDist)
  val newWorld = randomScene()
  val camera = Camera(newWorld, width, 20, Point3D(13,2,3), Point3D(0,0,0), Vector3D(0,1,0), aperture, focusDist)

  val colorsIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield camera.getSampledColor(i, j)

  val colors: List[Color] = colorsIterable.toList
  val timeE = System.nanoTime()
  camera.writeScene(colors)
  println("Time [s]: " + (timeE - timeS) / 1.0e9)
}

@main def runConcurrent() = {
  val timeS = System.nanoTime()
  val actorSystem: ActorSystem = ActorSystem("Rays")

  val newWorld = randomScene()
  val cameraMan = actorSystem.actorOf(Props(CameraActor(newWorld, width, height, 20, 
                                        Point3D(13,2,3), Point3D(0,0,0), Vector3D(0,1,0), aperture, focusDist)))
  cameraMan ! TakePhoto

  Await.ready(actorSystem.whenTerminated, Duration(10, TimeUnit.MINUTES))
  val timeE = System.nanoTime()
  println("Time [s]: " + (timeE - timeS) / 1.0e9)
}

def randomScene(): HittableList = 
  val groundMaterial = Lambertian(Color(0.5, 0.5, 0.5))
  var world = HittableList().add( Sphere( Point3D(0, -1000, 0), 1000, groundMaterial ) )
  for {
    a <- -11 to 12
    b <- -11 to 12
  } do {
    val chooseMat = randomFraction()
    val center = Point3D(a + 0.9*randomFraction(), 0.2, b + 0.9*randomFraction())

    if (center - Point3D(4, 0.2, 0)).length > 0.9 then
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
  
  