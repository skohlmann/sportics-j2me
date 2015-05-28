# Introduction #

To build the project for BlackBerry the [ant-bb-tools](http://bb-ant-tools.sourceforge.net/) are used.
The jar is already packaged in the contrib folder of this project but you still need to meet their requirements.

More specific you need the BlackBerry JDE bin folder for the compiler and an installed WTK for the preverify executable.

When everything is set up you can specify the paths to these files in the Sportics-build.properties file.

To build the project use the ant target 'build-bb'
`$ ant build-bb`