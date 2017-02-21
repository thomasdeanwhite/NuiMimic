# NuiMimic #
[![Build Status](https://travis-ci.org/thomasdeanwhite/NuiMimic.svg?branch=master)](https://travis-ci.org/thomasdeanwhite/NuiMimic)[![Coverage Status](https://coveralls.io/repos/github/thomasdeanwhite/NuiMimic/badge.svg?branch=master)](https://coveralls.io/github/thomasdeanwhite/NuiMimic?branch=master)

Repository for the NuiMimic automated software testing tool.

# Installation #

To install:
- clone repo: 'git clone' 
- install LeapJava.jar as local maven dependancy: 'mvn install:install-file -Dfile=LeapJava.jar -DgroupId=com.leapmotion -DartifactId=leapmotion.sdk' [in LeapJava.jar folder]
- mvn clean install

# User Guide #

There are various steps to using NuiMimic:
1. Recording serialised Leap Motion frame data
2. Breaking the serialised frame data into raw NuiMimic data
3. Recording Screen State information
4. Processing NuiMimic data into final models.