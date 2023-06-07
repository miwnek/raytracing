package scala.base

import scala.objects._
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File

type Filepath = String

class Camera(
    private val filepath: Filepath,
    private val world: HittableList,
    val width: Int,
    val vfov: Double,
    val lookFrom: Point3D,
    val lookAt: Point3D,
    val vup: Vector3D,
    val aperture: Double,
    val focusDist: Double
) {
  val height: Int = (width.toDouble / Camera.aspectRatio).toInt

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

  val image: BufferedImage = 
      BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  private def getRay(s: Double, t: Double): Ray =
    val rd: Vector3D = Vector3D.randomInUnitDisk() *  lensRadius
    val offset: Vector3D = u * rd.x + v * rd.y
    Ray(origin + offset, (lowerLeftCorner + (horizontal * s)) + (vertical * t) - origin - offset)

  def writeScene(colors: List[Color]): Unit =
    colors.zipWithIndex.foreach { (color, idx) =>
      val (row, col) = (idx % height, idx / height)
      val (r, g, b) = color.color256(Camera.samplesPerPixel)
      val rgb = (((r << 8) + g) << 8) + b
      image.setRGB(col, height - 1 - row, rgb)
    }
    ImageIO.write(image, "jpg", new File("./", filepath))
    

  def getSampledColor(i: Int, j: Int): Color =
    val spp: Int = Camera.samplesPerPixel
    @scala.annotation.tailrec
    def recursiveColorSum(clr: Color, iteration: Int): Color = iteration match
      case `spp` => clr
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
  val samplesPerPixel: Int = 50

  private val rand = scala.util.Random

  def apply(world: HittableList, width: Int, vfov: Double, lookFrom: Point3D, 
            lookAt: Point3D, vup: Vector3D, aperture: Double, focusDist: Double): Camera =
    val id: Int = math.abs(rand.nextInt()) % 1000
    new Camera(s"img_$id.jpg", world, width, vfov, lookFrom, lookAt, vup, aperture, focusDist)
}
