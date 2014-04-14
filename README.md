idea-plugin-jibx
================

# JiBX plugin for JetBrains Idea.

## Description
Provides **JiBX Binding** support for Java language

To configure version 0.1, you must place all your jibx bindings in a folder called "jibx" in your module's root.
Files that are not jibx bindings can not exist in this directory.  You must also have jibx configured as a
dependency of your module, as it will be used for the actual compilation (not the version included with this plugin).

Please contact me via e-mail or github for questions, feature requests, bugs, pull requests, etc.

Future releases will include:
* Configuration of compilation verbosity logging
* Configurable (or scannable) jibx binding file location
* Integration of other JiBX 'extras' in the IDE
* Integration of JiBX validation with Idea's validation
* Unit tests for plugin's build
* Gradle build
* Want others?  Send requests.

## Changelog

**0.1**
* Integrated JiBX compilation into Idea's JPS External Make system
* Added detection of files with .jibx extension as XML files

## Building

Add the lib provided in the JiBX download directory under the lib folder in the plugin module folder.  See
http://confluence.jetbrains.com/display/IDEADEV/External+Builder+API+and+Plugins for more information on the JPS
external build system.