package base

import java.io.FileWriter
import java.io.File

type Filepath = String

class Renderer (
    private val filepath: Filepath, 
    private val vectors: List[Vector3D],
    private val width: Int,
    private val height: Int
) {
    lazy private val fileWriter: FileWriter = new FileWriter(new File(this.filepath))

    def writeScene(): Unit = 
        val file = this.fileWriter
        file.write("P3\n")
        file.write(s"$width $height\n")
        file.write("255\n")
        this.vectors.zipWithIndex.foreach{ (color, idx) =>
            if idx % width == 0 then
                val left: Int = ((width * height) - idx) / width
                System.err.println(f"\rScanlines remaining: $left ")
                System.err.flush()
            file.write(color.toStringRGB) 
        }
        System.err.println("\nDone.\n")
        file.close()
}
object Renderer {
    private val rand = scala.util.Random

    def apply(vectors: List[Vector3D], width: Int, height: Int): Renderer = 
        val id: Int = math.abs(rand.nextInt()) % 1000
        new Renderer(s"tmp_$id.ppm", vectors, width, height)

    def apply(filepath: Filepath, vectors: List[Vector3D], width: Int, height: Int): Renderer =
        new Renderer(filepath, vectors, width, height)
}