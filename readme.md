# Brum - quickly extract dependency graph from Scala source code

Brum is a command-line tool that takes as input a list of Scala sources and
outputs a dependency graph between those files. The motivation behind Brum is to
automatically generate fine-grained targets for build tools like Bazel or Pants.
The key features of Brum include:

- High performance: benchmarks indicate that Brum can process somewhere between
  200-300k lines per second. The exact performance varies from codebase to
  codebase depending on parameters like code density (for example, volume of
- High fidelity: Brum uses the Scala 2 compiler meaning that it parses source
  code exactly like the Scala compiler.
- Easy to build from source: Brum only has one external dependency on the Scala
  2 compiler making it easy to build the Brum codebase with any build tool.

## Getting started

Start the sbt shell to run Brum.

```
$ sbt
> cli/run <...files>
```

The current cli implementation prints out a newline delimited stream of JSON
objects containing the defined and referenced symbols of each file.

```
sbt:brum> ~cli/run /Users/olafurpg/dev/scalameta/brum/tests/src/main/snapshots/example/Example.scala
[info] {"file":"/Users/olafurpg/dev/scalameta/brum/tests/src/main/snapshots/example/Example.scala","definitions":["example.Example","example.Example.Inner","example.CompanionInner"],"references":["example.Example","example.Example.Inner","example.CompanionInner"]}
```

Caveats:

- The current implementation is a rough prototype and generates a large number
  of false positives.
