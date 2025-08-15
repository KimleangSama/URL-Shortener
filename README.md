### Command
To retrieve the password for the PostgreSQL user 'postgres',
```sql
SELECT rolname, rolpassword FROM pg_authid WHERE rolname = 'postgres';
```
```sql
create extension pg_stat_statements;
```
### Table
```sql
CREATE TABLE urls
(
    id          BIGSERIAL PRIMARY KEY,
    short_code  VARCHAR(255) NOT NULL UNIQUE,
    long_url    TEXT         NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE,
    click_count BIGINT
);
```