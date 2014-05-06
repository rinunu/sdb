package nu.rinu.sdb.util

import java.sql.ResultSet

class RichResultSet(val impl: ResultSet) extends AnyVal {
  def getMap: Map[String, Any] = {
    val meta = impl.getMetaData
    val keyValues =
      for {i <- 1 to meta.getColumnCount
           name = meta.getColumnName(i)
           _type = meta.getColumnType(i)
      } yield {
        (name, JDBCUtils.getValue(meta, impl, i).orNull)
      }
    keyValues.toMap
  }
}
