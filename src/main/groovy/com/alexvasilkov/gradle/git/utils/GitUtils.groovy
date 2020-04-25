package com.alexvasilkov.gradle.git.utils

import com.alexvasilkov.gradle.git.GitDependency
import org.ajoberstar.grgit.Credentials
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Constants
import org.gradle.api.GradleException

class GitUtils {

    static void init(GitDependency repo) {
        Grgit git = openGit(repo)

        if (git != null) {
            String remoteUrl = getRemoteUrl(git)

            if (remoteUrl == null || remoteUrl != repo.url) {
                throw new GradleException("Git cannot update from ${remoteUrl} to ${repo.url}.\n" +
                        "Delete directory '${repo.dir}' and try again.")
            }

            String targetCommit = repo.commit

            if (repo.keepUpdated && !isLocalCommit(git, targetCommit)) {
                String localCommit = git.head().id
                println "Git local version '${localCommit}' is not equal to target " +
                        "'${targetCommit}' for '${repo.dir}'"

                if (hasLocalChanges(git)) {
                    throw new GradleException("Git repo cannot be updated to '${targetCommit}', " +
                            "'${repo.dir}' contains local changes.\n" +
                            "Commit or revert all changes manually.")
                } else {
                    println "Git updating to version '${targetCommit}' for '${repo.dir}'"
                    update(git, repo)
                }
            }
        } else {
            cloneRepo(repo)
        }
    }

    private static Grgit openGit(GitDependency repo) {
        try {
            return Grgit.open(dir: repo.dir, credentials: getCreds(repo))
        } catch (RepositoryNotFoundException ignored) {
            return null
        }
    }

    private static String getRemoteUrl(Grgit git) {
        return git.repository.jgit.repository.config
                .getString('remote', Constants.DEFAULT_REMOTE_NAME, 'url')
    }

    private static boolean isLocalCommit(Grgit git, String targetId) {
        def head = git.head()
        // Checking if local commit is equal to (starts with) requested one.
        // If not then we should check if there are tags with target name pointing to current head.
        return head.id.startsWith(targetId) ||
                git.tag.list().find { it.commit == head && it.name == targetId }
    }

    private static boolean hasLocalChanges(Grgit git) {
        Status status = git.status()
        return !status.staged.allChanges.isEmpty() || !status.unstaged.allChanges.isEmpty()
    }

    static boolean hasLocalChangesInDir(File dir) {
        try {
            Grgit git = Grgit.open(dir: dir)
            return hasLocalChanges(git)
        } catch (RepositoryNotFoundException ignored) {
            return false
        }
    }

    private static void update(Grgit git, GitDependency repo) {
        long start = System.currentTimeMillis()
        println "Git update started '${repo.url}' at version '${repo.commit}'"
        git.fetch()
        switchToVersion(git, repo.commit)
        int spent = System.currentTimeMillis() - start
        println "Git update finished ($spent ms)"
    }

    private static void cloneRepo(GitDependency repo) {
        long start = System.currentTimeMillis()
        println "Git clone started '${repo.url}' at version '${repo.commit}'"
        Grgit git = Grgit.clone(dir: repo.dir, uri: repo.url, credentials: getCreds(repo))
        switchToVersion(git, repo.commit)
        int spent = System.currentTimeMillis() - start
        println "Git clone finished ($spent ms)"
    }

    private static void switchToVersion(Grgit git, String commit) {
        git.checkout(branch: commit)
    }

    private static Credentials getCreds(GitDependency repo) {
        return repo.needsAuth ? new Credentials(repo.username, repo.password) : null
    }
}
