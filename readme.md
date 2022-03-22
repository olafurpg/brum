# Brum - quickly extract dependency graph from Scala source code

Brum is a command-line tool that takes as input a list of Scala sources and
outputs a dependency graph between those files. The motivation behind Brum is to
automatically generate fine-grained targets for build tools like Bazel or Pants.
The key features of Brum include:

- **High performance**: benchmarks indicate that Brum can process somewhere
  between 200-300k lines per second. The exact performance varies from codebase
  to codebase depending on parameters like code density (for example, volume of
- **High fidelity**: Brum uses the Scala 2 compiler meaning that it parses
  source code exactly like the Scala compiler.
- **Small dependency footprint**: it should be easy to build Brum from source.
  Brum only has one external dependency on the Scala 2 compiler making it easy
  to copy the codebase, build it from other build tools than sbt and tweak the
  code.

## Getting started

Start the sbt shell to run Brum.

```
$ sbt
> cli/run <...files>
```

The current cli implementation prints out a newline delimited stream of JSON
objects containing the defined and referenced symbols of each file.

```
sbt:brum> ~cli/run /Users/olafurpg/dev/scalameta/brum/brum-tests/src/main/resources/example/Example.scala
[info] running (fork) brum.Brum /Users/olafurpg/dev/scalameta/brum/brum-tests/src/main/resources/example/Example.scala
[info] {"file":"/Users/olafurpg/dev/scalameta/brum/brum-tests/src/main/resources/example/Example.scala","definitions":["example.Example","example.Example.Inner","example.Example.CompanionInner","example.Example.CompanionInner2"],"references":["scala.collection.mutable.ArrayBuffer","scala.collection.mutable.TreeMap","scala.math.BigDecimal","scala.concurrent","scala.annotation.strictfp","example.scala.annotation.strictfp","scala.math.Ordered","example.Example.scala.math.Ordered","example.Example.Int","example.Example.ArrayBuffer","example.Example.TreeMap","example.Example.defaultMathContext","scala.annotation.compileOnly","example.Example.scala.annotation.compileOnly","scala.concurrent.Future","example.Example.List","scala.util.Success","example.Example.scala.util.Success","example.Example.Ordering","scala.AnyRef","example.Example.Inner.scala.AnyRef","example.Example.scala.AnyRef","example.Example.CompanionInner.scala.AnyRef","example.Example.CompanionInner2.scala.AnyRef"]}
```

For every document, we emit the following JSON keys:

- `"filename"`: absolute path of the file.
- `"definitions"`: fully qualified names of symbols that are defined in this
  document.
- `"references"`: fully qualified names of symbols that are referenced in this
  document. This list is a gross overapproximation, many symbols in this may not
  exist.

## Caveats

- The current implementation is a prototype and produces a large number of false
  negatives and false positives.

## Benchmarks

The benchmarks below are mostly intended to give a rough estimate for the
upper-bound on processing performance. These numbers will get slower as more
functionality gets added.

```
[info] Benchmark                         (corpus)  Mode  Cnt        Score   Error  Units
[info] BrumBench.indexCorpus                 spark  avgt          3581.355          ms/op
[info] BrumBench.indexCorpus:linesOfCode     spark  avgt       1090674.000     304k loc/s
[info] BrumBench.indexCorpus                 scala  avgt          1533.126          ms/op
[info] BrumBench.indexCorpus:linesOfCode     scala  avgt        363140.000     236k loc/s
[info] BrumBench.indexCorpus                  cats  avgt           348.193          ms/op
[info] BrumBench.indexCorpus:linesOfCode      cats  avgt         76470.000     219k loc/s
[info] BrumBench.indexCorpus                paiges  avgt            28.770          ms/op
[info] BrumBench.indexCorpus:linesOfCode    paiges  avgt          3518.000     122k loc/s
```
