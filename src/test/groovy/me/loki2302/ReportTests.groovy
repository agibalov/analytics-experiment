package me.loki2302

import groovy.sql.Sql
import me.loki2302.reports.TopRepositoriesByCommitCountReport
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

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
}
