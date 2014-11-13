package me.loki2302.reports

import groovy.sql.Sql
import groovy.transform.builder.Builder

class CommitCountByTimeElementReport {
    void makeCommitCountByYear(Sql sql) {
    }

    void makeCommitCountByMonthOfYear(Sql sql) {
    }

    List<Row> makeCommitCountByDayOfWeek(Sql sql) {
        sql.rows('''
select
    dayofweek(C.date) as dayOfWeekNumber,
    to_char(C.date, 'DAY') as dayName,
    count(C.id) as commitCount
from Commits as C
group by dayOfWeekNumber, dayName
order by dayOfWeekNumber asc
''').collect {
            Row.builder()
                    .dayOfWeek(it.dayOfWeekNumber)
                    .dayName(it.dayName)
                    .commitCount(it.commitCount)
                    .build()
        }
    }

    @Builder
    static class Row {
        int dayOfWeek
        String dayName
        long commitCount
    }

    void makeCommitCountByTimeOfDay(Sql sql) {
    }
}
