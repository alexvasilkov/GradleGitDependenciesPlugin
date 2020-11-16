package com.alexvasilkov.gradle.git

import org.gradle.api.GradleException

import java.util.regex.Matcher

class GitDependency {

    final String configName
    final String url
    final String name
    final String commit
    final String projectPath
    final File dir
    final String authGroup
    final boolean needsAuth
    final String username
    final String password
    final boolean keepUpdated

    final String projectName
    final File projectDir

    GitDependency(SettingsExtension props, Credentials credentials, File rootDir, Builder builder) {
        configName = builder.configName
        url = builder.url
        name = builder.name ?: getNameFromUrl(url)
        commit = builder.commit ?: 'master'
        projectPath = builder.projectPath
        dir = builder.dir ?: (name != null ? new File(props.dir, name) : null)
        authGroup = builder.authGroup ?: props.defaultAuthGroup
        needsAuth = authGroup || builder.username || builder.password
        username = needsAuth ? (builder.username ?: credentials.username(authGroup)) : null
        password = needsAuth ? (builder.password ?: credentials.password(authGroup)) : null
        keepUpdated = builder.keepUpdated == null ? true : builder.keepUpdated

        projectName = ":$name"
        projectDir = projectPath == null || dir == null ? dir : new File(dir, projectPath)

        if (!url) {
            throw new GradleException("Git dependency: 'url' is not specified")
        }
        if (!name) {
            throw new GradleException("Git dependency: 'name' is not specified")
        }
        if (dir.exists() && !dir.isDirectory()) {
            throw new GradleException("Git dependency: 'dir' is not a directory '${dir.path}'")
        }
        if (needsAuth && !username) {
            throw new GradleException("Git dependency: 'username' is not specified for '${name}'." +
                    "\n${credentials.usernameHelp(authGroup)}")
        }
        if (needsAuth && !password) {
            throw new GradleException("Git dependency: 'password' is not specified for '${name}'." +
                    "\n${credentials.passwordHelp(authGroup)}")
        }
    }

    private static String getNameFromUrl(String url) {
        if (url == null) return null
        // Splitting last url's part before ".git" suffix
        Matcher matcher = url =~ /([^\/]+)\.git$/
        return matcher.find() ? matcher[0][1] : null
    }

    void checkEquals(GitDependency dep) {
        if (dep.url != url) throwEqualCheckFail('url', url, dep.url)
        if (dep.name != name) throwEqualCheckFail('name', name, dep.name)
        if (dep.commit != commit) throwEqualCheckFail('commit', commit, dep.commit)
        if (dep.projectPath != projectPath) throwEqualCheckFail('path', projectPath, s.projectPath)
        if (!Objects.equals(dep.dir, dir)) throwEqualCheckFail('dir', dir.path, dep.dir.path)
        if (dep.username != username) throwEqualCheckFail('username', username, dep.username)
        if (dep.password != password) throwEqualCheckFail('passwords', '***', '***')
    }

    private void throwEqualCheckFail(String paramName, Object val1, Object val2) {
        throw new GradleException("Git dependency: Found 2 incompatible '$name' dependencies" +
                " with different '$paramName' values: '$val1' and '$val2'")
    }

    @SuppressWarnings("unused")
    static class Builder {
        private final String configName
        private final String url

        private String name
        private String commit
        private String projectPath
        private File dir
        private String authGroup
        private String username
        private String password
        private Boolean keepUpdated

        Builder(String configName, String url) {
            this.configName = configName
            this.url = url
        }

        void name(String name) {
            this.name = name
        }

        void commit(String commit) {
            this.commit = commit
        }

        void branch(String branch) {
            this.commit = branch
        }

        void tag(String tag) {
            this.commit = tag
        }

        void projectPath(String path) {
            this.projectPath = path
        }

        void dir(def dir) {
            this.dir = dir as File
        }

        void authGroup(String authGroup) {
            this.authGroup = authGroup
        }

        void username(String username) {
            this.username = username
        }

        void password(String password) {
            this.password = password
        }

        void keepUpdated(boolean keepUpdated) {
            this.keepUpdated = keepUpdated
        }
    }
}
