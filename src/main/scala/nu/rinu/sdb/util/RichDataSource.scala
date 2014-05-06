package nu.rinu.sdb.util

import javax.sql.DataSource
import nu.rinu.util.IOUtils
import java.sql.Connection

class RichDataSource(val impl: DataSource) extends AnyVal {
  def withConnection[T](f: Connection => T): T = {
    IOUtils.using(impl.getConnection) { connection =>
      f(connection)
    }
  }
}
