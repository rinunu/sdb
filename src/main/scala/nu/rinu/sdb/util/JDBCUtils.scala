package nu.rinu.sdb.util

import java.sql.{Types, ResultSetMetaData, ResultSet, PreparedStatement}
import java.util.Date
import javax.sql.DataSource


trait JDBCUtils {

  /**
   * get value from resultSet at index.
   *
   * @param index 1 to n
   */
  def getValue(meta: ResultSetMetaData, resultSet: ResultSet, index: Int): Option[Any] = {
    val i = index
    val _type = meta.getColumnType(i)

    Option(_type match {
      case Types.NULL =>
        null
      case Types.INTEGER | Types.DECIMAL | Types.TINYINT | Types.SMALLINT =>
        resultSet.getInt(i)
      case Types.BIGINT =>
        resultSet.getLong(i)
      case Types.REAL =>
        resultSet.getDouble(i)
      case Types.BIT =>
        resultSet.getBoolean(i)
      case Types.VARCHAR | Types.LONGVARCHAR | Types.CHAR =>
        resultSet.getString(i)
      case Types.DATE =>
        // util.Date にしないと、 Json 化する時に処理してくれなかった
        val opt = Option(resultSet.getDate(i))
        opt.map(t => new Date(t.getTime)).orNull
      case Types.TIMESTAMP =>
        val opt = Option(resultSet.getTimestamp(i))
        opt.map(t => new Date(t.getTime)).orNull
    })
  }
}

trait JDBCImplicits {
  implicit def richPreparedStatement(impl: PreparedStatement) =
    new RichPreparedStatement(impl)

  implicit def richResultSet(impl: ResultSet) =
    new RichResultSet(impl)

  implicit def richDataSource(a: DataSource) = new RichDataSource(a)
}

object JDBCUtils extends JDBCUtils with JDBCImplicits
