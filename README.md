# Banka 4 — worlds greatest bank

# Development
## Using the development compose file
To start up the backend and its services, execute:

```
docker compose up --build --watch
```

… in the root directory of the repository.  Upon doing so, Docker will build
each service and start them up with watch enabled.  This means that every time
you edit the source code of any service, it will get automatically rebuilt and
restarted.  This rebuild and restart usually takes somewhere in the range of 6
to 10 seconds.

By convention, for each of the services, the `dev` profile is activated.  This
means that one can store development-specific properties in
`application-dev.EXT`, where `EXT` is `yml` or `properties`.

### Required setup
To use the exchange office caching service, you need a `.env` with an API key.
<!-- Ironically, an API key is already hard-coded in this repository.  I don't
want to fish it out, and I also don't want to encourage that behaviour, so I'll
pretqend it's fine.  -->

Your `.env` file must look something like this (the API key is invalid):

```env
# -*- conf -*-
EXCHANGERATE_API_KEY=776879617265796f7572656164696e6774686973
```

### Getting to the PSQL CLI

Use one of these two commands:
```
docker compose exec user_service_db psql -U user-service user-service
docker compose exec notification_service_db psql -U notification-service notification-service
```

### Developer-specific Spring properties
If you wish to add some developer-specific properties (such as, for instance,
email credentials for an SMTP server you were using to test something), you
can place `application-local.properties` or `application-local.yml` in the
resources directory.

Please do note that these profiles will also be copied into any builds you make
and also into the tests you run locally, and so, care should be taken not to
interfere with the testsuite.

The aforementioned files are already ignored.

> [!CAUTION]
> **PLEASE USE THIS FEATURE SPARINGLY**, it will lead to differences between
> developer machines, which are very annoying.

### Swagger UI
A Swagger UI for the user service is available on `/docs/ui` on the usual port.

## Writing migrations
This project uses
[Flyway](https://documentation.red-gate.com/fd/migrations-271585107.html)
([Spring specific
docs](https://docs.spring.io/spring-boot/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway))
to perform migrations on databases.

<!-- TODO brief summary -->

### Development container specifics
This project uses Hibernate ORM.

The development container provides configuration for Hibernate to emit DDL it
believes is correct for the entities specified in the codebase to the standard
error output of the service.  You can, hence, read the DDL Hibernate expects to
see from the respective container logs in order to write migrations.

The `db/migration` directory is ignored by Docker Watch.  This is because
it is too easy to run partially-written migrations if they are watched, and it
is difficult to reapply a migration.  When you're certain you've finished a
migration, restart the respective service to start the migrations.
