package me.loki2302

import groovy.sql.Sql
import me.loki2302.reports.TopRepositoriesByCommitCountReport

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
        println(rows.collect {
            "${it.name} - ${it.commits}"
        }.join('\n'))
    }
}
