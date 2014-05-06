package nu.rinu.sdb

import java.sql.ResultSet

trait TableInfo {
  type ColumnName = String

  val tableName: String

  val primaryKeyNames: Seq[ColumnName]

  val columnNames: Seq[ColumnName]
}

/**
 * Define mapping rules of DB to Scala data.
 */
trait Convention[MODEL, KEY] extends TableInfo {
  def getPrimaryKeyValues(a: KEY): Seq[Any]

  def createObject(rs: ResultSet): MODEL

  def getColumnValueMap(a: MODEL): Map[ColumnName, Any]

  //  def fieldToDBColumn(fieldName: String): ColumnName

  //  def dbColumnToField(columnName: ColumnName): String

  //  def getPrimaryKeyValues(a: MODEL): Seq[Any]
}