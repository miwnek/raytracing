package base

// import akka.actor._

@main def run(args: String*): Unit = {

  // Writing a sample image to a file 
  // using prototyped data structures 
  val width: Int = 256
  val height: Int = 256

  val someVec: Vector3D = Vector3D(0, 1, 0.25)

  val vectorsIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield Vector3D(i.toDouble / (width - 1),
                   j.toDouble / (height - 1), 0.25)

  val vectors: List[Vector3D] = vectorsIterable.toList

  val renderer = Renderer(vectors, width, height)

  renderer.writeScene()
}

