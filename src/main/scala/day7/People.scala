package day7
import slick.jdbc.MySQLProfile.api._

class People(tag: Tag) extends Table[(Int,String,String,Int,String)](tag, "PEOPLE"){
  def id = column[Int]("Per_ID", O.PrimaryKey, O.AutoInc)
  def fName = column[String]("Per_fName")
  def lName = column[String]("Per_lName")
  def age = column[Int]("Per_age")
  def email = column[String]("Per_email")
  def * = (id,fName,lName,age,email)
}
