package com.alexvasilkov.gradle.git.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Log {

    private static final Logger logger = LoggerFactory.getLogger("git-dependencies-plugin")

    private Log() {}

    static void info(String msg) {
        logger.info("Git dependency: $msg")
    }

    static void warn(String msg) {
        logger.warn("Git dependency: $msg")
    }
}
