package me.loki2302.reports

import groovy.sql.Sql
import groovy.transform.builder.Builder

class CommitCountByTimePeriodReport {
    List<Row> makeCommitCountByYear(Sql sql) {
        // TODO
        throw new RuntimeException("Not implemented")
    }

    List<Row> makeCommitCountByYearAndMonth(Sql sql) {
        sql.rows('''
select
    to_char(C.date, 'YYYY-MM') as yearAndMonthString,
    count(C.id) as commitCount
from Commits as C
group by YearAndMonthString
order by YearAndMonthString desc
''').collect {
            Row.builder()
                    .timePeriod(it.yearAndMonthString)
                    .commitCount(it.commitCount)
                    .build()
        }
    }

    List<Row> makeCommitCountByYearMonthAndDay(Sql sql) {
        // TODO
        throw new RuntimeException("Not implemented")
    }

    @Builder
    static class Row {
        String timePeriod
        long commitCount
    }
}
