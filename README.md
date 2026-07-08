# Gene Interaction Networks simulation

GINsim is a software tool for the design and analysis of qualitative dynamical models
of biological interaction networks.
For more info see https://ginsim.github.io/


## Build and I nstall

GINsim uses maven: ```mvn package``` will compile it and prepare a jar
(executable Java archive) as well as dependancies into the ```target``` folder.
To use it outside of the build dir, copy the jar and the lib folder or use
```mvn assembly::assembly``` to build a fat jar (with dependencies included).


## Run and documentation

Run GINsim using with ```java -jar GINsim.jar``` command.

User documentation is available on the [GINsim website](https://ginsim.github.io/doc/).

Generate javadoc (in the "target/site" folder) using "mvn javadoc:javadoc".
	

  
## Plugins

Most features in GINsim are implemented as internal extensions and declared using java annotations.
See the developer documentation for more details.
Note that out-of-tree plugins (i.e. plugins distributed as separate jar files) are not
supported, but supporting such plugins should be easy if needed.

