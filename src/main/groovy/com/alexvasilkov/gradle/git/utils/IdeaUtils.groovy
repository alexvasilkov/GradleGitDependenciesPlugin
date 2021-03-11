package com.alexvasilkov.gradle.git.utils

import com.alexvasilkov.gradle.git.Dependencies
import com.alexvasilkov.gradle.git.GitDependency

import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class IdeaUtils {

    private IdeaUtils() {}

    static void cleanModules(File rootDir, Dependencies dependencies) {
        try {
            cleanModulesInternal(rootDir, dependencies)
        } catch (Exception ex) {
            Log.warn "Cannot clean up .idea/modules.xml file. ${ex.message}"
        }
    }

    private static void cleanModulesInternal(File rootDir, Dependencies dependencies) {
        final File modules = new File(new File(rootDir, '.idea'), 'modules.xml')
        if (!modules.exists() || !modules.isFile()) return

        final List<String> modulesLines = modules.readLines()

        // Finding module entries and reading module path and .iml file name
        final Pattern pattern = Pattern.compile(
                '\\s*<module fileurl="file://\\$PROJECT_DIR\\$/([^\\s]+)/([^\\s/]+\\.iml)')

        final Map<String, List<String>> modulesMap = new HashMap<>()
        final String[] moduleLinesPaths = new String[modulesLines.size()]

        modulesLines.eachWithIndex { String line, int pos ->
            final Matcher matcher = pattern.matcher(line)
            if (matcher.find()) {
                final String path = matcher[0][1]
                final String file = matcher[0][2]

                List<String> files = modulesMap.get(path)
                if (files == null) modulesMap.put(path, files = new ArrayList<>())
                files.add(file)

                moduleLinesPaths[pos] = path
            }
            null
        }

        // Collecting invalid dependencies modules' paths
        final Set<String> pathsToDelete = new HashSet<>()

        dependencies.all().each { GitDependency dep ->
            final String path = getRelativePath(rootDir, dep.projectDir)
            final List<String> files = modulesMap.get(path)

            if (files != null) {
                // Cleaning up dependency's module(s) if there are more than one module declaration
                // for single path or if corresponding .iml file doesn't exist.
                boolean delete = files.size() > 1 || !new File(dep.projectDir, files[0]).exists()
                if (delete) {
                    Log.info "Removing invalid module '$path' from .idea/modules.xml"
                    files.each { String fileName ->
                        File file = new File(dep.projectDir, fileName)
                        if (file.exists()) {
                            Log.info "Deleting file $path/$fileName"
                            file.delete()
                        }
                    }
                    pathsToDelete.add(path)
                }
            }
        }

        // Rewriting modules file excluding invalid modules detected above
        if (!pathsToDelete.isEmpty()) {
            modules.withWriter { BufferedWriter writer ->
                modulesLines.eachWithIndex { String line, int pos ->
                    String path = moduleLinesPaths[pos]
                    boolean writeLine = path == null || !pathsToDelete.contains(path)
                    if (writeLine) writer.writeLine(line)
                }
            }
        }
    }

    private static String getRelativePath(File root, File dir) {
        return Paths.get(root.absolutePath).relativize(Paths.get(dir.absolutePath)).toString()
    }
}
