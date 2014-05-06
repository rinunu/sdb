package nu.rinu.sdb.ops

import com.typesafe.scalalogging.slf4j.Logging
import nu.rinu.sdb.Convention

trait OpsBase extends Logging {
  type MODEL
  type KEY

  val convention: Convention[MODEL, KEY]
}
