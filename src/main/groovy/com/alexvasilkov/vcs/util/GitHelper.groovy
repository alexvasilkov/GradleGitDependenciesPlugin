package com.alexvasilkov.vcs.util

import com.alexvasilkov.vcs.dependency.GitDependency
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.GradleException

class GitHelper {

    static void init(GitDependency repo) {
        Grgit git = openGit(repo)

        if (git != null) {
            String commit = repo.commit
            String localCommit = git.head().id
            File dir = repo.repoDir

            if (repo.keepUpdated && !localCommit.startsWith(commit)) {
                println "Git local version '${localCommit}' does not eqaul to version '${commit}'" +
                        " for '${dir}'"

                if (hasLocalChanges(git)) {
                    throw new GradleException("Git repo cannot be updated to '${commit}', " +
                            "'${dir}' contains local changes.\n" +
                            "Commit or revert all changes manually.")
                } else {
                    println "Git updating to version '${commit}' for '${dir}'"
                    update(git, repo)
                }
            }
        } else {
            cloneRepo(repo)
        }
    }

    private static Grgit openGit(GitDependency repo) {
        try {
            return Grgit.open(dir: repo.repoDir, creds: getCreds(repo))
        } catch (RepositoryNotFoundException e) {
            return null
        }
    }

    private static boolean hasLocalChanges(Grgit git) {
        Status status = git.status()
        return !status.staged.allChanges.isEmpty() || !status.unstaged.allChanges.isEmpty()
    }

    private static void update(Grgit git, GitDependency repo) {
        println "Git update started '${repo.url}' at version '${repo.commit}'"
        Map params = repo.remote ? [remote: repo.remote] : Collections.EMPTY_MAP
        git.fetch(params)
        switchToVersion(git, repo.commit)
        println "Git update finished"
    }

    private static void cloneRepo(GitDependency repo) {
        println "Git cloning started '${repo.url}' at version '${repo.commit}'"
        Map params = [dir: repo.repoDir, uri: repo.url, credentials: getCreds(repo)]
        if (repo.remote) params += [remote: repo.remote]
        Grgit git = Grgit.clone(params)
        switchToVersion(git, repo.commit)
        println "Git cloning finished"
    }

    private static void switchToVersion(Grgit git, String commit) {
        git.checkout(branch: commit)
    }

    private static Credentials getCreds(GitDependency repo) {
        return new Credentials(repo.username, repo.password)
    }

}