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

    void apply(Settings settings) {
        CredentialsHelper.init(settings.gradle)
        DependenciesHelper dependencies = new DependenciesHelper()

        settings.gradle.settingsEvaluated { BaseSettings s ->
            GroovyShell shell = new GroovyShell()

            // Building vcs dependencies list by invoking build.gradle#vcs() method for each project
            s.projectDescriptorRegistry.allProjects.each { DefaultProjectDescriptor project ->
                if (project.buildFile.exists()) {
                    Script script = shell.parse(project.buildFile)

                    // Checks if there is a vcs dependencies configuration method
                    if (script.metaClass.respondsTo(script, 'vcs')) {
                        // Adding svn method
                        script.binding.setVariable('svn', { Map params ->
                            dependencies.add(project.name, new SvnDependency(project, params))
                        })

                        // Adding git method
                        script.binding.setVariable('git', { Map params ->
                            dependencies.add(project.name, new GitDependency(project, params))
                        })

                        // Executing configuration method
                        script.vcs()
                    }
                }
            }

            // Initializing vcs repositories. Including vcs project dependencies.
            dependencies.all().each { VcsDependency d ->
                if (d instanceof SvnDependency) {
                    SvnHelper.init((SvnDependency) d)
                }
                if (d instanceof GitDependency) {
                    GitHelper.init((GitDependency) d)
                }

                s.include(":${d.name}")
                s.project(":${d.name}").projectDir = d.projectDir
            }

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

        // Adding created vcs projects dependencies for each project
        settings.gradle.afterProject { Project p ->
            dependencies.get(p.name).each { VcsDependency d ->
                p.dependencies.add('compile', p.project(":${d.name}"))
            }
        }
    }

}
