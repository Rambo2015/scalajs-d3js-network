import io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._


case class Person(name: String)
// defined class Person

case class Greeting(salutation: String, person: String, exclamationMarks: Int)
// defined class Greeting

val greetJSON = Greeting("Hey", "Chris", 3).asJson

val back = decode[Greeting](greetJSON.spaces2)