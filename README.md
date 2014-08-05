sbt-clojure
===========

an sbt plugin for clojure, inspired from [sbt-groovy](https://github.com/fupelaqu/sbt-groovy).

To use Clojure code in your Scala project, follow these few steps:

1. add this to build.sbt

```
seq(clojure.settings :_*)

libraryDependencies += "org.clojure" % "clojure" % "1.5.1"
```

2. add this to projectt/plugins.sbt

```
addSbtPlugin("com.unhandledexpression" % "sbt-clojure" % "0.1")
```

3. Create a clojure directory to hold the sources

Example:

```
src/
└── main
    ├── clojure
    │   ├── hello.clj
    │   └── sub
    │       └── num.clj
    └── scala
        └── main.scala
```
