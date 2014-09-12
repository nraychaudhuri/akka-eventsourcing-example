package actors

import akka.actor.{ActorRef, Props, Actor}
import akka.persistence.PersistentActor

import models.Models._

import scala.util.{Success, Failure}


object Student {

  //commands
  case class CourseAssignment(courseId: Long)
  case class LoadActivity(activityId: Long)
  case class UpdateActivity(activityId: Long, p: XmlPayload, score: Option[Double] = None)

  //events
  case class CourseIsAssigned(studentId: Long, courseId: Long)
  case class ActivityAccessed(studentId: Long, activityId: Long)

  def props(studentId: Long) = Props(new Student(studentId))
}


class Student(studentId: Long) extends PersistentActor with StudentDomainService {

  import Student._

  //internal state
  var courseId: Long = _
  var lastActivityAccessedId: Long = _

  override val persistenceId = s"student-$studentId-flow"


  override def receiveRecover: Receive = {
    case e: CourseIsAssigned => updateCourseId(e)
    case e: ActivityAccessed => updateLastActivityId(e)
  }


  override def preStart(): Unit = {
    super.preStart()
    context.actorOf(StudentGradeBook.props(studentId), "grade-book")
  }


  override def receiveCommand = {

    case CourseAssignment(courseId: Long) =>
      loadActivities(studentId, courseId).foreach(activityId => createActivity(activityId, studentId))
      persist(CourseIsAssigned(studentId, courseId))(updateCourseId)

    case LoadActivity(activityId) =>
       lookUpActivity(activityId, studentId).foreach(_.forward(Activity.Get))
      persist(ActivityAccessed(studentId, activityId))(updateLastActivityId)

    case UpdateActivity(activityId, payload, score) =>
      lookUpActivity(activityId, studentId).foreach(_.forward(Activity.Update(payload, score)))
  }


  private def updateCourseId(e: CourseIsAssigned) = {
    courseId = e.courseId
  }

  private def updateLastActivityId(e: ActivityAccessed) = {
    lastActivityAccessedId = e.activityId
  }

  private def lookUpActivity(activityId: Long, studentId: Long): Option[ActorRef] = {
    context.child(s"activity-$studentId-$activityId")
  }

  //creating each activity as a child actor
  private def createActivity(activityId: Long, studentId: Long): ActorRef = {
    context.actorOf(Activity.props(activityId, studentId), name=s"activity-$studentId-$activityId")
  }
}


object Activity {

  case object Get
  case class Update(p: XmlPayload, score: Option[Double] = None)
  case class ActivityData(studentId: Long, activityId: Long, p: XmlPayload, score: Option[Double])


  //event published whenever activity is changed.
  case class ActivityUpdated(studentId: Long, activityId: Long, completionPercentage: Double)


  def props(activityId: Long, studentId: Long) = Props(new Activity(activityId, studentId))
}


class Activity(activityId: Long, studentId: Long) extends PersistentActor {

  import Activity._
  import scala.concurrent.ExecutionContext.Implicits.global

  override val persistenceId = s"activity-$studentId-$activityId"


  override def receiveRecover: Receive = {
    case ActivityUpdated(_, _, percentage) =>
      println(s"Inside the recover block of $studentId-$activityId")
      completionPercentage = percentage
      loadFromDatabase(activityId, studentId)
  }


  //this state would be loaded from some persistent storage
  var payload: XmlPayload = new XmlPayload {}
  var score: Option[Double] = None
  var completionPercentage: Double = _


  override def receiveCommand = {
    case Get =>
      sender() ! ActivityData(studentId, activityId, payload, score)

    case Update(newPayload, newScore) =>
      updateDatabase(payload, score)
      persist(ActivityUpdated(studentId, activityId, calculateCompletion(payload))) { e =>
        //perform side-effecting operation
        payload = newPayload
        score = newScore
        completionPercentage = e.completionPercentage
        context.system.eventStream.publish(e)
      }

  }



  import scala.concurrent.Future
  private def updateDatabase(payload: XmlPayload, score: Option[Double]): Future[Boolean] = {
    //save the information in the write database
    Future.successful(true)
  }

  private def loadFromDatabase(activityId: Long, studentId: Long): Future[Boolean] = {
    //load payload and score from database
    Future.successful(true)
  }

  def calculateCompletion(payload: XmlPayload): Double = 20
}




object StudentGradeBook {

  case object LoadGradeBook

  def props(studentId: Long) = Props(new StudentGradeBook(studentId))
}

//This could be persistent as well
class StudentGradeBook(studentId: Long) extends Actor {

  import StudentGradeBook._
  //state
  var gradeBookState: Any = _

  context.system.eventStream.subscribe(self, classOf[Activity.ActivityUpdated])

  override def receive = {
    case Activity.ActivityUpdated(id, activityId, completionPercentage) if studentId == id =>
      println(s">>>>>> student grade book $studentId $activityId")
      //update the grade book state

    case LoadGradeBook =>
      //have access to updated state and can immediately respond back

  }

}



