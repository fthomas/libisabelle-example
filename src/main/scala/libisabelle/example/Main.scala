package libisabelle.example

import java.nio.file.Paths

import edu.tum.cs.isabelle.api.{Configuration, Version}
import edu.tum.cs.isabelle.setup.Setup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process._

object Main extends App {
  val version = Version("2015")
  val session = "libisabelle-example"

  def mkRoot(isabelleBin: String): Future[Unit] = Future {
    Seq("rm", "-f", "ROOT").!!
    val out = Seq(isabelleBin, "mkroot", "-n", session).!!
    println(out)    
  }

  val res = for {
    setup <- Setup.defaultSetup(version)
    env <- setup.makeEnvironment
    isabelleBin = setup.home.resolve("bin/isabelle")
    _ <- mkRoot(isabelleBin.toString)
    config = Configuration.fromPath(Paths.get("."), session)
    built = edu.tum.cs.isabelle.System.build(env, config)
  } yield built

  println(Await.result(res, Duration.Inf))
}