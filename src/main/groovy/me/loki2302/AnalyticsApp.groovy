package me.loki2302

import groovy.sql.Sql
import me.loki2302.reports.*

class AnalyticsApp {
    static void main(String[] args) {
        runAnalytics()
    }

    static void runAnalytics() {
        def sql = Sql.newInstance('jdbc:hsqldb:mem:test', 'sa', '', 'org.hsqldb.jdbc.JDBCDriver')
        def databaseFacade = new DatabaseFacade(sql)
        databaseFacade.init()

        def dataLoader = new DataLoader()
        dataLoader.loadData(databaseFacade)

        def topRepositoriesByCommitCountReport = new TopRepositoriesByCommitCountReport()
        def rows = topRepositoriesByCommitCountReport.make(sql, 3)
        println()
        println(rows.collect {
            "${it.name} - ${it.commits}"
        }.join('\n'))

        // TODO: list all repositories with firstCommitDate and lastCommitDate, order by name
        def repositoriesWithFirstAndLastCommitDatesReport = new RepositoriesWithFirstAndLastCommitDatesReport()
        rows = repositoriesWithFirstAndLastCommitDatesReport.make(sql)
        println()
        println(rows.take(3).collect {
            "${it.name} - ${it.firstCommitDate} - ${it.lastCommitDate}"
        }.join('\n'))

        // TODO: get last 3 commits for each repository (master -> details)
        def lastCommitsByRepositoryReport = new LastCommitsByRepositoryReport()
        def repositories = lastCommitsByRepositoryReport.make(sql, 3)
        println()
        println repositories.take(3).collect { repository ->
            "${repository.name}\n" + repository.commits.collect { commit ->
                "  $commit.date $commit.sha"
            }.join('\n')
        }.join('\n')

        //
        def commitCountByTimePeriodReport = new CommitCountByTimePeriodReport()
        rows = commitCountByTimePeriodReport.makeCommitCountByYearAndMonth(sql)
        println()
        println rows.collect {
            "${it.timePeriod} - ${it.commitCount}"
        }.join('\n')

        rows = commitCountByTimePeriodReport.makeCommitCountByYear(sql)
        println()
        println rows.collect {
            "${it.timePeriod} - ${it.commitCount}"
        }.join('\n')

        //
        def commitCountByTimeElementReport = new CommitCountByTimeElementReport()
        rows = commitCountByTimeElementReport.makeCommitCountByDayOfWeek(sql)
        println()
        println rows.collect {
            "${it.dayOfWeek} - ${it.dayName} - ${it.commitCount}"
        }.join('\n')

        // TODO: list top 3 repositories with earliest firstCommitDate
        // TODO: list top 3 repositories with latest lastCommitDate
        // TODO: list top 3 repositories which have largest difference between firstCommitDate and lastCommitDate, specify first+last+difference
    }
}
