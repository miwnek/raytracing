package base

import scala.math.{max, min, pow, sqrt}
import akka.actor.{Actor, Props, ActorSystem}
import scala.base.{Color, Point3D, Vector3D}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import scala.collection.immutable.Vector3

class Ray(val origin: Point3D, val direction: Vector3D)
class LightSource(val position: Point3D, val intensity: Double)
class Material(val diffuseColor: Color, val specularColor: Color, val reflectivity: Double)

abstract class WorldObject(val material: Material) {
  def intersect(ray: Ray): Option[Point3D]

  def normal(point: Point3D): Vector3D

  def reflectRay(ray: Ray, point: Point3D): Ray = Ray(
    point,
    (ray.direction - this.normal(point) * 2 * (ray.direction o this.normal(point))).unitVector
  )

  def shade(point: Point3D, ray: Ray, lightSources: Array[LightSource]): Color = {
    var totalColor = Color(0, 0, 0)

    for (lightSource <- lightSources) {
      val lightDirection = point - lightSource.position
      val diffuseTerm = max(lightDirection o this.normal(point), 0)
      val specularTerm = pow(max(0, ray.direction o this.reflectRay(ray, point).direction), 2)

      totalColor += (this.material.diffuseColor * diffuseTerm + this.material.specularColor * specularTerm) * lightSource.intensity
    }

    return totalColor
  }
}
class WorldScene(val worldObjects: Array[WorldObject], val lightSources: Array[LightSource]) {
  def getIntersectedObjectsBy(ray: Ray): Array[(WorldObject, Point3D)] = {
    var intersectedObjects: Array[(WorldObject, Point3D)] = Array()
    for (worldObject <- this.worldObjects) {
      worldObject.intersect(ray) match
        case None                    => {}
        case Some(intersectionPoint) => intersectedObjects :+= (worldObject, intersectionPoint)
    }

    return intersectedObjects
  }
}

def TraceRay(ray: Ray, worldScene: WorldScene, depth: Int = 0, maxDepth: Int = 4): Color = {

  if (depth >= maxDepth) return Color(0, 0, 0)

  val intersectedObjects =
    worldScene
      .getIntersectedObjectsBy(ray)
      .sortBy((worldObject, intersectionPoint) => (intersectionPoint - ray.origin).length)

  intersectedObjects.length match
    case 0 => return Color(0, 0, 0)
    case _: Int =>
      val (closestWorldObject, closestIntersection) = intersectedObjects(0)
      val normal = closestWorldObject.normal(closestIntersection)
      var pixelColor = closestWorldObject.shade(closestIntersection, ray, worldScene.lightSources)

      val reflectedRay = closestWorldObject.reflectRay(ray, closestIntersection)
      val reflectionColor = TraceRay(reflectedRay, worldScene, depth + 1, maxDepth)

      pixelColor =
        pixelColor * (1 - closestWorldObject.material.reflectivity) + reflectionColor * closestWorldObject.material.reflectivity

      return pixelColor
}

//****************
class Sphere(material: Material, val radius: Double, val center: Point3D)
    extends WorldObject(material) {
  override def intersect(ray: Ray): Option[Point3D] = {
    val oc = ray.origin - center
    val u = ray.direction.unitVector
    val delta =
      pow((u o oc), 2) - (oc.length_squared - radius * radius)

    if (delta < 0) return None

    val d1 = -(u o oc) + sqrt(delta)
    val d2 = -(u o oc) - sqrt(delta)
    val x1 = ray.origin + u * d1
    val x2 = ray.origin + u * d2

    if ((x1 - ray.origin).length < (x2 - ray.origin).length) return Some(x1)

    return Some(x2)
  }

  override def normal(point: Point3D): Vector3D = (point - center).unitVector

}

class Glass(
    diffuseColor: Color = Color(0.8, 0.8, 0.8),
    specularColor: Color = Color(0.8, 0.8, 0.8),
    reflectivity: Double = 0
) extends Material(diffuseColor, specularColor, reflectivity)
//*****************************************

case class ExecAll()
case class ExecFoo(f: Any => Any, arg: Any)
case class Return(index: Int, result: Any)

class RayActor extends Actor {
  override def receive: Actor.Receive = {
    case ExecFoo(f, arg) => {
      val result = f(arg)
      val index = self.path.name.toInt

      sender() ! Return(index, result)
    }
  }
}

class CameraActor(cameraPosition: Point3D, width: Int, height: Int) extends Actor {

  override def receive: Actor.Receive = {
    case ExecAll()             => {}
    case Return(index, result) => {}
  }
}

object Main extends App {
  println("Hello")

  val height = 480
  val width = 640
  val pixelColors: Array[Array[Color]] = Array.ofDim(height, width)

  var worldObjects: Array[WorldObject] = Array()
  worldObjects :+= Sphere(Glass(), 100, Point3D(0, 0, 200))

  var lightSources: Array[LightSource] = Array()
  lightSources :+= LightSource(Point3D(2, 2, 200), 0.5)

  val worldScene = WorldScene(worldObjects, lightSources)
  val origin = Point3D(0, 0, 0)

  for (x <- 0 until width) {
    for (y <- 0 until height) {
      val ray = Ray(origin, Vector3D(x - width / 2, y - height / 2, 200))
      pixelColors(y)(x) = TraceRay(ray, worldScene)
    }
  }

  val image: BufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  for (x <- 0 until width) {
    for (y <- 0 until height) {

      val Color(r, g, b) = pixelColors(y)(x)
      var rgb: Int = (r * 255).toInt
      rgb = (rgb << 8) + (g * 255).toInt
      rgb = (rgb << 8) + (b * 255).toInt
      image.setRGB(x, y, rgb)
    }
  }

  ImageIO.write(image, "jpg", new File("./", "test.jpg"))
}
