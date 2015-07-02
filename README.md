#play-oauth-server

[OAuth 2](http://tools.ietf.org/html/rfc6749) server implemented using:
	+ [Play framework 2.4.x](https://github.com/playframework/playframework) 
	+ [play-oauth](https://github.com/giabao/play-oauth)
	+ [couchbase 4.0](http://www.couchbase.com/nosql-databases/downloads#PreRelease)
	+ [couchbase-scala](https://github.com/giabao/couchbase-scala)


### references
+ [OAuth 2](http://tools.ietf.org/html/rfc6749)
+ [OAuth 2: Bearer Token Usage](https://tools.ietf.org/html/rfc6750)
+ [Assertion Framework for OAuth 2.0 Client Authentication and Authorization Grants](https://tools.ietf.org/html/rfc7521)
+ [JSON Web Token (JWT)](https://tools.ietf.org/html/rfc7519)
+ [JWT Profile for OAuth 2.0 Client Authentication and Authorization Grants](https://tools.ietf.org/html/rfc7523)

### dev guide
+ jdk 8
+ scala 2.11.x & sbt 0.13.8
+ Intellij + scala plugin
+ [couchbase 4.0](http://www.couchbase.com/nosql-databases/downloads#PreRelease)
    + **4.0**
    + enable query & index service
    + remove default bucket, then create a bucket named `pk` with password as in file [application.conf](conf/application.conf)
+ run `cbq` - see [n1ql-guide](http://docs.couchbase.com/4.0/n1ql/getting-started.html)

```
/* `1` is pk.auth.CBType.TUser */
CREATE INDEX acc_by_email ON pk(email) WHERE tpe = 1 USING GSI;

/* `3` is pk.auth.CBType.TToken */
CREATE INDEX refresh_token ON pk(refreshToken) WHERE tpe = 3 USING GSI;

SELECT * FROM system:indexes;

EXPLAIN
    SELECT pk.* FROM pk USE INDEX (acc_by_email USING GSI)
    WHERE email LIKE 'abc@%' AND tpe = 1;
```

some other syntax:
```
DROP INDEX pk.acc_by_email USING GSI;
CREATE PRIMARY INDEX ON pk USING GSI;
DROP PRIMARY INDEX ON pk USING GSI;
```
