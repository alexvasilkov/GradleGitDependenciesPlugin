package com.alexvasilkov.vcs.dependency

import org.gradle.api.GradleException

class GitDependency extends VcsDependency {

    final String remote
    final String commit

    GitDependency(Map map) {
        super(map)
        remote = map.remote
        commit = map.commit
    }

    @Override
    void check() {
        super.check()
        if (!commit) throw new GradleException("Repo 'commit' was not specified")
    }

    @Override
    File getProjectDir() {
        return path == null ? repoDir : new File(repoDir, path)
    }

    @Override
    void checkEquals(VcsDependency d) {
        super.checkEquals(d)

        GitDependency g = (GitDependency) d

        if (g.remote != remote) throwEqualCheckFail('remote', remote, g.remote)
        if (g.commit != commit) throwEqualCheckFail('commit', commit, g.commit)
    }
}
