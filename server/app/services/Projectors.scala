
package projector

object Projectors {

  val projectors = Map(
    "Tests" -> (1 to 10).map { case id =>
      val p = new TestProjector()
      p.name = s"Test-$id"
      p
    }
  )

  def all = projectors.values.flatten
  all.zipWithIndex.foreach { case (p,id) => p.id = id }
}

