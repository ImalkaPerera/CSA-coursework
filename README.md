# Smart Campus API

## Overview
Smart Campus API is a JAX-RS REST service for managing rooms, sensors, and sensor readings in a university campus environment. It uses in-memory data structures only, which keeps the coursework simple to run, test, and demonstrate.

## How to Build and Run
### 1. Clone the repository
```bash
git clone <your-github-repo-url>
cd CSA-coursework
```

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
JAX-RS resources are per-request by default, so a new resource instance is created for each request. That keeps request handling stateless and avoids shared mutable fields inside a resource class. In this project, shared data lives in the static `DataStore` maps instead. Because multiple requests can access those maps at the same time, `ConcurrentHashMap` is used to make basic access thread-safe. That said, thread-safe collections do not automatically make multi-step workflows atomic, so compound operations still need careful design.

### Part 1.2
HATEOAS means the API returns links that tell the client what to do next. That makes the service more discoverable because clients can follow links instead of hardcoding every URI. Compared with static API docs alone, HATEOAS is more flexible at runtime and reduces coupling between the client and the server's URI structure.

### Part 2.1
Returning full room objects gives clients immediate access to the room name, capacity, and sensor IDs without extra requests. That improves usability and reduces client-side complexity. The trade-off is a larger response payload and slightly more bandwidth use. Returning only IDs is lighter on the network, but it pushes extra lookup work onto the client and usually leads to more requests. For this coursework API, full room objects are the better trade-off because the data set is small and the client benefits from having the details immediately.

### Part 2.2
Yes, DELETE is idempotent in this implementation. The first DELETE on an existing room removes it and returns 204 No Content if the room has no sensors. Repeating the same DELETE after the room is already gone returns 404 Not Found, but it does not change the server state any further. That is still consistent with idempotent behavior because repeated identical requests do not keep modifying the resource state.

### Part 3.1
When a client sends `text/plain` or `application/xml` to an endpoint that only consumes JSON, Jersey rejects the request before the resource method runs. The framework returns 415 Unsupported Media Type because the request body does not match the method's declared `@Consumes(APPLICATION_JSON)`. This is a framework-level media type validation failure, not a business logic error.

### Part 3.2
Query parameters are the right design for optional collection filters because they refine a collection without changing the resource identity. `/sensors?type=CO2` still points to the sensors collection, just with a filter applied. A path-based design like `/sensors/type/CO2` is less flexible for optional filters and becomes awkward as more filters are added. Query parameters also support combinations such as filtering, paging, and sorting more naturally.

### Part 4.1
The Sub-Resource Locator pattern keeps the API better organized by delegating nested resource behavior to a separate class. That means sensor management and sensor-reading management do not end up in one large controller. It improves separation of concerns, makes the code easier to test, and keeps the parent resource focused on navigation rather than all child behavior.

### Part 5.2
HTTP 422 Unprocessable Entity is more accurate than 404 Not Found when the request body references a roomId that does not exist because the endpoint itself exists and the JSON can be parsed. The problem is semantic validation of the submitted data, not a missing route. The server understands the request but cannot process it because the linked room is invalid, so 422 communicates the failure more precisely.

### Part 5.4
Exposing Java stack traces in API responses is risky because it leaks internal implementation details to the client. An attacker can learn internal package names, class names, file system paths, framework or library versions, method names, and sometimes SQL or data-access structure. That information helps with reconnaissance and makes targeted attacks easier. For that reason, stack traces should remain server-side only.

### Part 5.5
Logging in a `ContainerRequestFilter` and `ContainerResponseFilter` is better than putting logger calls in every resource method because it is DRY, consistent, and easier to maintain. One filter gives the same logging format for every endpoint without duplicating code across controllers. It also reduces the chance that a resource method is missed or logged differently, and any logging change can be made in one place.

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
