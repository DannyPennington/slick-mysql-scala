package day7
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


object Main extends App {
  val db = Database.forConfig("mysqlDB")
  val peopleTable = TableQuery[People]

  val dropPeopleCmd = DBIO.seq(peopleTable.schema.drop)
  val initPeopleCmd = DBIO.seq(peopleTable.schema.create)

  def dropDB = {
    val dropFuture = Future{db.run(dropPeopleCmd)}
    Await.result(dropFuture, Duration.Inf).andThen {
      case Success(_) => initialisePeople
      case Failure(error) => println(s"Dropping failed due to: ${error.getMessage}"); initialisePeople
    }
  }

  def initialisePeople = {
    val setupFuture = Future{db.run(initPeopleCmd)}
    Await.result(setupFuture, Duration.Inf).andThen {
      case Success(_) => runQuery
      case Failure(error) => println(s"Initialising table failed due to: ${error.getMessage}")
    }
  }

  def runQuery = {
    val insertPeople = Future {
      val query = peopleTable ++= Seq(
        (1,"Jack", "Wood", 35, "jack@fakemail.com"),
        (2,"Jason", "Cats", 29, "jason@fakemail.com")
      )
      println(query.statements.head)
      db.run(query)
    }
    Await.result(insertPeople, Duration.Inf).andThen {
      case Success(_) => listPeople
      case Failure(error) => println(s"Something went wrong: ${error.getMessage}")
    }
  }

  def listPeople = {
    val queryFuture = Future {
      db.run(peopleTable.result).map(_.foreach {
        case (id, fName, lName, age, email) => println(s"$id $fName $lName $age $email")
      })
    }
    Await.result(queryFuture, Duration.Inf).andThen {
      case Success(_) => db.close()
      case Failure(error) => println(s"Listing failed: ${error.getMessage}")
    }
  }

  //def listByfName(fname:String) = {
  //  val queryFuture = Future {
  //    db.run(peopleTable.result).map()
  //  }
  //}

  def updateByID(id:Int, toUpdate: String, item:String) = {
    val updateFuture = Future {
      toUpdate match {
        case "fName" =>
          val query = for {i <- peopleTable if i.id === id} yield i.fName
          db.run(query.update(item))
        case "lName" =>
          val query = for {i <- peopleTable if i.id === id} yield i.lName
          db.run(query.update(item))
        case "email" =>
          val query = for {i <- peopleTable if i.id === id} yield i.email
          db.run(query.update(item))
    } }
    Await.result(updateFuture, Duration.Inf).andThen {
      case Success(_) => println("Updated")
      case Failure(error) => println(s"Update failed: ${error.getMessage}")
    }
  }

  updateByID(1,"email", "steve@mail.com")
  Thread.sleep(1000)
  listPeople
  Thread.sleep(5000)

}
