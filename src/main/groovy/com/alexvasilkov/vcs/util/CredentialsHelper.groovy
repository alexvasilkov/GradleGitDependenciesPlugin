package com.alexvasilkov.vcs.util

import org.gradle.api.invocation.Gradle

class CredentialsHelper {

    private static final USERNAME = '_USERNAME'
    private static final PASSWORD = '_PASSWORD'
    private static final GRADLE_FILE = 'gradle.properties'
    private static final VCS_FILE = 'vcs.properties'

    private static boolean isInitialized
    private static File gradleUserHome
    private static Properties vcsProps, gradleProps, gradleHomeProps

    static String username(String projectName, String authGroup) {
        return get(projectName, authGroup, USERNAME)
    }

    static String password(String projectName, String authGroup) {
        return get(projectName, authGroup, PASSWORD)
    }

    static String usernameHelp(String projectName, String authGroup) {
        return help(projectName, authGroup, USERNAME)
    }

    static String passwordHelp(String projectName, String authGroup) {
        return help(projectName, authGroup, PASSWORD)
    }

    private static String get(String projectName, String authGroup, String suffix) {
        if (!projectName) return null

        String value = get(projectName.toUpperCase() + suffix)
        if (value) return value

        return get(authGroup + suffix)
    }

    private static String get(String propName) {
        if (vcsProps != null) {
            String value = vcsProps.get(propName)
            if (value) return value
        }

        if (gradleProps != null) {
            String value = gradleProps.get(propName)
            if (value) return value
        }

        if (gradleHomeProps != null) {
            String value = gradleHomeProps.get(propName)
            if (value) return value
        }

        return System.getenv().get(propName)
    }

    private static String help(String projectName, String authGroup, String suffix) {
        return "You should provide either ${projectName.toUpperCase()}${suffix}" +
                " or ${authGroup}${suffix} in either ${VCS_FILE}, ${GRADLE_FILE}" +
                " or ${gradleUserHome.absolutePath}/${GRADLE_FILE} files" +
                " or as environment variable"
    }

    public static void init(Gradle gradle) {
        if (isInitialized) return
        isInitialized = true

        File vcsFile = new File(VCS_FILE)
        if (vcsFile.exists()) {
            vcsProps = new Properties()
            vcsFile.withInputStream { stream -> vcsProps.load(stream) }
        }

        File gradleFile = new File(GRADLE_FILE)
        if (gradleFile.exists()) {
            gradleProps = new Properties()
            gradleFile.withInputStream { stream -> gradleProps.load(stream) }
        }

        gradleUserHome = gradle.gradleUserHomeDir
        File gradleHomeFile = new File(gradleUserHome, GRADLE_FILE)
        if (gradleHomeFile.exists()) {
            gradleHomeProps = new Properties()
            gradleHomeFile.withInputStream { stream -> gradleHomeProps.load(stream) }
        }
    }
}
