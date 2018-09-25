package com.alexvasilkov.vcs.dependency

import org.gradle.api.GradleException

class SvnDependency extends VcsDependency {

    final long rev
    final boolean isHead

    SvnDependency(Map map) {
        super(map)
        isHead = 'HEAD'.equals(map.rev)
        rev = map.rev instanceof Long || map.rev instanceof Integer ? map.rev : 0L
    }

    @Override
    void check() {
        super.check()
        if (rev == 0 && !isHead) throw new GradleException("Repo 'rev' was not specified")
    }

    @Override
    void checkEquals(VcsDependency d) {
        super.checkEquals(d)

        SvnDependency s = (SvnDependency) d

        if (s.isHead != isHead) throwEqualCheckFail('isHead', isHead, s.isHead)
        if (s.rev != rev) throwEqualCheckFail('rev', rev, s.rev)
    }
}
