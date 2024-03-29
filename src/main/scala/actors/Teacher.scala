package actors

import akka.actor.{Props, Actor}



object Teacher {

  def props = Props(new Teacher)
}

class Teacher extends Actor {

  val gradeBook = context.actorOf(TeacherGradeBook.props, "grade-book")

  override def receive = Actor.emptyBehavior
}


object TeacherGradeBook {

  def props = Props(new TeacherGradeBook)

  case class UpdateBoard(studentId: Long, completionPercentage: Double)
}

class TeacherGradeBook extends Actor {

  context.system.eventStream.subscribe(self, classOf[Student.ActivityUpdated])

  override def receive = {
    case Student.ActivityUpdated(studentId, activityId, completionPercentage) =>
      println(s">>>>>> teacher grade book $studentId $activityId")
  }
}