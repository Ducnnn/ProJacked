package com.projacked.app.fakes

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** A [Clock] whose date the test sets. */
class TestClock(var date: LocalDate) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = this

    override fun instant(): Instant = date.atStartOfDay(ZoneOffset.UTC).toInstant()
}
