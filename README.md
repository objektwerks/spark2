Spark2
------
>The purpose of the project is to test Spark 2 features.

Test
----
1. sbt clean test

Bloop
-----
1. sbt bloopInstall
2. bloop projects
3. bloop clean spark
4. bloop compile spark
5. bloop test spark

Run
---
1. sbt clean test run

    * [1] objektwerks.DeltaLakeApp
    * [2] objektwerks.FlightGraphApp
    * [3] objektwerks.KMeansApp
    * [4] objektwerks.LinearRegressionApp
    * [5] objektwerks.LogEntryApp
    * [6] objektwerks.RecommendationApp
    * [7] objektwerks.WinePricePredictionApp
 
Logs
----
1. ./target/test.log
2. ./target/app.log

Events
------
1. ./target/local-*

Tunning
------- 
>kyro serialization, partitions, driver and executor memory/cores, cache/persist/checkpointing, narrow vs wide transformations,
>shuffling (disk/network io), splittable files, number of files and size, data locality, jvm gc, spark web/history ui,
>tungsten

JDKs
----
>If you have more than one JDK installed, such as JDK 8 and JDK 11, you need to run sbt using JDK 8.
Here's a few examples:

* sbt clean test -java-home /Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home
* sbt run -java-home /Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home

>Or, optionally, create an .sbtopts file.
 
.sbtopts
--------
1. Create an .sbtopts file in the project root directory.
2. Add this line ( to line 1 ): -java-home /Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home