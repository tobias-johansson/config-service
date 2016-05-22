
scalaVersion := "2.11.8"

libraryDependencies ++= {
  object v {
    val finch = "0.10.0"
    val circe = "0.4.1"
  }
  Seq(
    "com.github.finagle" %% "finch-core"    % v.finch,
    "com.github.finagle" %% "finch-circe"   % v.finch,
    "io.circe"           %% "circe-core"    % v.circe,
    "io.circe"           %% "circe-generic" % v.circe,
    "io.circe"           %% "circe-parser"  % v.circe
  )
}
