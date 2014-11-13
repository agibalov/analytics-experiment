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

    @Test
    void lastCommitsByRepositoryReportWorks() {
        def lastCommitsByRepositoryReport = new LastCommitsByRepositoryReport()
        def repositories = lastCommitsByRepositoryReport.make(sql, 3)
        assertEquals('DotNetOpenAuth.GoogleOAuth2', repositories[0].name)
        assertEquals(Timestamp.valueOf('2013-07-07 00:35:00.0'), repositories[0].commits[0].date)
        assertEquals('e9a120c2e975a61199e63756baec0aed995a316a', repositories[0].commits[0].sha)
        assertEquals(Timestamp.valueOf('2013-03-14 21:41:40.0'), repositories[0].commits[1].date)
        assertEquals('c66347bd8843df7bf83cd951cb5d68d3363854b2', repositories[0].commits[1].sha)
        assertEquals(Timestamp.valueOf('2013-03-14 21:36:07.0'), repositories[0].commits[2].date)
        assertEquals('be60f6f741687e5bdce7009fdf0b3c3f9b6480b7', repositories[0].commits[2].sha)

        assertEquals('abstract-mvc-experiment', repositories[1].name)
        assertEquals(Timestamp.valueOf('2013-12-04 10:50:00.0'), repositories[1].commits[0].date)
        assertEquals('b7e0439790694e15a74e835371668382f16e41a4', repositories[1].commits[0].sha)
        assertEquals(Timestamp.valueOf('2013-12-04 10:47:00.0'), repositories[1].commits[1].date)
        assertEquals('480319c6018f922df912ef3c0d2442e849a07f7a', repositories[1].commits[1].sha)
        assertEquals(Timestamp.valueOf('2013-12-04 10:25:28.0'), repositories[1].commits[2].date)
        assertEquals('856724c4e456ad3cd7bfac7cc8d3b9525bbf702c', repositories[1].commits[2].sha)

        assertEquals('android-custom-layout-loader-experiment', repositories[2].name)
        assertEquals(Timestamp.valueOf('2013-11-28 09:12:24.0'), repositories[2].commits[0].date)
        assertEquals('5805b9da60cd156ba162e65f3b2b719de62feacc', repositories[2].commits[0].sha)
        assertEquals(Timestamp.valueOf('2013-11-28 09:05:38.0'), repositories[2].commits[1].date)
        assertEquals('04d7bc6c58da42e2d3dfbabb36300697ff9b3023', repositories[2].commits[1].sha)
        assertEquals(Timestamp.valueOf('2013-11-28 09:05:12.0'), repositories[2].commits[2].date)
        assertEquals('b87131004535c3dcfca2b1ef8c3455ebc73b6ece', repositories[2].commits[2].sha)
    }
}
