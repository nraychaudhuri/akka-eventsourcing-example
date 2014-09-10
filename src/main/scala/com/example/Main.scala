package com.example

import actors.{XmlPayload, Teacher, Student}
import akka.actor.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem("courseware")

    val student1 = system.actorOf(Student.props)
    val student2 = system.actorOf(Student.props)
    val student3 = system.actorOf(Student.props)

    val teacher = system.actorOf(Teacher.props, "susan")

    //assign courses studentActivities
    student1 ! Student.CourseAssignment(101L, 1001L)
    student2 ! Student.CourseAssignment(202L, 2002L)
    student3 ! Student.CourseAssignment(303L, 3003L)

    //update
    student1 ! Student.UpdateActivity(101L, 1, XmlPayload())
    student2 ! Student.UpdateActivity(202L, 1, XmlPayload())
    student3 ! Student.UpdateActivity(303L, 1, XmlPayload())

    scala.io.StdIn.readLine()

    system.shutdown()
    system.awaitTermination()

  }
}
