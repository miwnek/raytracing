package scala.base

import scala.objects._
import java.io.FileWriter
import java.io.File

type Filepath = String

class Renderer (
    private val filepath: Filepath, 
    private val rays: List[Ray],
    private val world: HittableList,
    private val width: Int,
    private val height: Int
) {
    lazy private val fileWriter: FileWriter = new FileWriter(new File(this.filepath))

    def writeScene(): Unit = 
        val file = this.fileWriter
        file.write("P3\n")
        file.write(s"$width $height\n")
        file.write("255\n")
        this.rays.zipWithIndex.foreach{ (ray, idx) =>
            if idx % width == 0 then
                val left: Int = ((width * height) - idx) / width
                System.err.println(f"\rScanlines remaining: $left ")
                System.err.flush()
            file.write(ray.rayColorString(world)) 
        }
        System.err.println("\nDone.\n")
        file.close()
}
object Renderer {
    val aspectRatio: Double = 16.0 / 9.0
    val viewportHeight: Double = 2.0
    val viewportWidth: Double = aspectRatio * viewportHeight
    val focalLength: Double = 1.0

    val origin: Point3D = Point3D(0, 0, 0)
    val horizontal: Vector3D = Vector3D(viewportWidth, 0, 0)
    val vertical: Vector3D = Vector3D(0, viewportHeight, 0)
    val lowerLeftCorner: Vector3D = 
        origin - (horizontal / 2) - (vertical / 2) - Vector3D(0, 0, focalLength)

    private val rand = scala.util.Random

    def apply(rays: List[Ray], world: HittableList, width: Int, height: Int): Renderer = 
        val id: Int = math.abs(rand.nextInt()) % 1000
        new Renderer(s"tmp_$id.ppm", rays, world, width, height)

    def apply(filepath: Filepath, rays: List[Ray], world: HittableList, width: Int, height: Int): Renderer =
        new Renderer(filepath, rays, world, width, height)
}