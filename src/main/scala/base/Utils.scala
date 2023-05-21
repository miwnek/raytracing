package scala.base

val infinity: Double = Double.MaxValue
val pi: Double = math.Pi
val degreesToRadians: Double => Double = deg => deg  * pi / 180.0

val randomFraction = () =>
    val rand = scala.util.Random
    rand.between(0.0, 1.0)

val randomDouble = (x: Double, y: Double) =>
    val rand = scala.util.Random
    rand.between(x, y)

val clamp= (x: Double, min: Double, max: Double) =>
    if x < min then min
    else if x > max then max
    else x