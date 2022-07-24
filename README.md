# ktor-health-check

Simple, opinionated Ktor health and readiness checks made for Kubernetes.

**Supported Ktor Version**: 2.x \
Check out the [original Repo](https://github.com/zensum/ktor-health-check) for Builds for older Ktor Versions 

## Installation

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("cc.rbbl:ktor-health-check:2.0.0")
}
```

## Usage

```kotlin
import cc.rbbl.ktor_health_check.Health

fun main(args: Array<String>) {
    embeddedServer(Netty, 80) {
        // Install the middleware...
        install(Health)
    }.start(wait = true)
}
```

... and boom, the your application now exposes a /healthz and /readyz
endpoint for use with for example Kubernetes. For a simple application
this is all you configuration you will ever need. In a more
complicated application we might want to our readycheck to start
failing if the database goes down.

```kotlin
import cc.rbbl.ktor_health_check.Health

fun main(args: Array<String>) {
    embeddedServer(Netty, 80) {
        install(Health) {
            readyCheck("database") { myDatabase.ping() }
        }
    }.start(wait = true)
}
```

And now getting `/readyz` returns:
```
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Content-Length: 17

{"database":true}
```

Let's add another check

```kotlin
install(Health) {
            readyCheck("database") { myDatabase.ping() }
            readyCheck("redis") { redis.ping() }
}
```

Now lets say someone tripped on the cord for our Redis server.

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json; charset=UTF-8
Content-Length: 31

{"database":true,"redis":false}
```

The database check is still returning true, but redis has turned
false. If any single check is down, as is the case here, the result of
the entire request becomes 500, indicating that the service isn't
operational.

For some use-cases you may want to expose checks on URLs other than
`/healthz` and `/readyz`. In that case we need to use `customCheck`

```kotlin
customCheck("/smoketest", "database") { database.test() }
```
And the smoketest should now be available on `/smoketest`
 