GradleVcsDependencyPlugin
=========================

[![Maven Central][mvn-img]][mvn-url]

Gradle plugin to add external git/svn repositories as dependencies.

### How it works ###

1. You can provide dependency name, git or svn repository info and version in `build.gradle` file.
1. Plugin clones repository to `libraries/[NAME]` directory (can be changed, see further)
at specified commit (git) / revision (svn).
1. Cloned repo will be included as sub-project and correct project dependency will be added to original project.
1. Dependencies are resolved recursively, so your vcs dependencies can have other vcs dependencies
1. If several projects have dependencies with same name then dependencies info and versions
should be completely the same. Otherwise plugin will fail build process.
1. Plugin automatically updates repository if version info was updated.
But if there are any uncommited changes in local repo, than plugin will fail build process
until you manually resolve conflicts.
1. Removed dependencies will be automatically cleaned from `libraries` directory
(can be changed, see further).

### How to use ###

In `settings.gradle` file add next lines:

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'com.alexvasilkov:gradle-vcs-dependency:1.0.0'
        }
    }

    apply plugin: 'com.alexvasilkov.vcs-dependency'

Optionally you can also provide some settings next in `settings.gradle`:

    vcs {
        dir = 'libs' // Directory in which to store vcs repositories, 'libraries' by default
        cleanup = false // Whether to cleanup unused dirs inside 'libraries' dir, true by default
    }

In `build.gradle` add next method:

    def vcs() {
        svn name: '[Svn dependency name. Required]',
            url: '[Svn repository url. Required]',
            path: '[Path within repo url, i.e. /trunk/library/. Optional]',
            rev: [Revision number or 'HEAD'. Required]

        git name: '[Git dependency name. Required]',
            url: '[Git repository url. Required]',
            path: '[Path within repo which should be added as dependency, i.e. /library/. Optional]',
            commit: '[Commit id of any length or 'master' to checkout HEAD. Required]'
    }
    
Note, that using 'master' as git commit or 'HEAD' as svn revision is not recommended,
use explicit commit / revision instead.

Other optional parameters:

    def vcs() {
        xxx dir: '[Repository directory, overrides global vcs directory settings. Optional.]',
            username: '[Username to access repo. Optional.]',
            password: '[Password. Optional.]',
            authGroup: '[Group name to share same access credentials. Optional, default is VCS.]',
            includeProject: [Whether to include this repo as Gradle project. Optional, true by default.],
            addDependency: [Whether to add this project as dependency.
                            Only works if 'includeProject' is true. Optional, true by default.],
            keepUpdated: [Whether to update this repo automatically or not. Optional, true by default.]
    }

If `username` property is not specified, plugin will look first for property named
`[name in upper case]_USERNAME` and than for property `[authGroup]_USERNAME`
(`VCS_USERNAME` by default) in next places:

1. `vcs.properties` in the root directory of the project
1. `gradle.properties` in the root directory of the project
1. `gradle.properties` in `[USER_HOME]/.gradle/` directory
1. Environment variables

If `password` property is not specified, plugin will look first for property named
`[name in upper case]_PASSWORD` and than for property `[authGroup]_PASSWORD`
(`VCS_PASSWORD` by default) in same places.

I.e. if dependency name is `ProjectName` than plugin will first look for `PROJECTNAME_USERNAME`
and `PROJECTNAME_PASSWORD` properties.


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
