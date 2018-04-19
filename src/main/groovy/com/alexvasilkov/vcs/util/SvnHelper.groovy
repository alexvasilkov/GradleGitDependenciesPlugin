package com.alexvasilkov.vcs.util

import com.alexvasilkov.vcs.dependency.SvnDependency
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.wc.*

class SvnHelper {

    static void init(SvnDependency repo) {
        // Looks like IntelliJ IDEA forces SVNKit to use SSLv3 only, but this will fail for TLS 1.2
        System.clearProperty("svnkit.http.sslProtocols")

        boolean exists = isRepositoryExists(repo)
        SVNClientManager client = getClient(repo)

        if (exists) {
            String localUrl = getRepoUrl(client, repo)
            String targetUrl = getTargetUrl(repo).toString()

            if (!localUrl.equals(targetUrl)) {
                throw new GradleException("Svn cannot update from ${localUrl} to ${targetUrl}.\n" +
                        "Delete directory '${repo.repoDir}' and try again.")
            }

            long localRev = getRepoRevision(client, repo).number
            long targetRev = repo.rev

            boolean shouldUpdate

            if (!repo.keepUpdated) {
                shouldUpdate = false
            } else if (repo.isHead) {
                println "Svn updating ${localRev} to HEAD"
                shouldUpdate = true
            } else if (localRev < targetRev) {
                println "Svn local revision ${localRev}" +
                        " is less then target revision ${targetRev} for '${repo.repoDir}'"
                println "Svn updating to revision ${targetRev} for '${repo.repoDir}'"
                shouldUpdate = true
            } else if (localRev > targetRev) {
                println "Svn local revision ${localRev}" +
                        " is greater then target revision ${targetRev} for '${repo.repoDir}'"
                println "Svn reverting to revision ${targetRev} for '${repo.repoDir}'"
                shouldUpdate = true
            } else {
                shouldUpdate = false
            }

            if (shouldUpdate) {
                if (hasLocalChanges(client, repo)) {
                    throw new GradleException("Svn cannot update from revision ${localRev}" +
                            " to ${repo.isHead ? 'HEAD' : "revision ${targetRev}"}," +
                            " '${repo.repoDir}' contains local changes.\n" +
                            "Commit or revert all changes manually.")
                } else {
                    update(client, repo)
                }
            }
        } else {
            checkout(client, repo)
        }
    }

    private static SVNClientManager getClient(SvnDependency repo) {
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true)
        ISVNAuthenticationManager auth =
                repo.noAuth ? null : new BasicAuthenticationManager(repo.username, repo.password)
        return SVNClientManager.newInstance(options, auth)
    }

    private static boolean isRepositoryExists(SvnDependency repo) {
        File dir = repo.repoDir
        if (!dir.exists()) return false

        boolean repoExists = SVNWCUtil.isWorkingCopyRoot(dir)

        if (!repoExists && dir.list()) {
            throw new InvalidUserDataException("Svn local directory should be empty '${dir}'")
        }

        return repoExists
    }

    private static SVNRevision getRepoRevision(SVNClientManager client, SvnDependency repo) {
        return client.statusClient.doStatus(repo.repoDir, false).revision
    }

    private static String getRepoUrl(SVNClientManager client, SvnDependency repo) {
        SVNStatus status = client.statusClient.doStatus(repo.repoDir, false)
        return status.repositoryRootURL.toString() + '/' + status.repositoryRelativePath.toString()
    }

    private static boolean hasLocalChanges(SVNClientManager client, SvnDependency repo) {
        boolean hasChanges = false

        client.statusClient.doStatus(repo.repoDir, SVNRevision.HEAD, SVNDepth.INFINITY,
                false, false, false, false, new ISVNStatusHandler() {
            @Override
            void handleStatus(SVNStatus status) throws SVNException {
                if (status.contentsStatus != SVNStatusType.STATUS_NORMAL
                        && status.contentsStatus != SVNStatusType.STATUS_NONE) {
                    hasChanges = true
                }
            }
        }, null)

        return hasChanges
    }

    private static update(SVNClientManager client, SvnDependency repo) {
        SVNRevision rev = getTargetRevision(repo)

        try {
            println "Svn update started '${repo.repoDir}' at revision ${rev}"
            client.updateClient.doUpdate(repo.repoDir, rev, SVNDepth.INFINITY, false, false)
            println "Svn update finished"
        } catch (SVNException e) {
            throw new GradleException("Svn update failed for '${repo.repoDir}'\n${e.message}", e)
        }
    }

    private static checkout(SVNClientManager client, SvnDependency repo) {
        SVNURL svnUrl = getTargetUrl(repo)
        SVNRevision rev = getTargetRevision(repo)

        try {
            println "Svn checkout started '${svnUrl}' at revision ${rev}"
            client.updateClient.doCheckout(svnUrl, repo.repoDir, SVNRevision.UNDEFINED, rev,
                    SVNDepth.INFINITY, false)
            println "Svn checkout finished"
        } catch (SVNException e) {
            throw new GradleException("Svn checkout failed for '${svnUrl}'\n${e.message}", e)
        }
    }

    private static SVNRevision getTargetRevision(SvnDependency repo) {
        return repo.isHead ? SVNRevision.HEAD : SVNRevision.create(repo.rev)
    }

    private static SVNURL getTargetUrl(SvnDependency repo) {
        if (repo.url == null || repo.url.isEmpty()) {
            throw new InvalidUserDataException('Svn url is not specified')
        }

        String url = repo.path == null ? repo.url : repo.url + repo.path

        try {
            return SVNURL.parseURIEncoded(url)
        } catch (SVNException e) {
            throw new InvalidUserDataException("Wrong Svn url '${url}'", e)
        }
    }
}