# Utopia Auth Service

Microservice to handle user account persistence and authentication with JWT headers.

The service runs on port 8089 and requires a local instance of MySQL running on port 3306 with a database schema named `utopia_accounts`.

A docker instance for this can be created quickly with the following command:

```sh
docker run --name utopia-mysql -e MYSQL_ROOT_PASSWORD=Welcome_1! -p 3306:3306 -d mysql:8.0.23
```

After starting the container, you will need to connect to the MySQL root database and use

```sql
CREATE
DATABASE utopia_customers;
```

After the above is accomplished, the service can be started with `mvn spring-boot:run` or `mvn spring-boot:start` to fork a new process.

## Authentication

Authentication is accomplished via `host_url/authenticate` ie `http://localhost:8089/authenticate`. This will validate credentials and return a JWT in the `Authorization` header.

## Additional Services

This service is additionally responsible for maintaining user account records ([see UserAccount](./src/main/java/com/ss/utopia/auth/entity/UserAccount.java)) and resetting an account password.

Additional services and their API specifications can be retrieved via OpenAPI while the service is running by going to `http://localhost:8089/api-docs` for the most up-to-date version. A copy is stored locally in the root folder as [api-docs.json](./api-docs.json) but is not guaranteed to be updated with changes to the service.
