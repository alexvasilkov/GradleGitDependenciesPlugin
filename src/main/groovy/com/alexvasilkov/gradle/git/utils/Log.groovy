package com.alexvasilkov.gradle.git.utils

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class Log {

    private static final Logger logger = Logging.getLogger("git-dependencies-plugin")

    private Log() {}

    static void info(String msg) {
        logger.lifecycle("Git dependency: $msg")
    }

    static void warn(String msg) {
        logger.warn("Git dependency: $msg")
    }
}
