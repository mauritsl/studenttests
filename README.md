Student tests
===

This repository contains a research tool for analysing student tests
using the all-pairs method described by Edwards and Shams
in [Do student programmers all tend to write the same software tests?](https://dl.acm.org/doi/pdf/10.1145/2591708.2591757).

## Running

This project depends on the Akka, JavaParser and Apache POI libraries.
These packages are managed by Maven.

Some arguments are required when running this application.
These are the directory that contains all implementations
(both reference and student submissions), and the directory that is
used to run tests and write results.
When using Eclipse, these can be configured under
Run -> Run Configurations -> Arguments tab.
Also, it might be required to explicitly set a JAVA_HOME environment
variable that points to the Java JDK installation. This variable
can be added on the Environment tab.

Alternatively, the provided Docker configuration can be used to run
the test in a Docker container:

```
docker build -t studenttests .
docker run -it \
  -v /path/to/data:/data \
  -v /path/to/test:/test \
  studenttests
```

The data folder must contain a subfolder per submission / reference test.
Each subfolder must directly contain the files to test. These are:

* `LinkedQueue.java`: the class implementation
* `QueueTest.java`: test suite
* `Queue.java`: interface code

The `Queue.java` file may be placed in the test directory,
rather than storing a copy in each submission folder.
