In order to run this you should run thins in the following order

1. sample.cluster.transformation.frontend.TransformationFrontendApp command line arg 2551
2. sample.cluster.transformation.backend.TransformationBackendApp command line arg 2552
3. sample.cluster.transformation.backend.TransformationBackendApp
4. sample.cluster.transformation.backend.TransformationBackendApp
5. sample.cluster.transformation.frontend.TransformationFrontendApp



ClusterClient

https://groups.google.com/forum/#!topic/akka-user/rqSqh82fimE
Future<Object> future = Patterns.ask(clusterClient, new ClusterClient.Send(ClusterActorPath, message, true), timeout)

https://gist.github.com/phoenix24/6097895

http://blog.michaelhamrah.com/2014/02/using-akkas-clusterclient/



Distributed workers
http://www.lightbend.com/activator/template/akka-distributed-workers?_ga=1.183640678.1129354442.1469166689