/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys._
import sbt._
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {

  import uk.gov.hmrc._

  val appName = "http-verbs-example"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      scalaVersion := "2.11.7",
      libraryDependencies ++= AppDependencies(),
      crossScalaVersions := Seq("2.11.7"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      )
    )
}

private object AppDependencies {


  private val playVersion = "2.5.15"

  val compile = Seq(
    "com.typesafe.play" %% "play-ws" % playVersion,
    "com.typesafe.play" %% "play-json" % playVersion,
    "uk.gov.hmrc" %% "time" % "2.0.0",
    "uk.gov.hmrc" %% "http-verbs" % "7.1.0",
    "uk.gov.hmrc" %% "http-verbs-play-25" % "0.8.0"
  )


  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }



  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "com.typesafe.play" % "play-test_2.11" % playVersion % scope,
        "com.typesafe.play" % "play-specs2_2.11" % playVersion % scope,
        "commons-codec" % "commons-codec" % "1.7" % scope,
        "org.scalatest" %% "scalatest" % "2.2.4" % scope,
        "org.scalacheck" %% "scalacheck" % "1.12.2" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.7.1" % scope,
        "uk.gov.hmrc" %% "http-verbs-test" % "1.1.0" % scope,
        "ch.qos.logback" % "logback-core" % "1.1.7",
        "ch.qos.logback" % "logback-classic" % "1.1.7",
      "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}
