import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import com.twitter.finagle.Http
import com.twitter.util.Await
import shapeless.HNil

object Data {
  case class InValue(value: String, ttl: Option[Long])

  case class Result(space: String, key: String, value: String)
  case class Value(value: String, tod: Option[Long])
  case class Time(time: Long)
}

object Storage {
  import Data._

  type Space = Map[String, Value]
  type Store = Map[String, Space]
  var store = Map[String, Space]()

  def read(space: String, key: String): Option[String] =
    for {
      spc   <- store.get(space)
      value <- spc.get(key)
      if !expired(value)
    } yield value.value

  def write(space: String, key: String, inValue: InValue): Unit = {
    val tod   = inValue.ttl map (_ + now())
    val value = Value(inValue.value, tod)
    val spc   = store.get(space)
                     .getOrElse(Map[String, Value]())
                     .updated(key, value)
    store = store.updated(space, spc)
  }

  def expired(value: Value) =
    value.tod map (_ < now()) getOrElse (false)

  def now(): Long = System.currentTimeMillis()

}

object Main extends App {

  import Data._

  val getVal: Endpoint[Result] = get(string/string) { (space: String, key: String) =>
    Storage.read(space, key) match {
      case Some(value) => Ok(Result(space, key, value))
      case None        => NotFound(new Exception("the message"))
    }
  }

  val putVal: Endpoint[Result] = post(string/string :: body.as[InValue]) { (space: String, key: String, value: InValue) =>
    Storage.write(space, key, value)
    Ok(Result(space, key, value.value))
  }

  val time: Endpoint[Time] = get(/) map { _: HNil => Time(Storage.now()) }

  val api =
    ("values" / (getVal :+: putVal)) :+:
    ("time"   / (time))

  val server = Http.serve(":8080", api.toService)


  Await.ready(server)
}
