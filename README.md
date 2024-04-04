# springboot-currency-app

This is very simple spring boot app, it allows user:
- get a list of currencies used in the project;
- get exchange rates for a currency;
- add new currency for getting exchange rates.

Currencies used in the app are loaded on startup and updated each 10 seconds using external api.
To mimic external api WireMock is used

For a database PostgreSQL is used
DB and WireMock should be launched as a docker containers 

Project has [API documentation](http://localhost:8080/swagger-ui/index.html)

You can use the postman collection to test it
`postman/Currencies.postman_collection.json`

## Requirements

For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Docker](https://www.docker.com/)

## How to run

Before you run it, execute:
```shell
docker-compose -f ./docker/docker-compose.yml up -d
```

There are several ways to run this locally.
One way is to execute the `main` method in the `com.example.currencies.CurrenciesApplication` class from your IDE.

Alternatively you can use gradle:

```shell
./gradlew bootRun
```

## How to run unit tests

```shell
./gradlew test
```

## How to run integration tests

Start docker daemon and then: 

```shell
./gradlew integrationTest
```