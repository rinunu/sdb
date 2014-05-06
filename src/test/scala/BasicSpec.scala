import java.sql.Connection
import nu.rinu.sdb.{Convention, Dao}
import nu.rinu.sdb.ops.BasicOps
import nu.rinu.sdb.util.SimpleDBConvention
import org.apache.derby.jdbc.EmbeddedDataSource
import org.scalatest.FunSuite
import nu.rinu.sdb.Implicits._
import scala.util.Random

case class Foo(id: Int, n: Int, s: String)

case class MultiId(id0: Int, id1: Int, s: String)

class MyDao[MODEL, KEY](convention: Convention[MODEL, KEY]) extends Dao(convention) with BasicOps

object FooDao extends MyDao(new SimpleDBConvention[Foo, Int])

object MultiIdDao extends MyDao[MultiId, (Int, Int)](new SimpleDBConvention[MultiId, (Int, Int)]) with BasicOps

class BasicSpec extends FunSuite {

  private def createFooTable()(implicit c: Connection) {
    c.prepareStatement( """CREATE TABLE foo
          (id INT PRIMARY KEY NOT NULL,
           s VARCHAR(100) NOT NULL,
           n INT NOT NULL)""") { stmt =>
      stmt.execute()
    }
  }

  private def createMultiIdTable()(implicit c: Connection) {
    c.prepareStatement( """CREATE TABLE multi_id
          (id0 INT NOT NULL,
           id1 INT NOT NULL,
           s VARCHAR(100) NOT NULL,
           PRIMARY KEY (id0, id1))""") { stmt =>
      stmt.execute()
    }
  }

  private def createBarTable()(implicit c: Connection) {
    c.prepareStatement( """CREATE TABLE bar
          (id INT PRIMARY KEY NOT NULL,
           s VARCHAR(100) NOT NULL,
           n INT NOT NULL,
           bar_id INT NOT NULL
           )""") { stmt =>
      stmt.execute()
    }
  }

  test("insert") {
    withConnection { implicit c =>
      createFooTable()

      FooDao.insert(Foo(1, 123, "test"))

      assert(c.prepareStatement("select count(*) from foo") { stmt =>
        val rs = stmt.executeQuery()
        rs.next()
        rs.getInt(1)
      } == 1)
    }
  }

  test("select.find") {
    withConnection { implicit c =>
      createFooTable()
      FooDao.insert(Foo(1, 123, "test1"))

      assert(FooDao.find(1) == Some(Foo(1, 123, "test1")))
    }
  }

  test("select.list") {
    withConnection { implicit c =>
      createFooTable()
      FooDao.insert(Foo(1, 0, "test1"))
      FooDao.insert(Foo(2, 1, "test2"))
      FooDao.insert(Foo(3, 1, "test3"))

      assert(FooDao.where("n = ?", 1).list() == Seq(Foo(2, 1, "test2"), Foo(3, 1, "test3")))
    }
  }

  test("select.all") {
    withConnection { implicit c =>
      createFooTable()
      FooDao.insert(Foo(1, 0, "test1"))
      FooDao.insert(Foo(2, 1, "test2"))

      assert(FooDao.all.list() == Seq(Foo(1, 0, "test1"), Foo(2, 1, "test2")))
    }
  }

  test("count") {
    withConnection { implicit c =>
      createFooTable()
      FooDao.insert(Foo(1, 0, "test1"))
      FooDao.insert(Foo(2, 1, "test2"))
      FooDao.insert(Foo(3, 2, "test2"))

      assert(FooDao.where("s = ? ", "test2").count() == 2)
    }
  }

  test("delete") {
    withConnection { implicit c =>
      createFooTable()
      FooDao.insert(Foo(1, 0, "test1"))
      FooDao.insert(Foo(2, 1, "test2"))
      FooDao.insert(Foo(3, 2, "test2"))

      FooDao.where("s = ? ", "test2").delete()
      assert(FooDao.all.count() == 1)
    }
  }

  test("replace(mysql)") {
    pending
    withConnection { implicit c =>
      createFooTable()
      FooDao.insert(Foo(1, 0, "test1"))
      FooDao.replace(Foo(1, 1, "test2"))

      c.prepareStatement("select id, n, s from foo") { stmt =>
        val rs = stmt.executeQuery()
        assert(rs.next())
        assert((rs.getInt(1), rs.getInt(2), rs.getString(3)) ==(1, 1, "test2"))
        assert(!rs.next())
      }
    }
  }

  test("multi key select.find ") {
    withConnection { implicit c =>
      createMultiIdTable()
      MultiIdDao.insert(MultiId(1, 1, "test1"))
      MultiIdDao.insert(MultiId(1, 2, "test2"))

      assert(MultiIdDao.find((1, 1)) == Some(MultiId(1, 1, "test1")))
      assert(MultiIdDao.find((1, 2)) == Some(MultiId(1, 2, "test2")))
    }
  }


  //  "raw select" >> {
  //    withConnection { implicit c =>
  //      createFooTable()
  //      createBarTable()
  //
  //      FooTable.insert(Foo(1, 123, "test"))
  //      BarTable.insert(Bar(1, 123, "test", 1))
  //
  //      case class IdAndN(id: Int, n: Int)
  //      pending
  //    }
  //  }

  private def withConnection[A](f: Connection => A): A = {
    val dbName = "memory:mydb" + Random.nextInt()
    val ds = new EmbeddedDataSource

    ds.setDatabaseName(dbName)
    ds.setCreateDatabase("create")
    ds.withConnection(f)
  }


}
