does the table need to be created first? or does spring create the table for me like it does in mongodb

```cql
create table people(
  first_name text,
  date_of_birth timestamp,
  person_id uuid,
  last_name text,
  salary double,
  primary key((first_name), date_of_birth, person_id)
) with clustering order by (date_of_birth asc, person_id desc);
```

the datastax driver knows not to allow you to query full table scans without adding @Query(allowFiltering = true). It throws an exception if a query is used that does not include the partition key.

```
Caused by: com.datastax.driver.core.exceptions.InvalidQueryException: Cannot execute this query as it might involve data filtering and thus may have unpredictable performance. If you want to execute this query despite the performance unpredictability, use ALLOW FILTERING
```