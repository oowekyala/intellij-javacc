# JJTricks

A code generator for JJTree grammars, with emphasis on very high
flexibility. Can be used as a drop-in replacement for JJTree.


### CLI basics

JJTricks takes as its main input a grammar file. If there is one
named `Java.jjt` in the current directory, then you can call jjtricks 
with `jjtricks Java`. This will generate everything in a `gen` subdirectory
of the current dir. You can change that with the `-o` option  (`--output`):
```
$ jjtricks Java -o target/generated-sources/javacc
```

JJTricks generates *classes*. The `-o` directory is just the root of the
package hierarchy, and generated classes will be placed in the subdirectory
corresponding to their package.

You may want JJTricks not to generate some files, because they're already
somewhere in your code. You can specify other source root directories
with the `-s` (`--source`) option, and existing files will not
be generated:
 
```
$ jjtricks Java -o target/generated-sources/javacc -s src/main/java
``` 
 
This is useful for example when you want JJTricks to generate
files for all nodes, except you want to write the class for some specific
nodes yourself, and place those in you main source root.

#### CLI Tasks

JJTricks comes with several goals that the CLI will run. The main
ones are the file generation goals:
* `gen:nodes`: generates Java source files for each node.
* `gen:common`: generates JJTricks support files
* `gen:javacc`: generates a JavaCC grammar from the JJTricks grammar file
* `gen:parser`: generates the parser from the JavaCC grammar produced by `gen:javacc`
* `gen:javacc-support`: generates additional (user-specified) JavaCC support files

Each of these has a corresponding configuration section in the configuration file.


#### Configuration model

JJTricks is configured via YAML files outside of the grammar itself.
If your grammar is named "Java.jjt", then the default configuration file
should be conventionally "Java.jjtopts.yaml", and JJTricks picks up on it
automatically. 


To allow sharing configuration, additional config files may be added with
the `--opts` CLI option. If several config files are provided, they are 
chained together, and default missing keys to the next member in the chain. 

JJTricks inserts two special config giles at the top of every config chain:
* `Root.jjtopts.yaml` -> this file contains the defaults for JJTricks, and 
several useful context variables (more on that later)
* The options specified inline in the grammar file (the JavaCC/JJTree 
`options { ... }` section). Most options of JJTree have an equivalent in
JJTricks, and if they're not provided in one of the config files, they're
defaulted to the corresponding JJTree/JavaCC option, or the default that
JavaCC uses itself.

A simple jjtopts file might look like this:
```yaml
jjtx:
  nodePrefix: Ex
  nodePackage: "org.expr"
```

This instructs JJTricks to generate node files into the package `org.expr`,
 and prepend their class name with `Ex`. These correspond to the JJTree options
 `NODE_PREFIX` and `NODE_PACKAGE` respectively, and override them if they were
 specified in the grammar file.

## Generation model

The support files for JJTricks are generated from Apache Velocity templates,
written in VTL. A run of JJTricks can generate any number of support files,
and you can add your own.

For example, the goal `gen:common` takes as input a set of *file generation 
tasks* which are described in YAML under the path `jjtx.commonGen`. For example,
your jjtopts file may feature the following:
```yaml
jjtx:
  commonGen:
    myVisitor:
      templateFile: "org/company/Visitor.java.vm"
      genClassName: "org.company.VisitorX"
      formatter: java
```

This describes a file gen task named `myVisitor`, which will generate a
class `org.company.VisitorX` by templating the given file with Velocity.
The output will then be formatted with a Java formatter. It will be run
when calling `jjtricks Java gen:common`.

Common gen can take any number of those file gen tasks.

### Templating context

The interesting thing with Velocity templates is that they can generate
different things based on context. Inside a template, variables may be 
fetched from the context object. 


JJTricks builds a context object for every file generation task. The following
 context variables are automatically filled-in:

* `$thisClass` provides info about the generated class, eg `$thisClass.simpleName`, `$thisClass.package` (see ClassVBean)
* `$thisFile` provides info about the location of the generated file, eg
 `$thisFile.absolutePath` (see FileVBean)
 
That information is derived from the `genClassName` YAML key of the file
 generation task in the YAML.
 
* `$grammar` provides information about the grammar JJTricks is being run on. 
For example, `$grammar.nodePackage` is the nodePackage defined in you config
 file, `$grammar.name` is the name of the grammar (eg `Java` for `Java.jjt`),
`$grammar.parser.class.qualifiedName` is the qualified name of the parser.
See GrammarVBean
 
#### User context

Context can be enriched on a case-by-case basis outside of the template. For example:
```yaml
jjtx:
  commonGen:
    myVisitor:
      templateFile: "org/company/Visitor.java.vm"
      genClassName: "org.company.VisitorX"
      context:
        date: "Today"
        maxLength: 16
```

The variables `$date` and `$maxLength` can then be used inside the template.


Since config files chain, you can actually override part of the context of
a file gen task defined in a parent config file. So if you defined the above
in a parent config file, in a some file lower in the config chain you could
 just write:

```yaml
jjtx:
  commonGen:
    myVisitor:
      context:
        date: "Tomorrow"
```
And the full file gen task would be resolved to:
```yaml
jjtx:
  commonGen:
    myVisitor:
      templateFile: "org/company/Visitor.java.vm"
      genClassName: "org.company.VisitorX"
      context:
        date: "Tomorrow"
        maxLength: 16
```

#### Global context














