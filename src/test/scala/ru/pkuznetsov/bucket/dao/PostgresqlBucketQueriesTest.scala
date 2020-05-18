package ru.pkuznetsov.bucket.dao

import org.scalatest.tagobjects.Slow
import ru.pkuznetsov.bucket.model.BucketEntry
import ru.pkuznetsov.core.dao.{DbTest, DbTestTag}

class PostgresqlBucketQueriesTest extends DbTest {

  test("delete bucket table", Slow, DbTestTag) {
    check(PostgresqlBucketQueries.deleteBucketTable)
  }

  test("insert bucket entry", Slow, DbTestTag) {
    check(PostgresqlBucketQueries.insertBucketEntry(BucketEntry(0, 23.67, Some("uunit"))))
  }

  test("select bucket", Slow, DbTestTag) {
    check(PostgresqlBucketQueries.selectBucket)
  }

}
