# Introduction

This repository contains all the files for the HTTP Server.

I took the following repository as inspiration but modified and enhanced it heavily:
https://github.com/victorbucutea/httpserver

As base for the HTTP Server I decided to use the java blocking I/O instead of nio because blocking 
I/O is pretty simple while nio is hard to do right.

Under normal circumstances I would use a library like Netty/Jetty, undertow/vert.x or Spring/akka 
depending on the level of abstraction. There are plenty of people that can write better HttpServers 
than I can do.

I also decided to use an already existing mongoDb driver instead of writing it from scratch.

# Architecture

## Core

The general architecture looks as follow:
All files in the `core` package could be distributed as a library, it handles all the heavy stuff 
like binding to a socket, connection management and http request decoding.

`Server` is the abstract base which handles the binding and starts the `SocketProcessor` which is 
responsible for the handling of the connection of incoming socket connections, it also sets a 
timeout for the socket when no data is arriving and passes the connection to the registered handlers.

Any class that wants to handle incoming sockets connections needs to implement the `SocketHandler` 
interface and register with the server which in turn will let the `SocketProcessor` know about the handler.

In this exercise I already created a `HttpServer` class that registers a single pipeline handler: `HttpPipeline`
The `HttpPipeline` will take the incoming socket connection and tries to create a `HttpRequest`.
It will reject any connection that is no `HttpRequest` and passes valid request into a new pipeline.
If the request wants to be a keep alive connection than the pipeline will start the processing again 
(with timeout) so that resources can be saved.

Any classes that want to handle `HttpRequest` need to implement the `HttpHandler` interface which
will provide subclasses with the `HttpRequest` and a `HttpResponse` to fill.

While `SocketHandler` can stop the processing chain, `HttpHandler` can not.
 
## Application
 
Classes in the `handler` package are classes relevant for this exercise as they represent the  different stages a `HttpRequest` goes through.
 
The `HttpRootHandler` just shows the link for the file listing and the comment listing.

The `HttpRequestLogHandler` will just log the request and response to the the console (via the logger interface)

The `HttpKeepAlivehandler` sets the `Connection` header based on the clients request of a keep-alive connection.

The `HttpEtagHandler` checks if an Etag is set (will be done in the `HttpFileHandler` and goes 
through the following flow to determine the correct response: https://tools.ietf.org/html/draft-ietf-httpbis-p4-conditional-26#section-6
It also support multiple entries and wildcards for `If-Match` and `If-None-Match` according to: https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24

The `HttpFileHandler` will check if the given path represents a file or directory and returns all files or sends the binary representation to the client.
If the given path is no file or directory then a 404 is returned. 
Files and Directories will return a ETag which is based on the last modified time which should be enough for this exercise but could be improved. 
On a directory it will take the last modified time of the latest changed file/directory.

The `HttpCommentHandler` will list comments from a repository and enables to add new comments via POST.
You can also clear all comments for the sake of the exercise.

## Database

The `CommentRepository` is the interface for fetching, clearing and adding comments.
Per request I added a `MongoDbCommentRepository` which will store all comments in mongoDB.
The tests are using an in-memory repository and a custom logger.

Everything should be tested well enough for the exercise, I used integration and unit tests.

# Assumptions

For the exercise I made the following assumptions:

1. HTTP Requests are done according to the spec, especially in regards to line endings and new lines. I am not checing for bad line endings or any broken request. I assume everything goes well which should be more robust in a productive application.
2. If a HTTP Request has no content-length set or it is 0 then no body is attached. I am not reading the body until I get a -1 from the stream, but again this should be covered in a productive environment.
3. If the request contains a body it is assumed that the body is a String.
4. No content type is send as response, but it should be no problem to implement that in the future. I only did it for the comments so a form is shown.
5. I am not returning the diagnostic information for keep-alive like the timeout or the max use of the socket. However I will close the socket after 3 seconds of inactivity.

# Limitations

There are also some limitations:
Limitations:

1. Etag implementation is not ready yet for distributed use as it uses the last modified time of files which can differ on different servers
2. Server is always issuing strong etags
3. MongoRepository only supports one host at the moment and uses a fixed database as well as no support for authentication

# JAR and options

The jar can be found under the releases, it also indicates all options available.
I used https://github.com/typesafehub/config for the parsing of the options and command line arguments.
The default options are

```conf
server {
  port = 8080
  threads = 10
  logging {
    active = true
  }
  files {
    active = true
    root = ""
  }
  comments {
    active = true
    host = "localhost"
    port = 27017
  }
}
```

# Tests

To test the exercise just run:

`./gradlew test`

# Load test (simple ab test)

The difference between `close` and `keep-alive` connections can be seen below:

### Keep-Alive
```bash
ab -k -t2 -c8 http://localhost:8080/Users/n.plaschke                                                                                                                             ✓  9620  02:23:42
This is ApacheBench, Version 2.3 <$Revision: 1706008 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 5000 requests
Completed 10000 requests
Completed 15000 requests
Completed 20000 requests
Completed 25000 requests
Completed 30000 requests
Completed 35000 requests
Completed 40000 requests
Completed 45000 requests
Completed 50000 requests
Finished 50000 requests


Server Software:        SimpleHttpServer
Server Hostname:        localhost
Server Port:            8080

Document Path:          /Users/n.plaschke
Document Length:        0 bytes

Concurrency Level:      8
Time taken for tests:   1.890 seconds
Complete requests:      50000
Failed requests:        0
Keep-Alive requests:    50000
Total transferred:      6300000 bytes
HTML transferred:       0 bytes
Requests per second:    26452.19 [#/sec] (mean)
Time per request:       0.302 [ms] (mean)
Time per request:       0.038 [ms] (mean, across all concurrent requests)
Transfer rate:          3254.86 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.0      0       0
Processing:     0    0   0.5      0      17
Waiting:        0    0   0.5      0      17
Total:          0    0   0.5      0      17

Percentage of the requests served within a certain time (ms)
  50%      0
  66%      0
  75%      0
  80%      0
  90%      0
  95%      1
  98%      1
  99%      1
 100%     17 (longest request)
 ```

### Close
```bash
ab -t2 -c8 http://localhost:8080/Users/n.plaschke                                                                                                                                ✓  9621  02:23:54
This is ApacheBench, Version 2.3 <$Revision: 1706008 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 5000 requests
Completed 10000 requests
Finished 13771 requests


Server Software:        SimpleHttpServer
Server Hostname:        localhost
Server Port:            8080

Document Path:          /Users/n.plaschke
Document Length:        0 bytes

Concurrency Level:      8
Time taken for tests:   2.000 seconds
Complete requests:      13771
Failed requests:        0
Total transferred:      1666291 bytes
HTML transferred:       0 bytes
Requests per second:    6885.41 [#/sec] (mean)
Time per request:       1.162 [ms] (mean)
Time per request:       0.145 [ms] (mean, across all concurrent requests)
Transfer rate:          813.61 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0  10.9      0     741
Processing:     0    1  12.6      0     741
Waiting:        0    1  12.6      0     741
Total:          0    1  16.7      1     741

Percentage of the requests served within a certain time (ms)
  50%      1
  66%      1
  75%      1
  80%      1
  90%      1
  95%      1
  98%      1
  99%      1
 100%    741 (longest request)
```