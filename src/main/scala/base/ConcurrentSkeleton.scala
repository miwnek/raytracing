package scala.base

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem

case class ExecAll()
case class ExecFoo(f: Point3D => Color, arg: Point3D)
case class SendRes(index: Int, result: Color)

class RayActor extends Actor {
  override def receive: Actor.Receive = {
    case ExecFoo(f, arg) => {
      val res = f(arg)
      val idx = self.path.name.toInt
      sender() ! SendRes(idx, res)
    }
  }
}

class CameraActor(no_rays: Int, f: Point3D => Color, points: Array[Point3D])
    extends Actor {

  val origin: Point3D = Vector3D(0, 0, 0)
  

  var received: Int = 0
  var results: Array[Color] = Array.fill(no_rays)(Color(0, 0, 0))
  val rays =
    (0 until no_rays).map(index =>
      context.actorOf(Props(RayActor()), index.toString)
    )

  override def receive: Actor.Receive = {

    case ExecAll =>
      for (idx <- 0 to no_rays) rays(idx) ! ExecFoo(f, points(idx))

    case SendRes(idx, res) => {
      results(idx) = res
      received += 1
      if (received == no_rays) {
        // Write image ...
        println("Results: " + results.mkString(", "))
        println("Writing image...")
        context.system.terminate()
      }
    }
  }
}

object Main extends App {
  val system = ActorSystem("Rays")
  val no_rays = 3
  def f(point: Point3D): Color = { Color(0, 0, 0) }
  val points: Array[Point3D] =
    Array(Vector3D(0, 0, 0), Vector3D(1, 1, 1), Vector3D(2, 2, 2))

  // def TraceRay(origin, direction, scene_objects, rec_depth) ...
  // CameraActor(no_rays, camera_position, TraceRay, *args)

  val camera = system.actorOf(Props(CameraActor(no_rays, f, points)))
  camera ! ExecAll

}
