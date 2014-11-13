package me.loki2302.reports

import groovy.sql.Sql
import groovy.transform.builder.Builder

class RepositoriesWithFirstAndLastCommitDatesReport {
    List<Row> make(Sql sql) {
        sql.rows('''
select
    R.name as name,
    (select min(C.date) from Commits as C where C.repositoryId = R.id) as firstCommitDate,
    (select max(C.date) from Commits as C where C.repositoryId = R.id) as lastCommitDate
from Repositories as R
order by name asc''').collect {
            Row.builder()
                    .name(it.name)
                    .firstCommitDate(it.firstCommitDate)
                    .lastCommitDate(it.lastCommitDate)
                    .build()
        }
    }

    @Builder
    static class Row {
        String name
        Date firstCommitDate
        Date lastCommitDate
    }
}
