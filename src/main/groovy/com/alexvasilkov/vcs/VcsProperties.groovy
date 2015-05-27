package com.alexvasilkov.vcs

@Singleton
class VcsProperties {

    def dir
    boolean cleanup = true

    void apply(Closure closure) {
        closure.delegate = this
        closure()
    }

}
