package com.alexvasilkov.vcs.dependency

import org.gradle.api.GradleException
import org.gradle.api.initialization.ProjectDescriptor

class SvnDependency extends VcsDependency {

    long rev

    SvnDependency(ProjectDescriptor project, Map map) {
        super(project, map)
        rev = map.rev
    }

    @Override
    void check() {
        super.check()
        if (rev == 0) throw new GradleException("Repo 'rev' was not specified")
    }

    @Override
    void checkEquals(VcsDependency d) {
        super.checkEquals(d)

        SvnDependency s = (SvnDependency) d;

        if (s.rev != rev) throwEqualCheckFail('rev', rev, s.rev)
    }

}
