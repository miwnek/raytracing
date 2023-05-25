package scala.base

import scala.objects._
import java.io.FileWriter
import java.io.File

type Filepath = String

class Camera(
    private val filepath: Filepath,
    private val world: HittableList,
    val width: Int,
    val height: Int,
    val samplesPerPixel: Int,
    val vfov: Double,
    val lookFrom: Point3D,
    val lookAt: Point3D,
    val vup: Vector3D,
    val aperture: Double,
    val focusDist: Double
) {
  lazy private val fileWriter: FileWriter = new FileWriter(new File(this.filepath))

  val theta = degreesToRadians(this.vfov)
  val h = math.tan(theta/2)
  val viewportHeight: Double = 2.0 * h
  val viewportWidth: Double = aspectRatio * viewportHeight

  val w = (lookFrom - lookAt).unitVector
  val u = (vup cross w).unitVector
  val v = w cross u

  val focalLength: Double = 1.0
  val origin: Point3D = lookFrom
  val horizontal: Vector3D = u * viewportWidth * focusDist
  val vertical: Vector3D = v * viewportHeight * focusDist
  val lowerLeftCorner: Vector3D =
    origin - (horizontal / 2) - (vertical / 2) - (w * focusDist)
  val lensRadius = aperture / 2

  private def getRay(s: Double, t: Double): Ray =
    val rd: Vector3D = Vector3D.randomInUnitDisk() *  lensRadius
    val offset: Vector3D = u * rd.x + v * rd.y
    Ray(origin + offset, (lowerLeftCorner + (horizontal * s)) + (vertical * t) - origin - offset)

  def writeScene(colors: List[Color]): Unit =
    val file = this.fileWriter
    file.write("P3\n")
    file.write(s"$width $height\n")
    file.write("255\n")
    colors.zipWithIndex.foreach { (color, idx) =>
      file.write(color.colorToWrite(samplesPerPixel))
    }
    System.err.println("\nDone.\n")
    file.close()

  def getSampledColor(i: Int, j: Int): Color =
    @scala.annotation.tailrec
    def recursiveColorSum(clr: Color, iteration: Int): Color = iteration match
      case `samplesPerPixel` => clr
      case _ =>
        val u: Double = (i + randomFraction()) / (width - 1)
        val v: Double = (j + randomFraction()) / (height - 1)
        val ray: Ray = this.getRay(u, v)
        recursiveColorSum(clr + ray.rayColor(world, Camera.maxDepth), iteration + 1)
    recursiveColorSum(Color(0, 0, 0), 0)

}
object Camera {
  val aspectRatio: Double = 16.0 / 9.0
  val maxDepth: Int = 50

  private val rand = scala.util.Random

  def apply(world: HittableList, width: Int, vfov: Double, lookFrom: Point3D, 
            lookAt: Point3D, vup: Vector3D, aperture: Double, focusDist: Double): Camera =
    val height: Int = (width.toDouble / aspectRatio).toInt
    val id: Int = math.abs(rand.nextInt()) % 1000
    new Camera(s"tmp_$id.ppm", world, width, height, 100, vfov, lookFrom, lookAt, vup, aperture, focusDist)

  // def apply(
  //     filepath: Filepath,
  //     rays: List[Ray],
  //     world: HittableList,
  //     vfov: Double,
  //     width: Int,
  //     samplesPerPixel: Int
  // ): Camera =
  //   val height: Int = (width.toDouble / aspectRatio).toInt
  //   new Camera(filepath, world, width, height, samplesPerPixel, vfov)
}
