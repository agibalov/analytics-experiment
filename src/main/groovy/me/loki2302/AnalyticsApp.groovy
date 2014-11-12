package me.loki2302

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.sql.Sql

class AnalyticsApp {
    static void main(String[] args) {
        runAnalytics()
    }

    static void runAnalytics() {
        def sql = Sql.newInstance('jdbc:hsqldb:mem:test', 'sa', '', 'org.hsqldb.jdbc.JDBCDriver')
        def databaseFacade = new DatabaseFacade(sql)
        databaseFacade.init()

        makeDb(databaseFacade)

        def rows = sql.rows('''
select top 10
    R.name,
    (select count(C.id) from Commits as C where C.repositoryId = R.id) as CommitCount
from Repositories as R
order by CommitCount desc
''')
        println rows.join('\n')
    }

    static void makeDb(DatabaseFacade databaseFacade) {
        def objectMapper = new ObjectMapper()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        def jsonFactory = new JsonFactory()
        def jsonParser = jsonFactory.createParser(AnalyticsApp.class.getResourceAsStream('/loki2302-github.json'))
        try {

            def token = jsonParser.nextToken()
            if(token != JsonToken.START_OBJECT) {
                throw new RuntimeException()
            }

            token = jsonParser.nextToken()
            if(token != JsonToken.FIELD_NAME) {
                throw new RuntimeException()
            }

            def fieldName = jsonParser.currentName
            if(fieldName != 'repositories') {
                throw new RuntimeException()
            }

            token = jsonParser.nextToken()
            if(token != JsonToken.START_ARRAY) {
                throw new RuntimeException()
            }

            while(true) {
                token = jsonParser.nextToken()
                if(token == JsonToken.END_ARRAY) {
                    break
                }

                if(token != JsonToken.START_OBJECT) {
                    throw new RuntimeException()
                }

                token = jsonParser.nextToken()
                if(token != JsonToken.FIELD_NAME) {
                    throw new RuntimeException()
                }

                fieldName = jsonParser.currentName
                if(fieldName != 'fields') {
                    throw new RuntimeException()
                }

                token = jsonParser.nextToken()
                if(token != JsonToken.START_OBJECT) {
                    throw new RuntimeException()
                }

                def gitHubRepository = objectMapper.readValue(jsonParser, GitHubRepository)
                // println "REPOSITORY: ${gitHubRepository.name} ${gitHubRepository.description}"

                def repositoryId = databaseFacade.insertRepository(gitHubRepository)

                token = jsonParser.nextToken()
                if(token != JsonToken.FIELD_NAME) {
                    throw new RuntimeException()
                }

                fieldName = jsonParser.currentName
                if(fieldName != 'commits') {
                    throw new RuntimeException()
                }

                token = jsonParser.nextToken()
                if(token != JsonToken.START_ARRAY) {
                    throw new RuntimeException()
                }

                while(true) {
                    token = jsonParser.nextToken()
                    if(token == JsonToken.END_ARRAY) {
                        break
                    }

                    if(token != JsonToken.START_OBJECT) {
                        throw new RuntimeException()
                    }

                    def gitHubCommit = objectMapper.readValue(jsonParser, GitHubCommit)
                    // println "COMMIT: ${gitHubCommit.sha} ${gitHubCommit.date} ${gitHubCommit.message}"

                    databaseFacade.insertCommit(repositoryId, gitHubCommit)
                }

                token = jsonParser.nextToken()
                if(token != JsonToken.END_OBJECT) {
                    throw new RuntimeException()
                }
            }

            token = jsonParser.nextToken()
            if(token != JsonToken.END_OBJECT) {
                throw new RuntimeException()
            }
        } finally {
            jsonParser.close()
        }
    }
}
