GradleVcsDependencyPlugin
=========================

[![Maven Central][mvn-img]][mvn-url]

Gradle plugin to add external Git and SVN repositories as dependencies.

### How to use ###

In `settings.gradle` file add the following lines:

    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.alexvasilkov:gradle-vcs-dependency:1.0.1'
        }
    }

    apply plugin: 'com.alexvasilkov.vcs-dependency'

Optionally you can provide settings next in `settings.gradle`:

    vcs {
        dir = 'libs' // Directory in which to store vcs repositories, 'libraries' by default
        cleanup = false // Whether to cleanup unused dirs inside 'libraries' dir, true by default
    }

Now in `build.gradle` add the following method:

    def vcs() {
        git name: 'GitDependencyName',
            url: 'https://example.com/repository.git',
            commit: '12345678abcdefgh'

        svn name: 'SvnDependencyName',
            url: 'https://example.com/repository',
            rev: 123
    }

Supported parameters:

| Parameter | Description |
| --------- | ----------- |
| name      | Dependency name, will be used as Gradle dependency name and as source code directory name. Required. |
| url       | Git (or SVN) repository url. Required. |
| path      | Path within repository which should be added as dependency. For example `/library/`, `/trunk/`. |
| commit    | Git commit id of any length, tag name, branch name. For example `v1.2.3` or `master`. Required for Git. |
| rev       | SVN revision number or 'HEAD'. Required for SVN. |
| dir       | Repository directory, overrides global directory settings. |
| username  | Username to access repository. |
| password  | Password to access repository. |
| authGroup | Group name (prefix) used when looking for access credentials. See `Credentials` section for more details. Default is `VCS`. |
| noAuth    | Whether authentication is required for this repository. Default value is `true` meaning that missing credentials will fail build process. |
| includeProject | Whether to include this repository as Gradle project or not. Can be set to `false` if you only want this repository to be fetched before building main project. Default is `true`. |
| keepUpdated    | Whether to update this repository automatically or not. Default is `true`. |
| configName     | Gradle dependency configuration name. For example `compile`, `implementation`, `api`. Default value is `implementation`. |

Note, that using 'master' as git commit or 'HEAD' as svn revision is not recommended, use explicit commit / revision instead.


### Example ###

    def vcs() {
        git name: 'GestureViews',
                url: 'https://github.com/alexvasilkov/GestureViews.git',
                commit: 'v2.5.1',
                path: '/library',
                noAuth: true
    }


### How it works ###

1. You're providing dependency name, Git or SVN repository URL and other details in `build.gradle` file.
1. Plugin clones repository to `libraries/[NAME]` project directory (can be changed, see further)
at specified commit (Git) or revision (SVN).
1. Cloned repo will be included as sub-project and necessary dependency will be added to original project.
1. Dependencies are resolved recursively, for example your Git dependency can have other Git or SVN dependencies.
1. If several projects have dependencies with same name then dependencies info and versions
should be completely the same. Otherwise plugin will fail build process.
1. Plugin automatically updates repository if version info was updated. But if there are any uncommited
changes in local repo then plugin will fail build process until you manually resolve conflicts.
1. Removed dependencies will be automatically cleaned from `libraries` directory (can be changed, see further).


### Credentials ###

If `username` property is not specified, plugin will look first for property named
`[name in upper case]_USERNAME` and then for property `[authGroup]_USERNAME`
(`VCS_USERNAME` by default) in next places:

1. `vcs.properties` in the root directory of the project
1. `gradle.properties` in the root directory of the project
1. `gradle.properties` in `[USER_HOME]/.gradle/` directory
1. Environment variables

If `password` property is not specified, plugin will look first for property named
`[name in upper case]_PASSWORD` and then for property `[authGroup]_PASSWORD`
(`VCS_PASSWORD` by default) in same places.

For example, if `name` property is set to `ProjectName` and `authGroup` property set to `Company`
then plugin will first look for properties called `PROJECTNAME_USERNAME` and `PROJECTNAME_PASSWORD`.
If no credentials found then plugin will check properties `COMPANY_USERNAME` and `COMPANY_PASSWORD`.


#### License ####

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[mvn-url]: https://maven-badges.herokuapp.com/maven-central/com.alexvasilkov/gradle-vcs-dependency
[mvn-img]: https://img.shields.io/maven-central/v/com.alexvasilkov/gradle-vcs-dependency.svg?style=flat-square
