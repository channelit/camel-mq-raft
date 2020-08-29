## Apache Camel + RSocket + ActiveMQ + Postgres + Spring Boot

### Spring Boot WebFlux Reactive application with Camel integration and ActiveMQ backend with Postgres persistence..

```shell script
docker build -t backend-app .
```

kubectl expose deployment postgres --port=5432 --target-port=5432 -n postgres