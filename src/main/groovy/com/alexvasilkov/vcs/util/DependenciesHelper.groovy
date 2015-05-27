package com.alexvasilkov.vcs.util

import com.alexvasilkov.vcs.dependency.VcsDependency

class DependenciesHelper {

    private final Map<String, VcsDependency> byName = new HashMap<>()
    private final Map<String, List<VcsDependency>> byProject = new HashMap<>()

    void add(String projectName, VcsDependency dependency) {
        dependency.check()

        // Checking if there are no dependencies with same name,
        // or if existing dependency is completely the same
        VcsDependency existing = byName.get(dependency.name)

        if (existing != null) {
            existing.checkEquals(dependency)
        } else {
            byName.put(dependency.name, dependency)
        }

        // Adding per-project dependency
        List list = byProject.get(projectName)
        if (list == null) {
            list = new ArrayList()
            byProject.put(projectName, list)
        }
        list.add(dependency)

        println "Added vcs dependency '${dependency.name}' for '${projectName}'"
    }

    List<VcsDependency> get(String projectName) {
        return byProject.containsKey(projectName) ? byProject.get(projectName) : Collections.EMPTY_LIST
    }

    Collection<VcsDependency> all() {
        return byName.values()
    }

}
