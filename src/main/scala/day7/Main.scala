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

  def addPerson(fName: String, lName: String, age: Int, email: String, id: Int = 0) = {
    val data = Iterable(id, fName, lName, age, email)
    val addFuture = Future {
      val add = peopleTable ++= Seq(
        (id,fName,lName,age,email)
      )
      db.run(add)
    }
    Await.result(addFuture, Duration.Inf).andThen {
      case Success(_) => println("Successfully added")
      case Failure(error) => println(s"Adding failed: ${error.getMessage}")
    }
  }

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

  def updateAgeByID(id: Int, item: Int) = {
    val updateFuture = Future {
      val query = for {i <- peopleTable if i.id === id} yield i.age
      db.run(query.update(item))
    }
    Await.result(updateFuture, Duration.Inf).andThen {
      case Success(_) => println("Updated")
      case Failure(error) => println(s"Updating failed: ${error.getMessage}")
    }
  }

  def deleteByID(id: Int) = {
    val deleteFuture = Future {
      val query = peopleTable.filter(_.id === id)
      db.run(query.delete)
    }
    Await.result(deleteFuture, Duration.Inf).andThen {
      case Success(_) => println("Item successfully deleted")
      case Failure(error) => println(s"Deletion failed: ${error.getMessage}")
    }
  }

  def searchByID(id: Int) = {
    val searchFuture = Future {
      val query = peopleTable.filter(_.id === id)
      db.run(query.result).map(_.foreach {
        case (id, fName, lName, age, email) => println(s"ID: $id \nName: $fName $lName\nAge: $age \nEmail: $email")
        case _ => println("No results found!")
      })
    }
    Await.result(searchFuture, Duration.Inf).andThen {
      case Success(_) => println("")
      case Failure(error) => println(s"Search failed: ${error.getMessage}")
    }
  }

  def countPeople() = {
    val countFuture = Future {
      db.run(peopleTable.length.result)
    }
    Await
  }



  countPeople()
  Thread.sleep(5000)

}
