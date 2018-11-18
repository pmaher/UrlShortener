
# URL Shortener API

A RESTful API that can be used to shorten a lengthy URL

# The operations supported

* Create a shortened URL from a long URL
```
$ curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"originalUrl": "http://www.npr.org"}' \
  http://localhost:8080/api/shortUrl
```
* Retrieve the original URL and number of times it's been accessed from a shortened URL (assuming 'b' is the short url)
```
$ curl --request GET \
  http://localhost:8080/api/shortUrl/b
```
* Redirect a user to the long URL when they access the short URL from your API
```
$ curl -D -   http://localhost:8080/go/b
```
* Delete a shortened URL
```
curl --request DELETE \
  http://localhost:8080/api/shortUrl/b
```

Unit and Integration tests provided

You can assume that if a user has access to your API that they are authorized to execute any operation mentioned above


# Setup

This uses Spring Boot configured to talk to an in memory H2 database to store the results

Jackson is included to provide JSON serialization and deserialization

## Dependencies

This project uses [Maven](https://maven.apache.org) for builds.

You need Java 8 installed.


## Building

```
$ mvn package
```


## Running

You can run the app through Maven:

```
$ mvn spring-boot:run
```

or you can run the jar file from the build:

```
$ java -jar target/url-shortener-1.0.0-SNAPSHOT.jar
```



