package me.loki2302.reports

import groovy.sql.Sql
import groovy.transform.builder.Builder

class TopRepositoriesByCommitCountReport {
    List<Row> make(Sql sql, int top) {
        def rows = sql.rows("""
select top $top
    R.name,
    (select count(C.id) from Commits as C where C.repositoryId = R.id) as CommitCount
from Repositories as R
order by CommitCount desc
""")

        rows.collect {
            Row.builder().name(it.name).commits(it.CommitCount).build()
        }
    }

    @Builder
    static class Row {
        String name
        long commits
    }
}
