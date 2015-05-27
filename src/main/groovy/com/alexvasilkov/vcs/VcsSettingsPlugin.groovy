package com.alexvasilkov.vcs

import com.alexvasilkov.vcs.dependency.GitDependency
import com.alexvasilkov.vcs.dependency.SvnDependency
import com.alexvasilkov.vcs.dependency.VcsDependency
import com.alexvasilkov.vcs.util.CredentialsHelper
import com.alexvasilkov.vcs.util.DependenciesHelper
import com.alexvasilkov.vcs.util.GitHelper
import com.alexvasilkov.vcs.util.SvnHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.initialization.BaseSettings
import org.gradle.initialization.DefaultProjectDescriptor

class VcsSettingsPlugin implements Plugin<Settings> {

    private DependenciesHelper dependencies = new DependenciesHelper()
    private GroovyShell shell = new GroovyShell()

    void apply(Settings settings) {
        // Adding configuration method
        settings.metaClass.vcs = VcsProperties.instance.&apply

        CredentialsHelper.init(settings.gradle)

        settings.gradle.settingsEvaluated { BaseSettings s ->
            resolveDependenciesRecursively(s, s.projectDescriptorRegistry.allProjects)
            cleanup(s)
        }

        // Adding created vcs projects dependencies for each project
        settings.gradle.afterProject { Project p ->
            dependencies.get(p.name).each { VcsDependency d ->
                p.dependencies.add('compile', p.project(projectName(d)))
            }
        }
    }


    void resolveDependenciesRecursively(BaseSettings s, Set<DefaultProjectDescriptor> projects) {
        Set<DefaultProjectDescriptor> newProjects = new HashSet<>()

        // Building vcs dependencies list by invoking build.gradle#vcs() method for each project
        projects.each { DefaultProjectDescriptor project ->

            if (project.buildFile.exists()) {
                Script script = shell.parse(project.buildFile)

                // Checks if there is a vcs dependencies configuration method
                if (script.metaClass.respondsTo(script, 'vcs')) {
                    // Adding git method
                    script.binding.setVariable('git', { Map params ->
                        dependencies.add(project.name, new GitDependency(project, params))
                    })

                    // Adding svn method
                    script.binding.setVariable('svn', { Map params ->
                        dependencies.add(project.name, new SvnDependency(project, params))
                    })

                    // Executing configuration method
                    script.vcs()

                    // Initializing vcs repositories. Including vcs project dependencies.
                    dependencies.get(project.name).each { VcsDependency d ->
                        if (d instanceof SvnDependency) {
                            SvnHelper.init((SvnDependency) d)
                        }
                        if (d instanceof GitDependency) {
                            GitHelper.init((GitDependency) d)
                        }

                        s.include(projectName(d))
                        DefaultProjectDescriptor newProject = s.project(projectName(d))
                        newProject.projectDir = d.projectDir
                        newProjects.add(newProject)
                    }
                }
            }

        }

        // If we have new projects we should process them too
        if (newProjects) resolveDependenciesRecursively(s, newProjects)
    }


    private void cleanup(BaseSettings s) {
        if (VcsProperties.instance.cleanup) {
            // Cleaning up unused directories and files
            File libsDir = VcsDependency.getDefaultDir(s.defaultProject)
            libsDir.listFiles().each { File dir ->
                boolean found = false
                dependencies.all().each { VcsDependency d ->
                    if (dir == d.repoDir) found = true
                }
                if (!found) dir.deleteDir()
            }
            if (!libsDir.list()) libsDir.deleteDir()
        }
    }


    private static String projectName(VcsDependency d) {
        return ":${d.name}"
    }

}
