package nu.rinu.sdb.util

import com.google.common.base.CaseFormat
import nu.rinu.sdb.Convention
import scala.reflect.runtime.universe._
import java.sql.ResultSet
import JDBCUtils._
import nu.rinu.util.CaseClassUtils

/**
  */
class SimpleDBConvention[MODEL: TypeTag, KEY: TypeTag] extends Convention[MODEL, KEY] {

  import CaseFormat._

  val tableName: String = {
    val tpe = implicitly[TypeTag[MODEL]].tpe
    LOWER_CAMEL.to(LOWER_UNDERSCORE, tpe.typeSymbol.name.decoded)
  }

  //  def getPrimaryKeyValues(a: MODEL): Seq[Any] = {
  //    // case class の最初の要素を key とみなします
  //    Seq(a.asInstanceOf[Product].productElement(0))
  //  }

  lazy val primaryKeyNames: Seq[ColumnName] = {
    //    // case class のコンストラクタの最初のパラメータをキーとみなします
    //    val tpe = implicitly[TypeTag[MODEL]].tpe
    //    val constructor = tpe.member(nme.CONSTRUCTOR).asMethod
    //    Seq(constructor.paramss(0)(0).name.decoded)

    columnNames.take(primaryKeyCount)
  }

  lazy val columnNames: Seq[ColumnName] = {
    val t = typeOf[MODEL]

    for {sym <- t.members.sorted
         if sym.isTerm
         term = sym.asTerm
         if term.isCaseAccessor && term.isGetter
    } yield term.name.decoded
  }

  def getPrimaryKeyValues(a: KEY): Seq[Any] = {
    if (primaryKeyCount == 1) {
      Seq(a)
    } else {
      val p = a.asInstanceOf[Product]
      p.productIterator.toSeq
    }
  }

  def createObject(resultSet: ResultSet): MODEL = {
    CaseClassUtils.from[MODEL](resultSet.getMap.map { case (key, value) =>
      (key.toLowerCase, value)
    })
  }

  def getColumnValueMap(a: MODEL): Map[ColumnName, Any] = {
    CaseClassUtils.toMap(a).map { case (key, value) =>
      (fieldToDBColumn(key), value)
    }
  }

  private lazy val primaryKeyCount: Int = {
    val t = typeOf[KEY]
    if (t <:< typeOf[Product]) {
      // count _n of tuple
      (for {sym <- t.members
            if sym.isTerm
            term = sym.asTerm
            if term.isVal && term.name.decoded.startsWith("_")
      } yield term).size
    } else {
      1
    }
  }

  private def fieldToDBColumn(fieldName: String): ColumnName = {
    LOWER_CAMEL.to(LOWER_UNDERSCORE, fieldName)
  }

  private def dbColumnToField(columnName: ColumnName): String = {
    LOWER_UNDERSCORE.to(LOWER_CAMEL, columnName)
  }

}
