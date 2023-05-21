package scala.objects

import scala.base.Vector3D
import scala.base.Point3D
import scala.base.Ray

trait HitResult
case class HitRecord(point: Point3D, frontFace: Boolean, normal: Vector3D, t: Double) extends HitResult
case object Missed extends HitResult

trait Hittable {
    def hitBy(ray: Ray, tMin: Double, tMax: Double): HitResult
}

class HittableList(val figures: List[Hittable]) {
    def add(figure: Hittable): HittableList = 
        HittableList(figure :: figures)

    def clear(): HittableList = 
        HittableList(List.empty[Hittable])

    def hitBy(ray: Ray, tMin: Double, tMax: Double): HitResult = 
        @scala.annotation.tailrec
        def hitInner(closestSoFar: Double, result: HitResult, objects: List[Hittable]): HitResult = objects match
            case Nil => result
            case x :: next =>
                val tempResult: HitResult = x.hitBy(ray, tMin, closestSoFar)
                tempResult match 
                    case Missed => hitInner(closestSoFar, result, next)
                    case hitResult =>
                        val newResult: HitRecord = hitResult.asInstanceOf[HitRecord]
                        hitInner(newResult.t, newResult, next)
        
        hitInner(closestSoFar = tMax, result = Missed, objects = this.figures)
}
object HittableList {
    def apply(): HittableList = new HittableList(List.empty[Hittable])
    def apply(figures: List[Hittable]) = new HittableList(figures)
    def apply(args: Hittable*) = new HittableList(args.toList)
}

case class Sphere(val center: Point3D, val radius: Double) extends Hittable {
    override def hitBy(ray: Ray, tMin: Double, tMax: Double): HitResult = 
        val oc: Vector3D = ray.origin - this.center
        val a: Double = ray.direction.length_squared
        val halfB: Double = oc dot ray.direction
        val c = oc.length_squared - radius * radius
        val delta = halfB*halfB - a*c

        if delta < 0 then 
            Missed
        else
            val sqrtd: Double = math.sqrt(delta)
            val rootOne: Double = - (halfB + sqrtd) / a
            val rootTwo: Double = (-halfB + sqrtd) / a
            val root: Double = 
                if (rootOne >= tMin) && (rootOne <= tMax) then rootOne
                else if rootTwo >= tMin && rootTwo <= tMax then rootTwo
                else -1
            if root == -1 then 
                Missed 
            else
                val point: Point3D = ray at root
                val outwardNormal: Vector3D = (point - this.center) / this.radius
                val frontFace: Boolean = (ray.direction dot outwardNormal) < 0
                val normal: Vector3D = if frontFace then outwardNormal else - outwardNormal 
                HitRecord(point, frontFace, normal, root)

    
}