name := "SecureCosSim"

version := "0.1"

scalaVersion := "2.11.0"

fork in run := true

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.6.0" % "provided"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.2"

