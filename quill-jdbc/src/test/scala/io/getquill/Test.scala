//package io.getquill
//
//import language.experimental.macros
//import io.getquill.ast.Ident
//import io.getquill.jdbc.JdbcSource
//import java.sql.ResultSet
//
//case class Person(id: Long, name: String, surname: String, age: Int)
//case class Address(id: Long, personId: Long, streetId: Long, number: Int)
//case class Street(id: Long, name: String, city: String)
//
//object Test extends App {
//
//  object db extends JdbcSource {
//
//    def person = entity[Person]
//    def address = entity[Address]
//    def street = entity[Street]
//  }
//
//  def q1 = db.person.filter(p => p.name == p.surname).map(p => (p.name, p.age))
//
//  def a = q1.run
//
//  println(q1)
//  println(q1.run)
//
//  def q2 =
//    for {
//      p <- db.person if (p.name == null)
//      a <- db.address if (a.personId == p.id)
//    } yield {
//      a
//    }
//  println(q2)
//  println(q2.run)
//
//  def q3 =
//    for {
//      a <- q2
//      s <- db.street if (a.streetId == s.id)
//    } yield {
//      a
//    }
//  println(q3)
//  println(q3.run)
//
//  //  def byName = Partial {
//  //    (name: String) => db.person.filter(_.name == name)
//  //  }
//  //
//  //  def q4 = byName("jesus")
//  //
//  //  println(q4)
//  //  println(q4.run)
//
//  //  def byFullName = Partial {
//  //    (name: String, surname: String) => byName(name).filter(_.surname == surname)
//  //  }
//  //
//  //  def q5 = byFullName("flavio", "brasil")
//  //
//  //  println(q5)
//  //  println(q5.run)
//
//  //  def nameEqualsSurname = Partial {
//  //    (p: Person) => p.name == p.surname
//  //  }
//  //
//  //  println(nameEqualsSurname)
//  //
//  //  def q6 = db.person.filter(nameEqualsSurname(_))
//  //
//  //  println(q6)
//  //  println(q6.run)
//  //
//  //  def nameIs = Partial {
//  //    (p: Person, name: String) => p.name == name
//  //  }
//  //
//  //  def q7 = db.person.filter(nameIs(_, "flavio"))
//  //
//  //  println(q7)
//  //  println(q7.run)
//
//  def names = db.person.map(_.name)
//
//  def q8 = for {
//    name <- names
//    p <- db.person if (p.name == name)
//  } yield {
//    (p.name, p.age)
//  }
//
//  println(q8)
//  println(q8.run)
//
//  def q9 = db.address.map(_.personId)
//
//  println(q9)
//  println(q9.run)
//
//  def q10 =
//    for {
//      p1 <- db.person
//      p2 <- db.person if (p1.name == p2.name)
//    } yield {
//      p2
//    }
//
//  println(q10)
//  println(q10.run)
//
//  def personAndAddress =
//    for {
//      p <- db.person
//      a <- db.address if (a.personId == p.id)
//    } yield {
//      (p, a)
//    }
//
//  def q11 =
//    for {
//      (pp, aa) <- personAndAddress
//      s <- db.street if (aa.streetId == s.id)
//    } yield {
//      (pp, aa, s.city)
//    }
//
//  println(q11)
//  println(q11.run)
//}
