package com.alexvasilkov.gradle.git

import com.alexvasilkov.gradle.git.utils.GitUtils
import com.alexvasilkov.gradle.git.utils.Log

class Dependencies {

    private final Map<String, GitDependency> byName = new HashMap<>()
    private final Map<String, List<GitDependency>> byProject = new HashMap<>()

    void add(String projectName, GitDependency dependency) {
        // Checking if there are no dependencies with same name,
        // or if existing dependency is completely the same
        GitDependency existing = byName.get(dependency.name)

        if (existing != null) {
            existing.checkEquals(dependency)
        } else {
            byName.put(dependency.name, dependency)
        }

        if (projectName == null) {
            Log.info "Added '${dependency.name}'"
        } else {
            // Adding per-project dependency
            List<GitDependency> list = byProject.get(projectName)
            if (list == null) {
                list = new ArrayList()
                byProject.put(projectName, list)
            }
            list.add(dependency)

            Log.info "Added '${dependency.name}' for '$projectName'"
        }

        GitUtils.init(dependency)
    }

    List<GitDependency> get(String projectName) {
        return byProject.get(projectName) ?: Collections.EMPTY_LIST
    }

    Collection<GitDependency> all() {
        return byName.values()
    }
}
