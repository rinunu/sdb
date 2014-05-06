package nu.rinu.util

import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => mirror}
import reflect.ClassTag
import org.json4s
import org.json4s.{Formats, Extraction}
import org.json4s.JsonAST.{JValue, JObject}
import json4s.native.{Serialization, JsonMethods}
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import scala.util.control.NonFatal

trait CaseClassUtils {

  /**
    */
  def from[T: TypeTag](map: Map[String, Any]): T = {
    // http://stackoverflow.com/questions/13812172/how-can-i-create-an-instance-of-a-case-class-with-constructor-arguments-with-no
    // macro を使うとなんだか不安定なので、 json で。。

    implicit val formats = org.json4s.DefaultFormats

    val jvalue = Extraction.decompose(map)
    try {
      extract[T](jvalue)
    } catch {
      case NonFatal(e) =>
        throw new RuntimeException("failed: " + map, e)
    }
  }

  private def extract[T: TypeTag](jvalue: JValue)(implicit formats: Formats) = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    implicit val ct = ClassTag[T](mirror.runtimeClass(implicitly[TypeTag[T]].tpe))
    jvalue.extract[T]
  }

  def describe[T <: AnyRef](obj: T): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats

    val jvalue = Extraction.decompose(obj)
    // Extraction.extract[Map[String, Any]](jvalue) // これだとダメ。。 
    jvalue.asInstanceOf[JObject].values // TODO スマートな方法ないのかな。。
  }

  /**
    */
  def toMap[A: TypeTag](obj: A): Map[String, Any] = {
    val t = typeOf[A]
    implicit val ct = ClassTag[A](obj.getClass)

    val fields = for {sym <- t.members.sorted
                      if sym.isTerm
                      term = sym.asTerm
                      if term.isCaseAccessor && term.isGetter
    } yield term

    val mirror = runtimeMirror(getClass.getClassLoader)
    val ojbM = mirror.reflect(obj)

    val nameValues = fields.map { field =>
      val name = field.name.decoded
      val value = ojbM.reflectField(field).get
      name -> value
    }
    nameValues.toMap
  }
}


object CaseClassUtils extends CaseClassUtils {

}