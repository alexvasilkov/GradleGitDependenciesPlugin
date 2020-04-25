package com.alexvasilkov.gradle.git

import org.gradle.api.invocation.Gradle

class Credentials {

    private static final USERNAME = 'username'
    private static final PASSWORD = 'password'

    private final File userDir
    private Properties gradleProps
    private Properties gradleUserProps
    private Properties localProps

    Credentials(Gradle gradle) {
        userDir = gradle.gradleUserHomeDir

        File gradleFile = new File('gradle.properties')
        if (gradleFile.exists()) {
            gradleProps = new Properties()
            gradleFile.withInputStream { stream -> gradleProps.load(stream) }
        }

        File gradleUserFile = new File(userDir, 'gradle.properties')
        if (gradleUserFile.exists()) {
            gradleUserProps = new Properties()
            gradleUserFile.withInputStream { stream -> gradleUserProps.load(stream) }
        }

        File localFile = new File('local.properties')
        if (localFile.exists()) {
            localProps = new Properties()
            localFile.withInputStream { stream -> localProps.load(stream) }
        }
    }

    private static String name(String authGroup, String suffix) {
        return "git.$authGroup.$suffix"
    }

    String username(String authGroup) {
        return get(authGroup, USERNAME)
    }

    String password(String authGroup) {
        return get(authGroup, PASSWORD)
    }

    private String get(String authGroup, String suffix) {
        if (!authGroup) return null
        String name = name(authGroup, suffix)
        return System.getProperty(name) ?:
                gradleProps?.getProperty(name) ?:
                        localProps?.getProperty(name) ?:
                                gradleUserProps?.getProperty(name) ?:
                                        System.getenv().get(name.replace('.', '_').toUpperCase())
    }

    String usernameHelp(String authGroup) {
        return help(authGroup, USERNAME)
    }

    String passwordHelp(String authGroup) {
        return help(authGroup, PASSWORD)
    }

    private String help(String authGroup, String suffix) {
        if (authGroup == null) {
            return ""
        } else {
            String name = name(authGroup, suffix)
            return "You should provide '$name'" +
                    " as command line argument (-D$name=...) OR" +
                    " in gradle.properties OR" +
                    " in local.properties OR" +
                    " in ${userDir.absolutePath}/gradle.properties OR" +
                    " as environment variable (${name.replace('.', '_').toUpperCase()})."
        }
    }
}
