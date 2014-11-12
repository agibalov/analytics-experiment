package me.loki2302

import groovy.transform.builder.Builder

@Builder
class GitHubCommit {
    String sha
    String message
    Date date
}