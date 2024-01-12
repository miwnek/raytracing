# Ray tracing

## Usage

```
git clone https://github.com/miwnek/raytracing
cd raytracing
sbt run
```
This will prompt you to pick  one of two main classes: <em>[1] runConcurrent</em> or <em>[2] runSequential</em>.<br>
 > <em>runConcurrent</em> runs a version of the program utilizing **Akka Actors** for concurrent ray computations. <br><em>runSequantial</em> runs a seqantial version. 

 Entering 1 or 2 will cause the corresponding program to run and generate a <em>jpg</em> image when it's done.

 > Note: by default the image takes a few minutes to render.<br>Depending on the machine you might want to change the **width** value in <em>src/main/scala/base/Main.scala</em>.

## Example
![alt text](https://github.com/miwnek/raytracing/blob/master/sample.jpg?raw=true)

## Authors
 - Mikołaj Wnęk - [miwnek](https://github.com/miwnek)
 - Bartosz Hanc - [barhanc](https://github.com/barhanc)
