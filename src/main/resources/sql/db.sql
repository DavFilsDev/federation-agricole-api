create database agricultural_federation;
create user user_agricultural with password 'admin_agricultural';

grant connect on database agricultural_federation to user_agricultural;
grant create on schema public to user_agricultural;

alter default privileges in schema public
    grant select, insert, update, delete on tables to user_agricultural;
alter default privileges in schema public
    grant usage, select, update on sequences to user_agricultural;