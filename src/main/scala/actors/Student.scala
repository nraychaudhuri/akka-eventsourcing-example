package actors

import akka.actor.{ActorRef, Props, Actor}

import models.Models._


object Student {

  //commands
  case class CourseAssignment(studentId: Long, courseId: Long)
  case class LoadActivity(studentId: Long, activityId: Long)
  case class UpdateActivity(studentId: Long, activityId: Long, p: XmlPayload, score: Option[Double] = None)

  //events
  case class CourseIsAssigned(studentId: Long, courseId: Long)
  case class ActivityAccessed(studentId: Long, activityId: Long)

  def props = Props(new Student)
}


//This is an aggregate represents everything about Student, its gradebook and activities
//Activities are represented as an child actor (in-memory model). This extends
//StudenDomainService trait to access all the domain specific services
//Right now this is quite simple as all the events generated to default Akka event stream
class Student extends Actor with StudentDomainService {

  import Student._


  //we can directly access db to access all the activity stuff but lets take this one step further and have them as
  //as a child actors to student
  //Ideally it should be student -> course[1..*] -> activity[1..*] Here it is simplified
  override def receive = {

    case CourseAssignment(studentId, courseId: Long) =>
      loadActivities(studentId, courseId).foreach(createActivity)
      //create grade book for the course
      context.actorOf(StudentGradeBook.props(studentId), "grade-book")
      //firing event
      context.system.eventStream.publish(CourseIsAssigned(studentId, courseId))

    case LoadActivity(studentId, activityId) =>
       lookUpActivity(activityId).foreach(_.forward(Activity.Get(studentId, activityId)))

       //firing event
       context.system.eventStream.publish(ActivityAccessed(studentId, activityId))

    case UpdateActivity(studentId, activityId, payload, score) =>
      lookUpActivity(activityId).foreach(_.forward(Activity.Update(studentId, activityId, payload, score)))
  }



  def lookUpActivity(activityId: Long): Option[ActorRef] = {
    context.child(s"activity-$activityId")
  }

  //creating each activity as a child actor
  def createActivity(activityId: Long): ActorRef = {
    context.actorOf(Activity.props, name=s"activity-$activityId")
  }
}


object Activity {

  case class Get(studentId: Long, activityId: Long)
  case class Update(studentId: Long, activityId: Long, p: XmlPayload, score: Option[Double] = None)
  case class ActivityData(studentId: Long, activityId: Long, p: XmlPayload, score: Option[Double])


  //event published whenever activity is changed.
  case class ActivityUpdated(studentId: Long, activityId: Long, completionPercentage: Double)


  def props = Props(new Activity)
}


//Represents each activity assigned to a student. This will be a stateful actor backed
//by some persistent journal.
//TODO: This should be persistentActor
class Activity extends Actor {

  import Activity._

  //this state would be loaded from some persistent storage
  var payload: XmlPayload = new XmlPayload {}
  var score: Option[Double] = None


  override def receive = {
    case Get(studentId, activityId) => sender() ! ActivityData(studentId, activityId, payload, score)
    case Update(studentId, activityId, newPayload, newScore) =>
      payload = newPayload
      score = newScore
      val completionPercentage = calculateCompletion(payload)
      context.system.eventStream.publish(ActivityUpdated(studentId, activityId, completionPercentage))
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



