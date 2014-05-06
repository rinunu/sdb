package nu.rinu.sdb.ops

import java.sql.Connection
import nu.rinu.sdb.Implicits._
import scala.collection.mutable
import nu.rinu.util.CaseClassUtils
import scala.reflect.runtime.universe._

/**
  */
trait BasicOps extends OpsBase {
  type SQL = String

  class Query(whereSQL: SQL, keys: Seq[Any]) {
    def count()(implicit connection: Connection): Int = {
      val from = convention.tableName
      val sql = s"select count(*) from $from $whereSQL"
      logger.debug(sql)
      connection.prepareStatement(sql) { s =>
        s.setValues(keys)
        val rs = s.executeQuery()
        rs.next()
        rs.getInt(1)
      }
    }

    def single()(implicit connection: Connection): MODEL = {
      val sql = createSelectSql() + s" $whereSQL"
      executeQuery(sql, keys).get
    }

    def list()(implicit connection: Connection): Seq[MODEL] = {
      val sql = createSelectSql() + s" $whereSQL"
      logger.debug(sql)
      connection.prepareStatement(sql) { s =>
        s.setValues(keys)
        val rs = s.executeQuery()
        val result = mutable.ArrayBuffer[MODEL]()
        while (rs.next()) {
          result += convention.createObject(rs)
        }
        result.toSeq
      }
    }

    def singleForUpdate()(implicit con: Connection): MODEL = ???

    def delete()(implicit connection: Connection): Int = {
      val from = convention.tableName
      val sql = s"delete from $from $whereSQL"
      logger.debug(sql)
      connection.prepareStatement(sql) { s =>
        s.setValues(keys)
        s.executeUpdate()
      }
    }
  }

  def where(f: MODEL => Any): Query = ???

  def where(where: String, params: Any*): Query = {
    new Query("where " + where, params)
  }

  def find(key: KEY)(implicit connection: Connection): Option[MODEL] = {
    val keyNames = convention.primaryKeyNames
    val keys = convention.getPrimaryKeyValues(key)
    val whereSQL = "where " + keyNames.map(_ + " = ?").mkString(" and ")

    executeQuery(createSelectSql() + s" $whereSQL", keys)
  }

  def all: Query = {
    new Query("", Seq.empty)
  }

  def replace(t: MODEL)(implicit connection: Connection): Int = {
    val nameValueMap = convention.getColumnValueMap(t)

    val columnNames = nameValueMap.keys.mkString(",")

    val tableName = convention.tableName
    val placeHolders = nameValueMap.map(a => "?").mkString(",")
    val sql = s"REPLACE INTO $tableName ($columnNames) VALUES ($placeHolders)"
    logger.debug(sql)

    connection.prepareStatement(sql) { statement =>
      statement.setValues(nameValueMap.values.toSeq)
      statement.executeUpdate()
    }
  }

  def insert(t: MODEL)(implicit connection: Connection): Int = {
    val nameValueMap = convention.getColumnValueMap(t)

    val columnNames = nameValueMap.keys.mkString(",")

    val tableName = convention.tableName
    val placeHolders = nameValueMap.map(a => "?").mkString(",")
    val sql = s"INSERT INTO $tableName ($columnNames) VALUES ($placeHolders)"
    logger.debug(sql)

    connection.prepareStatement(sql) { statement =>
      statement.setValues(nameValueMap.values.toSeq)
      statement.executeUpdate()
    }
  }

  @deprecated
  def findOne(key: KEY)(implicit connection: Connection): MODEL = {
    find(key).get
  }

  private def executeQuery(sql: String, keys: Seq[Any])
                          (implicit connection: Connection): Option[MODEL] = {
    connection.prepareStatement(sql) { s =>
      logger.debug(sql)

      s.setValues(keys)
      val rs = s.executeQuery()
      if (rs.next()) {
        Some(convention.createObject(rs))
      } else {
        None
      }
    }
  }

  private def createSelectSql(): String = {
    val columnNames = convention.columnNames.mkString(",")

    val tableName = convention.tableName

    s"select $columnNames from $tableName"
  }
}
