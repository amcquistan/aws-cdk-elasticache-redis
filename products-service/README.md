references

- https://medium.com/aeturnuminc/securing-redis-with-access-control-lists-acls-54623606f411
- https://github.com/deshh/elasticache-iam-auth-implementation/blob/develop/src/main/java/com/deshan/cache/config/RedisConfig.java
- https://github.com/aws-samples/elasticache-iam-auth-demo-app/blob/main/src/main/java/com/amazon/elasticache/IAMAuthTokenRequest.java
- https://stackoverflow.com/questions/76022749/noperm-issue-in-redis-connection
- https://redis.io/docs/management/security/acl/
- https://aws.amazon.com/blogs/database/simplify-managing-access-to-amazon-elasticache-for-redis-clusters-with-iam/
- https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/auth-iam.html

create products-service user with password and read / write permissions on all keys starting with products-service

```shell
docker exec -it cache /bin/sh
redis-cli --pass Develop3r
ACL SETUSER products-service RESET ON '>productsd3v' ~products-service:* -@all +@write +@read +@connection +info
```