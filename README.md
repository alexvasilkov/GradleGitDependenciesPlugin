GradleGitDependencies
=====================

[![Maven Central][mvn-img]][mvn-url]

Gradle plugin to add external git repositories as dependencies.

### Setup ###

In `settings.gradle` file add the following lines:

* If using Gradle 6.x.x

```
plugins {
    id 'com.alexvasilkov.git-dependencies' version '2.0.1'
}
```

* If using older Gradle version

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.alexvasilkov:gradle-git-dependencies:2.0.1'
    }
}

apply plugin: 'com.alexvasilkov.git-dependencies'
```

Optionally you can provide settings next in `settings.gradle`:

```
git {
    dir 'libs' // Directory in which to store git repositories, 'libs' by default
    cleanup true // Whether to cleanup unused dirs inside 'libs' dir, true by default
    defaultAuthGroup 'group name' // Default auth group to be used for all repos. See `Credentials` section below.
}
```

### Usage ###

Now in project's `build.gradle` add the following:

```
git {
    implementation 'https://example.com/repository.git', {
        name 'DependencyName'
        commit '12345678abcdefgh'
    }
}
```

Where `implementation` is a configuration name, similar as used for regular gradle dependencies.
Can be any valid configuration name.

#### Supported parameters ####

| Parameter       | Description |
| --------------- | ----------- |
| name            | Dependency name. Will be used as gradle project name and as repo directory name. If the name is not set then it will be taken from url. |
| commit          | Git commit id of any length, tag name or branch name. For example `e628b205`, `v1.2.3`. Set to `master` by default. |
| tag             | Same as `commit`, see above. |
| branch          | Same as `commit`, see above. |
| dir             | Directory for cloned repository. Used to override default directory as defined in `settings.gradle`. |
| projectPath     | Path within repository which should be added as gradle project. By default repo's root directory is added as project. |
| username        | Username to access repository. See `Credentials` section below. |
| password        | Password to access repository. See `Credentials` section below. |
| authGroup       | Group name used when looking for credentials. See `Credentials` section below. |
| keepUpdated     | Whether to update this repository automatically or not. Default is `true`. |

Note that using `master` or any other branch name as git commit is not recommended,
use explicit commit or tag instead.


You can also specify git repos in `settings.gradle` similar as it is done in `build.gradle`
but use `fetch` instead of configuration name:

```
git {
    fetch 'https://example.com/repository.git', {
        dir "$rootDir/gradle/scripts"
        tag 'v1.2.3'
    }
}
```

Such repositories will be downloaded but not added as dependencies.
This can be useful, for example, if you want to pre-fetch build scripts.

### Examples ###

```
git {
    implementation 'git@github.com:alexvasilkov/GestureViews.git'

    api 'https://github.com/alexvasilkov/GestureViews.git', {
        name 'GestureViews'
        tag 'v2.6.0'
        projectPath '/library'
    }
}
```

### How it works ###

1. You're providing git repository URL and other optional details in `build.gradle` file.
2. The plugin clones repository to `libs/[name]` directory (both name and directory can be changed)
at specified commit, tag or branch.
3. Cloned repo will be included as sub-project and defined as dependency of original project.
4. Dependencies are resolved recursively, i.e. your git dependency can have other git dependencies.
5. If several projects have dependencies with same name then all other details (url, commit, etc)
should be completely the same, otherwise build process will fail.
6. The plugin automatically updates repository if `commit` doesn't much local commit. If there're any
uncommited changes in local repo then build process will fail until you manually resolve conflicts.
7. Removed dependencies will be automatically cleaned from `libs` directory.

### Credentials ###

If git repo is using SSH url (starts with `git@`) then the plugin will automatically try to use
local SSH key. But you need to ensure your SSH key is correctly setup, see instructions for
[GitHub](https://help.github.com/en/github/authenticating-to-github/connecting-to-github-with-ssh)
or [Bitbucket](https://confluence.atlassian.com/bitbucket/ssh-keys-935365775.html)

If git repo is using HTTPS url then there are two options how you can define credentials:

* Using `username` and `password` options directly in `build.gradle`.
* Using `authGroup` option and providing credentials as specified below.

If `authGroup` is provided then the plugin will search for `git.[authGroup].username` and
`git.[authGroup].password` params in:

* command line arguments (e.g. `-Dgit.github.username=email@test.com`)
* gradle.properties
* local.properties
* ~/.gradle/gradle.properties
* environment variables, in uppercase and with `_` instead of `.`, e.g. `GIT_GITHUB_USERNAME`

If `defaultAuthGroup` is provided in `settings.gradle` then it will be used for all repos
unless `authGroup` is explicitly set.

### Migration from v1.x.x ###

There were several breaking changes since version 1.x.x:

* Default directory is changed from `libraries` to `libs`.
* Instead of defining `def vcs() { ... }` method you can use simpler `git { ... }`
* Git dependencies declaration is reimplemented to mimic default gradle dependencies section.
* Credentials properties names are changed, e.g. was `GIT_AUTHGROUP_USERNAME`,
become `git.authGroup.username`.
* Dropped SVN support.

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

[mvn-url]: https://maven-badges.herokuapp.com/maven-central/com.alexvasilkov/gradle-git-dependencies
[mvn-img]: https://img.shields.io/maven-central/v/com.alexvasilkov/gradle-git-dependencies.svg?style=flat-square
