package me.loki2302.reports

import groovy.sql.Sql
import groovy.transform.builder.Builder

class CommitCountByTimePeriodReport {
    List<Row> makeCommitCountByYear(Sql sql) {
        sql.rows('''
select
    to_char(C.date, 'YYYY') as yearString,
    count(C.id) as commitCount
from Commits as C
group by yearString
order by yearString desc
''').collect {
            Row.builder()
                    .timePeriod(it.yearString)
                    .commitCount(it.commitCount)
                    .build()
        }
    }

    List<Row> makeCommitCountByYearAndMonth(Sql sql) {
        sql.rows('''
select
    to_char(C.date, 'YYYY-MM') as yearAndMonthString,
    count(C.id) as commitCount
from Commits as C
group by yearAndMonthString
order by yearAndMonthString desc
''').collect {
            Row.builder()
                    .timePeriod(it.yearAndMonthString)
                    .commitCount(it.commitCount)
                    .build()
        }
    }

    List<Row> makeCommitCountByYearMonthAndDay(Sql sql) {
        sql.rows('''
select
    to_char(C.date, 'YYYY-MM-DD') as yearMonthAndDayString,
    count(C.id) as commitCount
from Commits as C
group by yearMonthAndDayString
order by yearMonthAndDayString desc
''').collect {
            Row.builder()
                    .timePeriod(it.yearMonthAndDayString)
                    .commitCount(it.commitCount)
                    .build()
        }
    }

    @Builder
    static class Row {
        String timePeriod
        long commitCount
    }
}
