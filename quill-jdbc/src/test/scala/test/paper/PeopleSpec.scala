package test.paper

import io.getquill.Source
import io.getquill.jdbc.JdbcSource
import test.Spec
import io.getquill.Queryable
import test.paper.ent.Person
import test.paper.ent.Couple

class PeopleSpec extends Spec {

  object peopleDB extends JdbcSource

  "Example 1 - diferences" in {

    def aaa = peopleDB[Couple]

    peopleDB[Person].flatMap(_ => aaa)

    def differences =
      for {
        c <- peopleDB[Person]
        w <- peopleDB[Person]
        m <- peopleDB[Person] if (c.her == w.name && c.him == m.name && w.age > m.age)
      } yield {
        (w.name, w.age - m.age)
      }

    println(peopleDB.run(differences))

    //    differences.run mustEqual List(("Alex", 5), ("Cora", 2))
  }

  //  "Example 2 - range simple" in {
  //
  //    def rangeSimple = Partial {
  //      (a: Int, b: Int) =>
  //        for {
  //          u <- peopleDB.people if (a <= u.age && u.age < b)
  //        } yield {
  //          u 
  //        }
  //    }
  //
  //    def r = rangeSimple(30, 40)
  //
  //    r.run mustEqual List(Person("Cora", 33), Person("Drew", 31))
  //  }

  //  "Example 3 - satisfies" in {
  //     for u in db.People do
  //                        if p u.Age then
  //                            yield u
  //     
  //    def satisfies =
  //      Partial {
  //        (p: Int => Boolean) =>
  //          for {
  //            u <- peopleDB.people if (p(u.age))
  //          } yield {
  //            u
  //          }
  //      }
  //  }
}
