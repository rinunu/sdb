package nu.rinu.sdb

import nu.rinu.sdb.ops.OpsBase


abstract class Dao[MODEL2, KEY2](val convention: Convention[MODEL2, KEY2]) extends OpsBase {
  type KEY = KEY2
  type MODEL = MODEL2
}
