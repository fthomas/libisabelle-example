package libisabelle.example

import java.nio.file.Paths

import edu.tum.cs.isabelle.api.{Configuration, Version}
import edu.tum.cs.isabelle.setup.Setup
import edu.tum.cs.isabelle.{Operation, System}
import org.log4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process._

object Main extends App {
  val logger = getLogger
  val version = Version("2015")
  val session = "libisabelle-example"

  def mkroot(isabelle: String): Future[Unit] = Future {
    Seq("rm", "-f", "ROOT").!!
    val out = Seq(isabelle, "mkroot", "-n", session).!!
    logger.info(out)
  }

  val future = for {
    setup <- Setup.defaultSetup(version)
    env <- setup.makeEnvironment
    isabelle = setup.home.resolve("bin/isabelle")
    _ <- mkroot(isabelle.toString)
    config = Configuration.fromPath(Paths.get("."), session)
    built = System.build(env, config)
    _ <- Future {
      if (!built) sys.error(s"""Could not built session "$session"""")
      else logger.info(s"""Built session "$session"""")
    }
    sys <- System.create(env, config)
    _ <- Future(logger.info(s"Created system $sys"))
    res <- sys.invoke(Operation.Hello)("hello")
    _ <- sys.dispose
  } yield res

  println(Await.result(future, Duration.Inf))
}
