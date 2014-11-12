package me.loki2302

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.sql.Sql
import org.eclipse.egit.github.core.Commit
import org.eclipse.egit.github.core.CommitUser
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.RepositoryService

class App {
    static void main(String[] args) {
        // runImport()

        runAnalytics()
    }

    static void runImport() {
        def gitHubUsername = System.properties.getProperty('gitHubUsername')
        def gitHubPassword = System.properties.getProperty('gitHubPassword')
        println gitHubUsername
        println gitHubPassword

        loadGitHubData(gitHubUsername, gitHubPassword)
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
        def jsonParser = jsonFactory.createParser(App.class.getResourceAsStream('/loki2302-github.json'))
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

    static void loadGitHubData(String gitHubUsername, String gitHubPassword) {
        def objectMapper = new ObjectMapper()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        def jsonFactory = new JsonFactory()
        def jsonGenerator = jsonFactory.createGenerator(new FileWriter('loki2302-github.json'))

        try {
            GitHubClient gitHubClient = new GitHubClient()
            gitHubClient.setCredentials(gitHubUsername, gitHubPassword)

            CommitService commitService = new CommitService(gitHubClient)
            RepositoryService repositoryService = new RepositoryService(gitHubClient)
            List<Repository> repositories = repositoryService.getRepositories()

            jsonGenerator.writeStartObject()
            jsonGenerator.writeFieldName('repositories')
            jsonGenerator.writeStartArray()

            for(Repository repository : repositories) {
                if(repository.private) {
                    continue
                }

                jsonGenerator.writeStartObject()
                jsonGenerator.writeFieldName('fields')
                objectMapper.writeValue(jsonGenerator, GitHubRepository.builder()
                        .name(repository.name)
                        .description(repository.description)
                        .build());

                jsonGenerator.writeFieldName('commits')
                jsonGenerator.writeStartArray()

                List<RepositoryCommit> repositoryCommits = commitService.getCommits(repository)
                for(RepositoryCommit repositoryCommit : repositoryCommits) {
                    repositoryCommit = commitService.getCommit(repository, repositoryCommit.getSha())

                    Commit commit = repositoryCommit.getCommit()
                    CommitUser commitUser = commit.getAuthor()

                    objectMapper.writeValue(jsonGenerator, GitHubCommit.builder()
                        .sha(repositoryCommit.sha)
                        .message(commit.message)
                        .date(commitUser.date)
                        .build())
                }

                jsonGenerator.writeEndArray()
                jsonGenerator.writeEndObject()

                // break
            }

            jsonGenerator.writeEndArray()
            jsonGenerator.writeEndObject()
        } finally {
            jsonGenerator.close()
        }
    }
}
