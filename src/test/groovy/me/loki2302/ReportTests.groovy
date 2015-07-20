package me.loki2302
import groovy.sql.Sql
import me.loki2302.reports.LastCommitsByRepositoryReport
import me.loki2302.reports.RepositoriesWithFirstAndLastCommitDatesReport
import me.loki2302.reports.TopRepositoriesByCommitCountReport
import org.junit.BeforeClass
import org.junit.Test

import java.sql.Timestamp

import static org.junit.Assert.assertEquals

class ReportTests {
    static Sql sql

    @BeforeClass
    static void setUpDatabase() {
        sql = Sql.newInstance('jdbc:hsqldb:mem:test', 'sa', '', 'org.hsqldb.jdbc.JDBCDriver')
        def databaseFacade = new DatabaseFacade(sql)
        databaseFacade.init()

        def dataLoader = new DataLoader()
        dataLoader.loadData(databaseFacade)
    }

    @Test
    void topRepositoriesByCommitCountWorks() {
        def topRepositoriesByCommitCountReport = new TopRepositoriesByCommitCountReport()
        def rows = topRepositoriesByCommitCountReport.make(sql, 3)
        assertEquals('nodejs-angular-app', rows[0].name)
        assertEquals(275, rows[0].commits)
        assertEquals('nodejs-app-experiment', rows[1].name)
        assertEquals(213, rows[1].commits)
        assertEquals('nodejs-experiment', rows[2].name)
        assertEquals(177, rows[2].commits)
    }

    @Test
    void repositoriesWithFirstAndLastCommitDatesReportWorks() {
        def repositoriesWithFirstAndLastCommitDatesReport = new RepositoriesWithFirstAndLastCommitDatesReport()
        def rows = repositoriesWithFirstAndLastCommitDatesReport.make(sql)
        assertEquals('analytics-experiment', rows[0].name)
        assertEquals(Timestamp.valueOf('2014-11-12 10:50:05.0'), rows[0].firstCommitDate)
        assertEquals(Timestamp.valueOf('2014-11-19 17:42:46.0'), rows[0].lastCommitDate)
    }

    @Test
    void lastCommitsByRepositoryReportWorks() {
        def lastCommitsByRepositoryReport = new LastCommitsByRepositoryReport()
        def repositories = lastCommitsByRepositoryReport.make(sql, 3)
        assertEquals('analytics-experiment', repositories[0].name)
        assertEquals(Timestamp.valueOf('2014-11-19 17:42:46.0'), repositories[0].commits[0].date)
        assertEquals('c77e7b32e3d30e7d082981116b86db96e2af0300', repositories[0].commits[0].sha)
        assertEquals(Timestamp.valueOf('2014-11-19 17:33:17.0'), repositories[0].commits[1].date)
        assertEquals('454c71a08137436efae61ba26e6d767db3ef3ffb', repositories[0].commits[1].sha)
        assertEquals(Timestamp.valueOf('2014-11-14 10:15:14.0'), repositories[0].commits[2].date)
        assertEquals('a325e4e3fdb8336011aab60c344d64b94d9f8396', repositories[0].commits[2].sha)
    }
}
