# Smart Campus API

## Overview
Smart Campus API is a JAX-RS REST service for managing rooms, sensors, and sensor readings in a university campus environment. It uses in-memory data structures only, which keeps the coursework simple to run, test, and demonstrate.

## How to Build and Run

### Option 1: Terminal (Embedded Grizzly Server)
**Clone the project:**
```bash
git clone https://github.com/thiviru7715/CSA-CW.git
cd CSA-CW
```
**Compile the project:**
```bash
mvn clean compile
```
**Launch the server:**
```bash
mvn exec:java
```
**Verify:** You should see `Smart Campus API started at: http://localhost:8080/api/v1/` in your terminal. Press Enter to stop the server gracefully.

### Option 2: NetBeans IDE (Tomcat Server)
1. Open NetBeans.
2. Select **File > Open Project**.
3. Navigate to the downloaded `CSA-CW` folder and select it (NetBeans will detect it as a Maven project automatically).
4. In the Projects explorer pane, right-click on the `CSA-CW` project name and select **Run**. NetBeans will automatically build and deploy the application to its configured Tomcat server.
5. The API will be available at `http://localhost:8080/api/v1/` (assuming Tomcat is running on port 8080).

### Option 3: Standalone Tomcat Server (WAR Deployment)
**Package the project:** In your terminal, run the following command to create a `.war` file:
```bash
mvn clean package
```
**Locate the WAR file:** Navigate to the `target/` directory inside your project folder. You will find a file named `smart-campus-api-1.0-SNAPSHOT.war` (or simply `ROOT.war` if you renamed it).

**Deploy to Tomcat:** Copy the `.war` file and paste it into the `webapps` folder of your Tomcat installation (e.g., `C:\apache-tomcat-9.0.x\webapps\`).

**Start Tomcat:** Run Tomcat by executing `bin/startup.bat` (Windows) or `bin/startup.sh` (Mac/Linux).

**Verify:** Tomcat will automatically deploy the application. Depending on what you named the `.war` file, the API will be available at `http://localhost:8080/<war-file-name>/api/v1/` (or `http://localhost:8080/api/v1/` if deployed as `ROOT.war`).

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

### Report Answers

### Part 1.1: JAX-RS Lifecycle & Thread Safety

JAX-RS, by default, creates a brand new instance of your resource class for every incoming request — and then just discards it. That sounds fine until you realise it means any data you stored in the object is gone the moment the response is sent.

For something like the Smart Campus API, where rooms and sensors need to persist across many requests, this means the data has to live somewhere outside the resource class itself. To solve this, I created a separate `DataStore` class that acts as a singleton — one shared object that holds all the in-memory data for the lifetime of the application.

The tricky part is that this shared store gets hit by multiple threads at the same time, since a web server handles many requests concurrently. Without proper synchronization, two requests could try to write to the same collection simultaneously and corrupt the data. To handle this, I used `ConcurrentHashMap` for the main room and sensor stores, which handles concurrent reads and writes safely without needing manual locking. For sensor readings, I wrapped the lists with `Collections.synchronizedList`, and for operations that involve multiple steps — like updating a sensor's `currentValue` when a new reading comes in — I used explicit `synchronized` blocks to make sure those compound operations complete atomically.

---

### Part 1.2: HATEOAS & The Discovery Endpoint

HATEOAS stands for Hypermedia as the Engine of Application State, and it sits at the top of the Richardson Maturity Model for REST APIs. The basic idea is that instead of just returning raw data, your API responses also include links that tell the client what it can do next.

The reason this matters is decoupling. Without HATEOAS, client developers have to read through static documentation to know that `/api/v1/rooms` exists. If the server ever changes those paths, every client breaks. With HATEOAS, the client just calls the root endpoint and gets back the links it needs — no hardcoded paths, no outdated docs to worry about.

For this project, the `GET /api/v1` discovery endpoint returns:

```json
{
  "version": "1.0",
  "_links": {
    "rooms":   { "href": "/api/v1/rooms" },
    "sensors": { "href": "/api/v1/sensors" }
  }
}
```

A developer (or an automated system) can start from that one URL and navigate the entire API from there, which is much cleaner than maintaining a separate document that can fall out of sync with the actual implementation.

---

### Part 2.1: Representation Trade-offs (IDs vs. Full Objects)

There are genuine arguments on both sides here. Returning only IDs keeps the initial response very small, which saves bandwidth — especially useful when there are hundreds of rooms. The downside is that the client then has to make a separate request for each room to get any useful information. This is the classic N+1 problem: one request to get the list, then N more requests to get the details. For a campus with 500 rooms, that's 501 HTTP calls just to display a room list.

Returning full objects costs more bandwidth on the first call, but it means the client gets everything it needs in one go. Since each Room object in this API is pretty lightweight — just an ID, name, capacity, and a list of sensor IDs — the payload size is not really a concern. The benefit of saving all those extra round-trips far outweighs the slightly larger response body, especially for mobile clients or dashboard applications that need to render a full list quickly.

For this reason, I chose to return full room objects in the collection response.

---

### Part 2.2: Idempotency in DELETE Operations

Yes, DELETE is idempotent in this implementation. Idempotency means that calling the same operation multiple times produces the same server state as calling it once — it doesn't matter how many times you repeat it, the end result is the same.

In practice, here's what happens:

1. **First DELETE request:** The room exists, gets removed, server returns 204 No Content.
2. **Second DELETE request (same ID):** The room is already gone, server returns 404 Not Found.

The response codes are different, but the state of the server is identical both times — the room doesn't exist. That's what makes it idempotent. This is actually very useful in real systems because if a client sends a DELETE and the network drops before the response arrives, it can safely retry without worrying about accidentally deleting something twice.

It's also worth distinguishing idempotency from safety. GET is both safe and idempotent — it doesn't change anything on the server at all. DELETE is idempotent but not safe, because that first call does change the server state by removing the resource. The subsequent calls just don't change it any further.

---

### Part 3.1: Content-Type Enforcement (@Consumes)

The `@Consumes(MediaType.APPLICATION_JSON)` annotation is basically a contract between the server and the client — it says "this endpoint only accepts JSON." If a client sends a request with a different Content-Type, like `text/plain` or `application/xml`, JAX-RS catches that mismatch before the request even reaches my code.

The framework automatically responds with **HTTP 415 Unsupported Media Type**. This is actually a nice feature because the rejection happens at the routing layer, not in the business logic. My resource method never runs, so there's no risk of a bad payload getting half-processed or causing an unexpected exception. It also means the client gets a clear, standard error code that tells them exactly what went wrong — they sent the wrong format, not the wrong data.

From a stability standpoint, this prevents garbage data from reaching the Jackson deserializer, which could otherwise throw parsing exceptions and potentially crash the request handler if not properly managed.

---

### Part 3.2: Filtering Design (Query vs. Path Parameters)

A useful way to think about this is: the URL path identifies *what* you're looking at, and query parameters describe *how* you want to see it. The path `/api/v1/sensors` refers to the sensors collection. Adding `?type=CO2` doesn't change what resource you're accessing — it just filters the view of that resource.

Using query parameters for filtering is better than putting the filter in the path for a few reasons. First, they're optional by nature — `GET /api/v1/sensors` still works perfectly without any filter, returning everything. Second, they combine easily: adding a second filter is just `?type=CO2&status=ACTIVE` — no need to invent a new URL structure. Third, a URL like `/api/v1/sensors/type/CO2` is misleading — it implies "CO2" is some kind of sub-resource under "type", which it isn't. It's just an attribute value being used as a search term.

Query parameters keep the resource hierarchy clean and make the API much easier to extend later.

---

### Part 4.1: The Sub-Resource Locator Pattern & Complexity Management

When I first looked at how to handle the `/sensors/{id}/readings` path, the obvious approach would have been to just add more `@Path` annotations to the existing `SensorResource` class. But that approach has a serious problem as the API grows.

If every nested endpoint lives in one controller, that class balloons in size very quickly. You end up with a single file handling sensor creation, sensor lookup, sensor filtering, reading history, posting readings, and anything else that gets added later. The class just keeps growing and it becomes a nightmare to navigate. This is what people call a "God Class", and I wanted to avoid that.

Instead, I used a sub-resource locator method in `SensorResource` that simply returns an instance of `SensorReadingResource` when the path hits `{sensorId}/readings`. JAX-RS then hands off the request to that class to handle from there.

`SensorResource` stays focused on sensor-level operations and `SensorReadingResource` stays focused on reading history — each class has one clear job. Testing is also much easier because I can write unit tests for `SensorReadingResource` on its own, without needing to set up the entire sensor registry. The locator also passes the `sensorId` directly into the sub-resource's constructor, so the readings class always knows which sensor's context it's operating in without having to extract path parameters again.

---

### Part 5.2: Semantic Accuracy (422 vs. 404)

Honestly this distinction confused me at first — 404 felt like the obvious choice. But once I thought about it more carefully, 422 makes a lot more sense here.

A 404 means the thing you asked for doesn't exist at this URL. If someone calls `GET /api/v1/rooms/FAKE-999` and that room doesn't exist, 404 is correct. But when a client posts a new sensor with a `roomId` that doesn't exist, the situation is different. The URL they're calling — `POST /api/v1/sensors` — is completely valid. The server understood the request. The JSON was well-formed. The problem is that the `roomId` inside the body references a room that doesn't exist in the system.

Returning 404 in this case would be genuinely confusing — the client would look at their URL and think the `/sensors` endpoint is missing, which it isn't. A **422 Unprocessable Entity** tells them clearly: "I understood your request, but I can't act on it because the data inside it violates a business rule." That's a much more useful signal for developers debugging an integration.

---

### Part 5.4: Cybersecurity Risks of Stack Traces

Returning a raw Java stack trace in an API response is a fairly serious security mistake, even though it might seem harmless on the surface. The information in a stack trace gives an attacker a surprisingly useful map of the system.

The main risks are:

1. **Internal structure exposure** — the trace shows full package and class names, which reveals how the application is organised internally and which code paths handle which operations.
2. **Library version fingerprinting** — stack traces include the names and sometimes versions of third-party libraries like Jersey or Jackson. Once an attacker knows the exact version, they can look up known CVEs for that version and craft targeted exploits.
3. **File system paths** — certain exceptions, like `FileNotFoundException`, include the actual file paths on the server, revealing the directory structure of the deployment.
4. **Injection refinement** — if an attacker deliberately sends malformed input to trigger an exception, the trace tells them exactly which layer rejected it and how far the input got into the system. They can then adjust their attack and try again.

The fix is straightforward: the global `ExceptionMapper<Throwable>` catches everything unexpected, logs the full technical details on the server side where only the development team can see them, and returns a clean generic JSON response to the client — something like `{ "error": "An unexpected error occurred" }`. The client gets a useful signal without any internal details leaking out.

---

### Part 5.5: Cross-Cutting Concerns & Filters

Logging is one of those things that needs to happen for every single request, regardless of what the request is doing. It has nothing to do with creating rooms or registering sensors — it's purely about observability. That kind of concern, which cuts across the whole application without belonging to any one part of it, is called a cross-cutting concern.

The wrong way to handle it is to paste a `Logger.info()` call into every resource method. That works initially, but it creates problems over time. If the log format needs to change, you're editing dozens of methods. If someone adds a new endpoint and forgets to add the log statement, that endpoint goes untracked. The logging becomes inconsistent and unreliable.

Using a `ContainerRequestFilter` and `ContainerResponseFilter` solves this cleanly. The filter runs automatically for every request and response — there's no way to accidentally skip it. The log format is defined in one place, so changes take seconds. And because the filter is its own class, it can be tested independently of the resource methods it wraps. This is essentially the Decorator pattern in practice: you're adding behaviour (logging) around the core logic without touching the core logic itself.

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
