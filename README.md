# Smart Campus API

## Overview
Smart Campus API is a JAX-RS REST service for managing rooms, sensors, and sensor readings in a university campus environment. It uses in-memory data structures only, which keeps the coursework simple to run, test, and demonstrate.

## How to Build and Run
### 1. Clone the repository
```bash
git clone <your-github-repo-url>
cd CSA-coursework
```

### Open in NetBeans (Marker-Friendly)
1. Open NetBeans.
2. Go to File -> Open Project.
3. Select the repository folder that contains pom.xml.
4. NetBeans will detect it as a Maven project automatically.
5. Use the Projects view, then run the main class `com.smartcampus.Main`.

### 2. Build the project
```bash
mvn clean package
```

### 3. Run the API
```bash
mvn exec:java
```

The API starts at:

```text
http://localhost:8080/api/v1
```

## API Endpoints
| Method | Path | Description |
| --- | --- | --- |
| GET | /api/v1 | API discovery endpoint |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{roomId} | Get a room by ID |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | List all sensors, optionally filtered by type |
| POST | /api/v1/sensors | Create a sensor |
| GET | /api/v1/sensors/{sensorId} | Get a sensor by ID |
| GET | /api/v1/sensors/{sensorId}/readings | Get reading history for a sensor |
| POST | /api/v1/sensors/{sensorId}/readings | Add a sensor reading |

## Sample curl Commands
### 1. Discover the API
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a room and check the Location header
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Library Quiet Study","capacity":50}'
```

### 3. List all rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Create a sensor with a valid roomId
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"CO2","status":"ACTIVE","currentValue":400.5,"roomId":"<room-id>"}'
```

### 5. List sensors
```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

### 6. Filter sensors by type (case-insensitive)
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
curl -X GET "http://localhost:8080/api/v1/sensors?type=co2"
```

### 7. Post a sensor reading
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/<sensorId>/readings \
  -H "Content-Type: application/json" \
  -d '{"value":450.0}'
```

### 8. Get sensor readings
```bash
curl -X GET http://localhost:8080/api/v1/sensors/<sensorId>/readings
```

### 9. Delete a room
```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/<roomId>
```

## Report Answers

### Part 1.1
By default, JAX-RS follows a per-request lifecycle for resource classes. A new resource instance is created for each incoming HTTP request and discarded after the response is produced.

This directly affects in-memory data management. If maps or lists are kept as normal instance fields inside resource classes, they are recreated on every request and previously stored data is lost. Shared state must therefore be stored outside the per-request resource lifecycle, typically in a singleton store class such as DataStore.

Because the store is shared across many request threads, thread safety is essential. ConcurrentHashMap is appropriate for room and sensor maps, and synchronized or concurrent list strategies are needed for shared reading collections. Compound operations still need careful synchronization.

In summary, per-request lifecycle supports stateless resource design, but requires external shared storage with explicit concurrency control.

### Part 1.2
HATEOAS is a key sign of advanced REST maturity because responses include navigational links, not just data. Instead of relying on hardcoded endpoints, clients discover available actions from the response itself.

This reduces coupling between client and server. If endpoint paths change, clients that follow links remain functional. It also improves developer experience because clients can navigate the API dynamically without depending entirely on static documents.

For this Smart Campus API, the discovery endpoint at GET /api/v1 acts as the entry point by exposing links to major collections such as rooms and sensors.

### Part 2.1
Returning only IDs reduces payload size, but it often creates an N+1 request problem because clients must call GET for each room ID to retrieve details.

Returning full room objects increases payload size but significantly reduces latency and client complexity by delivering useful information in one request.

For this coursework, full objects are the better trade-off because room representations are small and the API remains simpler for client applications.

### Part 2.2
DELETE is idempotent in this implementation in terms of server state.

First request: if the room exists and can be removed, it is deleted and the API returns success (typically 204). Repeated request for the same room ID: the room is already absent, so the API returns 404.

Although the status codes differ, the resulting server state is unchanged after the first successful delete. This satisfies idempotency and makes retries safe.

### Part 3.1
The annotation @Consumes(MediaType.APPLICATION_JSON) defines a strict media-type contract for the endpoint.

If a client sends text/plain or application/xml, JAX-RS rejects the request before resource method execution and returns HTTP 415 Unsupported Media Type.

This is handled by framework-level method and message-body matching, which improves robustness by enforcing content constraints before business logic runs.

### Part 3.2
Using query parameters for filtering is more RESTful than embedding filters in path segments.

Path identifies the resource, while query parameters refine the returned representation. GET /api/v1/sensors identifies the collection, and type=CO2 filters that collection view.

Query parameters also scale better for optional and combinable filters, such as type, status, and room, without creating rigid endpoint patterns.

### Part 4.1
The sub-resource locator pattern improves maintainability by delegating nested path handling to focused classes.

Instead of placing all sensor and reading operations in one large resource class, SensorResource routes to SensorReadingResource for readings-related endpoints.

This provides clearer separation of concerns, simpler testing, and easier growth as additional nested resources are added.

### Part 5.2
HTTP 422 Unprocessable Entity is more accurate than 404 for a missing linked roomId inside a valid sensor creation payload.

404 means the URL resource itself was not found. In this case, POST /api/v1/sensors is valid and reachable, and the JSON syntax is valid. The failure is semantic: roomId references a non-existent room.

Therefore 422 communicates the exact problem: request understood, but entity content is semantically invalid.

### Part 5.4
Exposing raw Java stack traces is a security risk because it reveals internal implementation details to external clients.

Stack traces may leak package and class structure, framework internals, file system paths, method flow, and version fingerprints that can help attackers identify exploitable weaknesses.

A safer design is to log full exception details server-side and return a generic HTTP 500 response body to the client through a global exception mapper.

### Part 5.5
Using JAX-RS request and response filters for logging is better than adding manual log statements in every resource method.

Filters centralize this cross-cutting concern, guarantee consistent logging across all endpoints, and reduce maintenance overhead.

They also preserve clean business-focused resource code and allow format or policy changes in one location.

## Final Checklist
- [ ] `mvn clean package` succeeds
- [ ] Server starts on `localhost:8080`
- [ ] All 5+ curl commands work as expected
- [ ] `Location` header present on `POST /api/v1/rooms` response
- [ ] `?type=` filtering is case-insensitive (test with mixed case)
- [ ] GitHub repo is public
- [ ] `README.md` is in the repo root and contains all 10 report answers
- [ ] Video demo recorded (max 10 min, face + voice, Postman tests shown)
- [ ] PDF report submitted to Blackboard
