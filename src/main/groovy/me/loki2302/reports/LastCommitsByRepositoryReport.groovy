package me.loki2302.reports

import groovy.sql.Sql
import groovy.transform.builder.Builder

class LastCommitsByRepositoryReport {
    List<Repository> make(Sql sql, int lastCommits) {
        def rows = sql.rows("""
select R.name, C.sha, C.date
from Repositories as R
join Commits as C on C.repositoryId = R.id
where C.id in (
    select top $lastCommits C.id
    from Commits as C
    where C.repositoryId = R.id
    order by date desc
)
order by R.name asc, C.date desc
""")

        rows.groupBy({
            it.name
        }).collect { key, value ->
            Repository.builder()
                    .name(key)
                    .commits(value.collect {
                        Commit.builder()
                                .sha(it.sha)
                                .date(it.date)
                    }).build()
        }
    }

    @Builder
    static class Repository {
        String name
        List<Commit> commits
    }

    @Builder
    static class Commit {
        String sha
        Date date
    }
}
