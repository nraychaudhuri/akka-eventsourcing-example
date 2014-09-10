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

  //TODO: subscribing to event stream. This is a naive example. In real world
  //we will replace this with akka journal and views
  context.system.eventStream.subscribe(self, classOf[Activity.ActivityUpdated])

  override def receive = {
    case Activity.ActivityUpdated(studentId, activityId, completionPercentage) =>
      println(s">>>>>> teacher grade book $studentId $activityId")
  }
}