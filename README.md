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

## Report Answers

### Part 1.1: JAX-RS Lifecycle & Thread Safety
By default, JAX-RS follows a **per-request lifecycle** for resource classes. This means the JAX-RS runtime (Jersey) instantiates a new instance of the resource class for every incoming HTTP request and discards it once the response is produced.

This architectural decision has critical implications for state management. Because resource instances are short-lived, any data stored in normal instance fields would be lost between requests. To maintain a persistent state across the campus infrastructure, we must externalize our data storage. In this project, I implemented a **Singleton DataStore** pattern.

Since this shared store is accessed by multiple concurrent request threads, **thread safety** is paramount. I utilized `ConcurrentHashMap` for primary resource collections (Rooms and Sensors) to allow high-concurrency reads and writes without blocking. For nested collections, such as sensor readings, I employed `Collections.synchronizedList` and explicit `synchronized` blocks for compound operations (like updating a sensor's `currentValue` during a reading post) to prevent race conditions and ensure data consistency.

### Part 1.2: HATEOAS & The Discovery Endpoint
HATEOAS (Hypermedia as the Engine of Application State) is the final level of the Richardson Maturity Model. It transforms an API from a collection of static endpoints into a **self-documenting, navigable web of resources**.

By providing hypermedia links (such as those in our `GET /api/v1` response), we decouple the client from the server's URI structure. Client developers no longer need to hardcode paths; they can discover available actions dynamically from the response body. This approach is superior to static documentation because the API becomes "discoverable" in real-time, allowing for smoother versioning and reduced client-side maintenance when the resource hierarchy evolves.

**Example HATEOAS Discovery Response:**
```json
{
  "version": "1.0",
  "_links": {
    "rooms":   { "href": "/api/v1/rooms" },
    "sensors": { "href": "/api/v1/sensors" }
  }
}
```

### Part 2.1: Representation Trade-offs (IDs vs. Full Objects)
When returning collections, returning only IDs minimizes the initial payload size and saves network bandwidth. However, it often forces the client into an **N+1 request problem**, where the client must make a separate GET request for every single item to retrieve useful metadata.

Returning full objects increases the initial response size but significantly reduces **latency** and total round-trips. For the Smart Campus API, where room data is relatively lightweight, returning full objects is the optimal trade-off. It provides a more "greedy" and efficient developer experience, as the client receives all necessary context (like capacity and sensor lists) in a single operation, which is particularly beneficial for mobile or low-latency applications.

### Part 2.2: Idempotency in DELETE Operations
The `DELETE` operation in this implementation is **idempotent**. An operation is considered idempotent if making multiple identical requests has the same effect on the server's state as a single request.

In our system:
1. **First Request:** The room is found and removed from the `DataStore` (Status 204 No Content).
2. **Subsequent Requests:** The room no longer exists, so the server returns a 404 Not Found.

Although the HTTP status codes differ between the first and subsequent calls, the **end-state of the server** is identical: the room is gone. This satisfies the definition of idempotency, allowing clients to safely retry a DELETE request if they experience a network timeout without worrying about unintended side effects. It is important to note that idempotency is distinct from safety — GET is both safe and idempotent, whereas DELETE is idempotent but not safe, since the first call does alter server state.

### Part 3.1: Content-Type Enforcement (@Consumes)
The `@Consumes(MediaType.APPLICATION_JSON)` annotation establishes a strict **media-type contract**. If a client attempts to POST data using an unsupported format (like `text/plain` or `application/xml`), the JAX-RS runtime intercepts the request before it even reaches our business logic.

The server will automatically respond with an **HTTP 415 Unsupported Media Type**. This framework-level enforcement is a critical security and stability feature; it ensures that our Jackson provider only attempts to parse valid JSON payloads, preventing potential parsing exceptions, malformed data injection, or "garbage-in" scenarios that could crash the service.

### Part 3.2: Filtering Design (Query vs. Path Parameters)
In RESTful design, the **URL Path** identifies the resource or collection (the "Noun"), while **Query Parameters** are used to refine, filter, or sort the representation (the "Adjectives").

Using query parameters for filtering (`GET /sensors?type=CO2`) is superior because:
1. **Scalability:** It allows for optional and combinable filters (e.g., `?type=CO2&status=ACTIVE`) without creating a combinatorial explosion of path segments.
2. **Clarity:** It maintains a clean resource hierarchy. Paths like `/sensors/type/CO2` falsely imply that "CO2" is a sub-resource, whereas it is actually just a metadata attribute of the sensor collection.
3. **Caching:** Query strings are more naturally handled by caches as variants of the same base resource.

### Part 4.1: The Sub-Resource Locator Pattern & Complexity Management
The Sub-Resource Locator pattern is a sophisticated JAX-RS mechanism for managing **architectural complexity** in large-scale APIs. In a "Smart Campus" scenario where resources like rooms and sensors have deep hierarchical relationships (e.g., `/sensors/{id}/readings`), the URI space can quickly become unmanageable.

#### The Pitfalls of a Monolithic Controller
Without sub-resource locators, one would be forced to create a "Monolithic Controller" (or God Class) that defines every possible nested path using hardcoded `@Path` annotations (e.g., `@Path("/sensors/{id}/readings")`). This approach fails in three key areas:
1. **Maintenance:** The class becomes thousands of lines long, making it difficult to read and prone to "Merge Hell" in team environments.
2. **Cognitive Load:** It mixes different domains (sensor registry vs. time-series readings) in a single file, violating the Single Responsibility Principle.
3. **Fragility:** Any change to the base path requires updating every single nested method.

#### Benefits of the Delegated Approach
By using a **locator method** in `SensorResource` that returns a `SensorReadingResource` object, we achieve several professional advantages:
*   **Separation of Concerns:** `SensorResource` handles device identity and routing, while `SensorReadingResource` is a "POJO-like" resource focused purely on historical data operations.
*   **Enhanced Testability:** Because `SensorReadingResource` is a focused, decoupled class, we can unit test its GET and POST methods in isolation without spinning up the entire sensor registry logic. This makes our test suite faster and more resilient.
*   **Reusability:** If our campus expands to include other devices that also generate readings (e.g., smart meters or vehicle trackers), we can **reuse** the `SensorReadingResource` class across multiple parent locators, significantly reducing code duplication.
*   **Dynamic Routing:** The locator pattern allows us to inject state (like the `sensorId`) into the sub-resource's constructor, ensuring that the sub-resource always operates within the correct context without needing to repeatedly extract path parameters.

This modularity ensures that the Smart Campus API can scale to support hundreds of nested resource types while remaining clean, testable, and maintainable.

### Part 5.2: Semantic Accuracy (422 vs. 404)
HTTP **422 Unprocessable Entity** is semantically superior to a standard 404 when handling invalid references within a payload. 

A **404 Not Found** implies that the URI itself is incorrect or the resource does not exist. However, when posting a sensor, the endpoint `/api/v1/sensors` is valid. The error is not in the "where," but in the "what"—the payload is syntactically correct JSON but **semantically invalid** because it references a non-existent `roomId`. Using 422 communicates to the developer that the request was understood, but the business rules (referential integrity) were violated, which is far more descriptive than a generic "Not Found" error.

### Part 5.4: Cybersecurity Risks of Stack Traces
Exposing raw Java stack traces to external consumers is a high-risk **Information Disclosure** vulnerability. An attacker can use the trace to map out:
1. **Class Structure:** Internal package names and class logic flow.
2. **Library Fingerprints:** Specific versions of frameworks like Jersey or Jackson, allowing them to target known CVEs.
3. **Internal Paths:** Physical file system paths on the server.
4. **Injection Attack Refinement:** If a malformed input triggered the exception, the stack trace reveals exactly which layer rejected it and how far the input penetrated — allowing an attacker to iteratively refine their payload until it passes deeper into the system logic.

By using a global `ExceptionMapper<Throwable>`, we implement a **security boundary**. We log the technical details privately on the server for debugging but return a generic, non-descriptive JSON 500 error to the client. This "obfuscation" is a standard industry practice to reduce the attack surface of the API.

### Part 5.5: Cross-Cutting Concerns & Filters
Using JAX-RS Filters to handle logging is an example of addressing **Cross-Cutting Concerns**. These are tasks (like logging, authentication, or compression) that apply to almost every part of the application but aren't part of the core business logic.

Centralizing logging in a `ContainerRequestFilter` and `ContainerResponseFilter` ensures **Global Consistency**—every single request and response is tracked automatically. This approach follows the **DRY (Don't Repeat Yourself)** principle; if we manually inserted log statements into every resource method, the code would become cluttered, and any change to the logging format would require editing dozens of files. Filters allow us to manage observability in one single location. Architecturally, these filters represent the **Decorator design pattern**, allowing us to wrap core business logic with additional functionality (logging) without modifying the resources themselves. This makes the filters independently testable and maintainable.

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
