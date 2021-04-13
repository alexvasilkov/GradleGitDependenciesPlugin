package com.alexvasilkov.gradle.git.utils

import org.eclipse.jgit.util.SystemReader

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

// Based on https://github.com/ajoberstar/grgit/blob/1a1c6b7/grgit-core/src/main/groovy/org/ajoberstar/grgit/auth/GrgitSystemReader.java
class SshSystemReader extends SystemReader {

    @Delegate
    private final SystemReader delegate

    private final String gitSsh

    private SshSystemReader(SystemReader delegate, String gitSsh) {
        this.delegate = delegate
        this.gitSsh = gitSsh
    }

    @Override
    String getenv(String variable) {
        String value = delegate.getenv(variable)
        if ('GIT_SSH' == variable && value == null) {
            return gitSsh
        } else {
            return value
        }
    }

    static void install() {
        final SystemReader current = instance
        if (current instanceof SshSystemReader) return

        final String gitSsh = findExecutable("ssh") ?: findExecutable("plink")
        instance = new SshSystemReader(current, gitSsh)
    }

    private static String findExecutable(String exe) {
        final String path = System.getenv('PATH') ?: ''
        final String pathExt = System.getenv('PATHEXT') ?: ''
        final Pattern splitter = Pattern.compile(Pattern.quote(File.pathSeparator))

        final String[] extensions = splitter.split(pathExt)
        final List<String> withExt =
                extensions.length == 0 ? [exe] : extensions.collect { ext -> exe + ext }

        return splitter.split(path).findResult {
            final Path dir = Paths.get(it)
            withExt.findResult {
                final Path exePath = dir.resolve(it)
                Files.isExecutable(exePath) ? exePath.toAbsolutePath().toString() : null
            }
        }
    }
}
