package me.loki2302
import groovy.sql.Sql
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
        assertEquals('docpad-experiment', rows[0].name)
        assertEquals(121, rows[0].commits)
        assertEquals('spring-web-app-experiment', rows[1].name)
        assertEquals(102, rows[1].commits)
        assertEquals('nodejs-app-experiment', rows[2].name)
        assertEquals(93, rows[2].commits)
    }

    @Test
    void repositoriesWithFirstAndLastCommitDatesReportWorks() {
        def repositoriesWithFirstAndLastCommitDatesReport = new RepositoriesWithFirstAndLastCommitDatesReport()
        def rows = repositoriesWithFirstAndLastCommitDatesReport.make(sql)
        assertEquals('DotNetOpenAuth.GoogleOAuth2', rows[0].name)
        assertEquals(Timestamp.valueOf('2013-02-28 23:55:11.0'), rows[0].firstCommitDate)
        assertEquals(Timestamp.valueOf('2013-07-07 00:35:00.0'), rows[0].lastCommitDate)
        assertEquals('abstract-mvc-experiment', rows[1].name)
        assertEquals(Timestamp.valueOf('2013-12-04 10:24:24.0'), rows[1].firstCommitDate)
        assertEquals(Timestamp.valueOf('2013-12-04 10:50:00.0'), rows[1].lastCommitDate)
        assertEquals('android-custom-layout-loader-experiment', rows[2].name)
        assertEquals(Timestamp.valueOf('2013-11-28 09:05:12.0'), rows[2].firstCommitDate)
        assertEquals(Timestamp.valueOf('2013-11-28 09:12:24.0'), rows[2].lastCommitDate)
    }
}
