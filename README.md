# ip2loc

[![Build Status](https://travis-ci.org/awelzel/ip2loc.svg?branch=master)](https://travis-ci.org/awelzel/ip2loc)

Playing with plain Servlets, log4j2, SQLite/JDBC, ip2location data and Maven
to provide a tiny web service to lookup geo info for an IPv4 address.

```
$ curl http://localhost:8080/ip2loc/lookup?ip=15.21.148.1
{
    "data": {
        "country_name": "United States",
        "country_code": "US",
        "city_name": "Palo Alto",
        "latitude": 37.409911,
        "longitude": -122.1604
    },
    "_meta": {"took_ms": 0},
    "success": true
}

$ curl http://localhost:8080/ip2loc/lookup?ip=meh
{
    "_meta": {"took_ms": 0},
    "success": false,
    "errors": [{
        "message": "invalid ip",
        "source": {"parameter": "ip"},
        "status": "400"
    }]
}
```
