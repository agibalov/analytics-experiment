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

        runTopRepositoriesByCommitCountReport(sql)
        runRepositoriesWithFirstAndLastCommitDatesReport(sql)
        runLastCommitsByRepositoryReport(sql)
        runCommitCountByTimePeriodReport(sql)
        runCommitCountByTimeElementReport(sql)
    }

    private static void runCommitCountByTimePeriodReport(Sql sql) {
        def commitCountByTimePeriodReport = new CommitCountByTimePeriodReport()
        def rows = commitCountByTimePeriodReport.makeCommitCountByYearMonthAndDay(sql)
        println()
        println rows.collect {
            "${it.timePeriod} - ${it.commitCount}"
        }.join('\n')

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
    }

    private static void runCommitCountByTimeElementReport(Sql sql) {
        def rows
        def commitCountByTimeElementReport = new CommitCountByTimeElementReport()
        rows = commitCountByTimeElementReport.makeCommitCountByDayOfWeek(sql)
        println()
        println rows.collect {
            "${it.dayOfWeek} - ${it.dayName} - ${it.commitCount}"
        }.join('\n')
    }

    private static void runLastCommitsByRepositoryReport(Sql sql) {
// TODO: get last 3 commits for each repository (master -> details)
        def lastCommitsByRepositoryReport = new LastCommitsByRepositoryReport()
        def repositories = lastCommitsByRepositoryReport.make(sql, 3)
        println()
        println repositories.take(3).collect { repository ->
            "${repository.name}\n" + repository.commits.collect { commit ->
                "  $commit.date $commit.sha"
            }.join('\n')
        }.join('\n')
    }

    private static void runRepositoriesWithFirstAndLastCommitDatesReport(Sql sql) {
        def repositoriesWithFirstAndLastCommitDatesReport = new RepositoriesWithFirstAndLastCommitDatesReport()
        def rows = repositoriesWithFirstAndLastCommitDatesReport.make(sql)
        println()
        println(rows.take(3).collect {
            "${it.name} - ${it.firstCommitDate} - ${it.lastCommitDate}"
        }.join('\n'))
    }

    private static void runTopRepositoriesByCommitCountReport(Sql sql) {
        def topRepositoriesByCommitCountReport = new TopRepositoriesByCommitCountReport()
        def rows = topRepositoriesByCommitCountReport.make(sql, 3)
        println()
        println(rows.collect {
            "${it.name} - ${it.commits}"
        }.join('\n'))
    }
}
