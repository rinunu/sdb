package nu.rinu.sdb.util

import java.sql.{Timestamp, PreparedStatement}
import java.util.Date

class RichPreparedStatement(val impl: PreparedStatement) extends AnyVal {
  def apply[T](f: PreparedStatement => T): T = {
    f(impl)
  }

  def setValues(values: Seq[Any]) {
    values.zipWithIndex.foreach {
      case (value, i) =>
        value match {
          case v: String => impl.setString(i + 1, v)
          case v: Double => impl.setDouble(i + 1, v)
          case v: Date =>
            // TODO DB の型によって型を変えるべき? 
            // java.sql.Date is date-only, regardless of the type of the column.
            impl.setTimestamp(i + 1, new Timestamp(v.getTime))
          case v: Int => impl.setInt(i + 1, v)
          case v: Long => impl.setLong(i + 1, v)
          case _ => sys.error("unsupported type: " + value)
        }
    }
  }
}
