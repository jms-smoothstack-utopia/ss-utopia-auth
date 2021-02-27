# Utopia Authentication Service

Microservice to handle user authentication and JWT token creation.

Authentication is achieved with the `/authenticate` endpoint from the host.

This service currently runs on port 8089 to avoid collisions on ports during development.

It requires the use of a local instance of MySQL running on port 3306 with a `utopia_accounts`
database for connection. See [./src/main/resources/application.properties](./src/main/resources/application.properties) for more details.

A docker instance for this can be created quickly with the following command:

```sh
docker run --name utopia-mysql -e MYSQL_ROOT_PASSWORD=Welcome_1! -p 3306:3306 -d mysql:8.0.23
```

After starting the container, you will need to connect to the MySQL root database and use

```sql
CREATE DATABASE utopia_accounts;
```

After the above is accomplished, the service can be started with `mvn spring-boot:run`
or `mvn spring-boot:start` to fork a new process.