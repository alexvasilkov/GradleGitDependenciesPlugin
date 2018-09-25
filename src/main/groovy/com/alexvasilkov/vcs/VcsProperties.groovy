package com.alexvasilkov.vcs

import org.gradle.api.initialization.Settings

@Singleton
class VcsProperties {

    private static final String DEFAULT_DIR = 'libraries'

    def dir
    boolean cleanup = true

    void apply(Closure closure) {
        closure.delegate = this
        closure()
    }

    void resolve(Settings settings) {
        if (!dir) dir = new File(settings.rootDir, DEFAULT_DIR)
    }

    File getDir() {
        return dir as File
    }
}
