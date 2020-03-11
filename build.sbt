name := "slick-mysql-scala"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "mysql" % "mysql-connector-java" % "6.0.6"
)