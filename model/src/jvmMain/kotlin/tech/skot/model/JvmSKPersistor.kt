package tech.skot.model

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import tech.skot.model.persist.PersistDb

class JvmSKPersistor(dbFilename: String) : CommonSKPersistor() {
    override val db =
        PersistDb(
            JdbcSqliteDriver(
                JdbcSqliteDriver.IN_MEMORY,
            ).apply { PersistDb.Schema.create(this) },
        )
}
