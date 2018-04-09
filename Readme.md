## Database Profile Plugin

### Goal

The idea of this plugin is to extend Gradle with the idea of named database profiles that a 
user could choose between when starting a build.  The selection of a profile might imply a number of 
actions that must occur based on the profile selected - we might need to add extra jars to the 
`testRuntime` configuration, adjust properties files, etc.


### Applying the plugin

Using Gradle's new plugins DSL:

    plugins {
        ...
        id 'org.hibernate.testing.database-profile' version 'X.Y.Z'
    }


Using Gradle's legacy Configuration approach:

	buildscript {
	  repositories {
        gradlePluginPortal()
		// ^^ same as:
		// maven {
		//   url "https://plugins.gradle.org/m2/"
		// }
	  }
	  dependencies {
		classpath "gradle.plugin.org.hibernate.build:database-profile-plugin:X.Y.Z"
	  }
	}
	
	apply plugin: "org.hibernate.testing.database-profile"


### Basics

When applied, the plugin will:

1. Register an extension (`ProfileExtension`) under `databaseProfiles`
2. Create a task called `applyDatabaseProfile` of type `ProfileTask`

`ProfileExtension` is used to control where the plugin can look for profiles and control
the profile that should be selected.

`ProfileTask` acts as a Gradle `Provider` (think delayed resolution) of the `Profile`
to use.  


### Defining Profiles

Available profiles are resolved using a directory search, as follows:

1. If the project is non-root, see if it defines a `${projectDir}/databases/` directory.  If it
	does then search here first
2. If any custom search directories have been added, check these next.  Custom directories
	can be added either by specifying a `custom-profiles-dir` build property or 
	through `ProfileExtension#customSearchDirectory`
3. Finally look at the project's parent project for a `${projectDir}/databases/` directory, up
	to the root project

These directories are searched recursively.  We leverage this in Hibernate to allow the standard _databases_ directory
to hold local profiles too.  That is achieved by a _.gitignore_ which says to ignore any directory named
_local_ under the directory _databases_.  So one option to provide custom profiles is to drop them in there.  That
has the benefit of not having to specify _hibernate-matrix-databases_
Within these directories, the plugin looks for sub-directories which either:

*    contain a file named _matrix.gradle_.  _matrix.gradle_ is a limited DSL Gradle file which currently understands
     just a specialized org.gradle.api.artifacts.Configuration reference named _jdbcDependency_.  All that is a fancy
     way to say that _matrix.gradle_ allows you to specify some dependencies this database profile needs (JDBC drivers,
     etc).  Any dependency artifacts named here get resolved using whatever resolvers (Maven, etc) are associated with
     the build.  For example

        jdbcDependency {
            "mysql:mysql-connector-java:5.1.17"
        }
*    contain a directory named _jdbc_ which is assumed to hold jar file(s) needed for the profile.

Such directories become the basis of a database profile made available to the build.  The name of the profile
is taken from the directory name.  Database profiles can also contain a _resources_ directory.

An example layout using _matrix.gradle_ might be

        ├── mysql50
        │   ├── jdbc
        │   │   └── mysql-connector-java-5.1.9.jar
        │   └── resources
        │       └── hibernate.properties

Or

        ├── mysql50
        │   ├── matrix.gradle
        │   └── resources
        │       └── hibernate.properties


Either would result in a database profile named _mysql50_


### Specifying the profile to use

The profile to use can be specified in 2 ways:

1. Set `database_profile_name` build property to the name of the profile to use
2. Use `ProfileExtension#profileToUse`


### Plugin behavior

After project evaluation, the plugin will resolve the `Profile` to use.  If none,
the rest of behavior is short-circuited.

The behavior comes from the plugin's `ProfileTask`.  `ProfileTask` is a 
"grouping task" - it defines no "task action", it simply acts as a container
to which we can hook other actions as a singular task name.  It is 
expected that most of these "other actions" come from:

* `ProfileTask#augment`
* `ProfileTask#filterCopy`
* `ProfileTask#extend`

By default the plugin will resolve overlay the profile's properties over 
top of the `${buildDir}/resources/test/hibernate.properties` file.  This is a 
process called augmentation - a properties file is loaded into a `Properties`
object and then the profile's properties are added over top of them and then
written back out.

A custom properties file augmentation can be requested using `ProfileTask#augment`.

Builds can also request a filtered-copy using `ProfileTask#filterCopy`.  A filtered-copy
is basically a copy based on the provided CopySpec config closure extended profile properties 
replacements.

Lastly, pretty generic actions can be requested using `ProfileTask#extend` which
accepts an `Action<Profile>` - during `doLast`, the task will call this action with 
the resolved profile.   

