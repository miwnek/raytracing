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
    val samplesPerPixel: Int
) {
  lazy private val fileWriter: FileWriter = new FileWriter(new File(this.filepath))

  def writeScene(colors: List[Color]): Unit =
    val file = this.fileWriter
    file.write("P3\n")
    file.write(s"$width $height\n")
    file.write("255\n")
    colors.zipWithIndex.foreach { (color, idx) =>
      if idx % width == 0 then
        val left: Int = ((width * height) - idx) / width
        System.err.println(f"\rScanlines remaining: $left ")
        System.err.flush()
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
        val ray: Ray = Camera.getRay(u, v)
        recursiveColorSum(clr + ray.rayColor(world, Camera.maxDepth), iteration + 1)
    recursiveColorSum(Color(0, 0, 0), 0)

}
object Camera {
  val aspectRatio: Double = 16.0 / 9.0
  val viewportHeight: Double = 2.0
  val viewportWidth: Double = aspectRatio * viewportHeight
  val focalLength: Double = 1.0
  val maxDepth: Int = 50

  val origin: Point3D = Point3D(0, 0, 0)
  val horizontal: Vector3D = Vector3D(viewportWidth, 0, 0)
  val vertical: Vector3D = Vector3D(0, viewportHeight, 0)
  val lowerLeftCorner: Vector3D =
    origin - (horizontal / 2) - (vertical / 2) - Vector3D(0, 0, focalLength)

  private def getRay(u: Double, v: Double): Ray =
    Ray(origin, lowerLeftCorner + horizontal * u + vertical * v - origin)

  private val rand = scala.util.Random

  def apply(world: HittableList, width: Int): Camera =
    val height: Int = (width.toDouble / aspectRatio).toInt
    val id: Int = math.abs(rand.nextInt()) % 1000
    new Camera(s"tmp_$id.ppm", world, width, height, 100)

  def apply(
      filepath: Filepath,
      rays: List[Ray],
      world: HittableList,
      width: Int,
      samplesPerPixel: Int
  ): Camera =
    val height: Int = (width.toDouble / aspectRatio).toInt
    new Camera(filepath, world, width, height, samplesPerPixel)
}
