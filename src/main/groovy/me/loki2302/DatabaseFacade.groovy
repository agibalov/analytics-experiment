package me.loki2302

import groovy.sql.Sql

public class DatabaseFacade {
    Sql sql

    DatabaseFacade(Sql sql) {
        this.sql = sql
    }

    void init() {
        sql.execute('''
create table Repositories(
    id int identity primary key,
    name varchar(256) not null,
    description varchar(256) not null
)
''')

        sql.execute('''
create table Commits(
    id int identity primary key,
    sha varchar(40) not null,
    date timestamp not null,
    message varchar(512) not null,
    repositoryId int not null constraint CommitRepositoryRef references Repositories(id))
''')
    }

    int insertRepository(GitHubRepository gitHubRepository) {
        sql.executeInsert("""
insert into Repositories(name, description)
values(${gitHubRepository.name}, ${gitHubRepository.description})
""").first().first()
    }

    int insertCommit(int repositoryId, GitHubCommit gitHubCommit) {
        sql.executeInsert("""
insert into Commits(sha, date, message, repositoryId)
values(${gitHubCommit.sha}, ${gitHubCommit.date}, ${gitHubCommit.message}, $repositoryId)
""").first().first()
    }
}