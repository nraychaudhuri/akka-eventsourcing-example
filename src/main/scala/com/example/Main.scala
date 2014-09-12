package com.example

import actors.{Teacher, Student}
import akka.actor.{Props, ActorSystem}

import models.Models._

object Main {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem("courseware")


    val student1 = system.actorOf(Student.props(101L))
    val student2 = system.actorOf(Student.props(202L))
    val student3 = system.actorOf(Student.props(303L))

    val teacher = system.actorOf(Teacher.props, "susan")

    //assign courses studentActivities
    student1 ! Student.CourseAssignment(1001L)
    student2 ! Student.CourseAssignment(2002L)
    student3 ! Student.CourseAssignment(3003L)

    //update
    student1 ! Student.UpdateActivity(1, XmlPayload())
    student2 ! Student.UpdateActivity(1, XmlPayload())
    student3 ! Student.UpdateActivity(1, XmlPayload())

    scala.io.StdIn.readLine()

    system.shutdown()
    system.awaitTermination()

  }
}
