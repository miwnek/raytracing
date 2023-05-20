package scala.base

// import akka.actor._

@main def run(args: String*): Unit = {

  // Just a sample, doesn't really make sense.
  // Everything here and in the Scene.scala file is temporary
  val aspect_ratio: Double = Renderer.aspectRatio
  val width: Int = 400
  val height: Int = (width.toDouble / aspect_ratio).toInt


  
  val raysIterable = for {
    j <- (height - 1).until(-1, -1)
    i <- 0 until width
  } yield {
    val u: Double = i.toDouble / (width - 1)
    val v: Double = j.toDouble / (height - 1)
    Ray(Renderer.origin, Renderer.lowerLeftCorner + Renderer.horizontal * u + Renderer.vertical * v - Renderer.origin)
  }

  val rays: List[Ray] = raysIterable.toList

  val renderer = Renderer(rays, width, height)

  renderer.writeScene()
}

