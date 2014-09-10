package models

object Models {

  object XmlPayload { def apply() = new XmlPayload {}}
  trait XmlPayload

  trait StudentDomainService {

    def loadActivities(studentId: Long, courseId: Long): Seq[Long] = Seq(1, 2, 3)
  }

}
