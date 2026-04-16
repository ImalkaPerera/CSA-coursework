# 🏛️ Smart Campus API — 10-Day Implementation Plan
### 5COSC022W Client-Server Architectures Coursework (Due: 24th April 2026, 13:00)

---

> **How to use this plan:** Each day has a clearly defined scope. When you're ready to work on a day, paste the exact prompt from the **"Prompt for AI Agent"** box into your AI assistant and it will complete that day's work end-to-end.

---

## 📅 Overview

| Day | Focus | Parts Covered | Marks at Stake |
|-----|-------|---------------|----------------|
| Day 1 | Maven project setup + Application config | Part 1.1 | 5 |
| Day 2 | Discovery endpoint + POJOs | Part 1.2 | 5 |
| Day 3 | Room Resource (GET all, POST with Location header, GET by ID) | Part 2.1 | 10 |
| Day 4 | Room Deletion + Safety Logic + 409 mapper wired immediately | Part 2.2 | 10 |
| Day 5 | Sensor Resource + Integrity Checks | Part 3.1 | 10 |
| Day 6 | Sensor Filtered Retrieval (case-insensitive) | Part 3.2 | 10 |
| Day 7 | Sub-Resource Locator Pattern | Part 4.1 | 10 |
| Day 8 | Sensor Readings + Side Effects | Part 4.2 | 10 |
| Day 9 | Exception Mappers + Global Safety Net | Part 5.1–5.4 | 25 |
| Day 10 | Logging Filters + README (all 10 Qs) + Polish + Submission | Part 5.5 + Submission | 5 + all |

---

## 📁 Final Project Structure (for reference)

```
smart-campus-api/
├── pom.xml
└── src/
    └── main/
        ├── java/com/smartcampus/
        │   ├── SmartCampusApplication.java
        │   ├── model/
        │   │   ├── Room.java
        │   │   ├── Sensor.java
        │   │   └── SensorReading.java
        │   ├── store/
        │   │   └── DataStore.java
        │   ├── resource/
        │   │   ├── DiscoveryResource.java
        │   │   ├── RoomResource.java
        │   │   ├── SensorResource.java
        │   │   └── SensorReadingResource.java
        │   ├── exception/
        │   │   ├── RoomNotEmptyException.java
        │   │   ├── LinkedResourceNotFoundException.java
        │   │   ├── SensorUnavailableException.java
        │   │   ├── RoomNotEmptyExceptionMapper.java
        │   │   ├── LinkedResourceNotFoundExceptionMapper.java
        │   │   ├── SensorUnavailableExceptionMapper.java
        │   │   └── GlobalExceptionMapper.java
        │   └── filter/
        │       └── LoggingFilter.java
        └── webapp/
            └── WEB-INF/
                └── web.xml
```

---

## 🗓️ Day 1 — Maven Project Setup & Application Configuration

**Goal:** Get a working JAX-RS project that compiles and starts a server on `localhost:8080/api/v1`.

**Covers:** Part 1.1 (5 marks)

**Deliverables:**
- `pom.xml` with Jersey + Grizzly (embedded server)
- `SmartCampusApplication.java` with `@ApplicationPath("/api/v1")`
- `Main.java` to boot the server
- Project compiles with `mvn clean package` and server starts

---

### 📋 Prompt for AI Agent — Day 1

```
I am building a JAX-RS RESTful API called "Smart Campus" for a university coursework.
I need you to set up the full Maven project from scratch.

Requirements:
1. Create a Maven project with groupId: com.smartcampus, artifactId: smart-campus-api, version: 1.0-SNAPSHOT
2. Use Java 11
3. Add dependencies for:
   - Jersey (JAX-RS implementation): jersey-container-grizzly2-http, jersey-media-json-jackson — use version 2.39.1
   - Grizzly embedded server: grizzly-http-server
   - Jackson for JSON: jackson-databind
4. Configure maven-compiler-plugin for Java 11
5. Create SmartCampusApplication.java in src/main/java/com/smartcampus/ that:
   - Extends javax.ws.rs.core.Application
   - Uses @ApplicationPath("/api/v1")
   - Overrides getClasses() to return an empty set (we'll populate later)
6. Create Main.java in the same package that:
   - Boots a Grizzly HTTP server on http://localhost:8080/
   - Prints "Smart Campus API started at http://localhost:8080/api/v1" on startup
   - Waits for Enter key to stop the server
7. Create a DataStore.java singleton class in com.smartcampus.store with:
   - A static ConcurrentHashMap<String, Room> rooms
   - A static ConcurrentHashMap<String, Sensor> sensors
   - A static ConcurrentHashMap<String, List<SensorReading>> sensorReadings
   - Static accessor methods (getRooms(), getSensors(), getSensorReadings())
8. Create stub POJOs (just fields + getters/setters + constructors, no logic yet):
   - Room.java: id (String), name (String), capacity (int), sensorIds (List<String>)
   - Sensor.java: id (String), type (String), status (String), currentValue (double), roomId (String)
   - SensorReading.java: id (String), timestamp (long), value (double)
9. Show me the complete file for each: pom.xml, SmartCampusApplication.java, Main.java, DataStore.java, Room.java, Sensor.java, SensorReading.java

Make sure the project compiles with: mvn clean package
```

---

## 🗓️ Day 2 — Discovery Endpoint

**Goal:** Implement `GET /api/v1` returning API metadata JSON with HATEOAS links.

**Covers:** Part 1.2 (5 marks)

**Deliverables:**
- `DiscoveryResource.java` at path `/`
- Returns JSON: version, contact, links to `/api/v1/rooms` and `/api/v1/sensors`
- Register resource in `SmartCampusApplication.java`

---

### 📋 Prompt for AI Agent — Day 2

```
Continuing my Smart Campus JAX-RS project. All Day 1 files exist (pom.xml, SmartCampusApplication, Main, DataStore, POJOs).

Today I need to implement the Discovery Endpoint.

Requirements:
1. Create DiscoveryResource.java in com.smartcampus.resource:
   - Path: @Path("/") relative to the application root (/api/v1)
   - GET /api/v1 returns a JSON object with:
     {
       "version": "1.0",
       "description": "Smart Campus Sensor & Room Management API",
       "contact": "admin@smartcampus.ac.uk",
       "links": {
         "rooms": "/api/v1/rooms",
         "sensors": "/api/v1/sensors"
       },
       "timestamp": <current epoch millis>
     }
   - Use @Produces(MediaType.APPLICATION_JSON)
   - Return a Response object with status 200

2. Update SmartCampusApplication.java to register DiscoveryResource in getClasses()

3. Add a test: show me the exact curl command to test this endpoint

Give me the complete updated files: DiscoveryResource.java and SmartCampusApplication.java
```

---

## 🗓️ Day 3 — Room Resource (List, Create, Get by ID)

**Goal:** Full CRUD foundation for rooms — list all, create new (with Location header), fetch one.

**Covers:** Part 2.1 (10 marks)

**Deliverables:**
- `RoomResource.java` at `/api/v1/rooms`
- `GET /api/v1/rooms` — return all rooms as JSON array
- `POST /api/v1/rooms` — create room, return **201 with Location header** pointing to the new resource
- `GET /api/v1/rooms/{roomId}` — return specific room or 404

> ✅ **Fix applied:** POST must return a `Location` header — the rubric video demo requirement explicitly says "show 201 Created & Location header". Use `Response.created(uri).entity(room).build()`.

---

### 📋 Prompt for AI Agent — Day 3

```
Continuing my Smart Campus JAX-RS project. Days 1 and 2 are complete.
DataStore exists with ConcurrentHashMap<String, Room> rooms.
Room POJO has: id, name, capacity, sensorIds (List<String>).

Today I need to build the Room Resource.

Requirements:
1. Create RoomResource.java in com.smartcampus.resource at path @Path("/rooms"):
   
   a) GET / → GET /api/v1/rooms
      - Returns Response with status 200 and a JSON array of ALL Room objects from DataStore
      - If no rooms exist, return empty array [] with 200
   
   b) POST / → POST /api/v1/rooms
      - @Consumes(MediaType.APPLICATION_JSON) and @Produces(MediaType.APPLICATION_JSON)
      - Accepts a Room object in the request body
      - Auto-generate a unique id using UUID.randomUUID().toString() if no id is provided
      - Add the room to DataStore.getRooms()
      - Return Response with status 201 (CREATED), the created Room as body, AND a Location header
        pointing to the new resource. Use:
          URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
          return Response.created(location).entity(room).build();
        Inject UriInfo with: @Context UriInfo uriInfo
      - If name is null/empty or capacity <= 0, return 400 Bad Request with JSON:
          {"error": "Invalid room data", "reason": "name and a positive capacity are required"}
   
   c) GET /{roomId} → GET /api/v1/rooms/{roomId}
      - Fetch room from DataStore by roomId
      - Return 200 with room JSON if found
      - Return 404 with JSON error body {"error": "Room not found", "roomId": "<id>"} if not found

2. Register RoomResource in SmartCampusApplication.getClasses()

3. Provide 3 curl commands to test: create a room (verify Location header in response), list all rooms, get room by ID

Give me the complete file: RoomResource.java and updated SmartCampusApplication.java
```

---

## 🗓️ Day 4 — Room Deletion + Safety Logic

**Goal:** Delete rooms, but block deletion if sensors are assigned to it.

**Covers:** Part 2.2 (10 marks)

**Deliverables:**
- `DELETE /api/v1/rooms/{roomId}` implemented
- `RoomNotEmptyException.java` custom exception
- `RoomNotEmptyExceptionMapper.java` returning 409 JSON response — **wired on this day**
- Room with sensors → 409; room without sensors → 204

> ✅ **Fix applied:** The mapper is registered on Day 4 (not deferred to Day 9), so you can demo the 409 response immediately and the video can be recorded at any point after Day 4.

---

### 📋 Prompt for AI Agent — Day 4

```
Continuing my Smart Campus JAX-RS project. Days 1–3 are complete.
RoomResource exists with GET all, POST, GET by ID.
DataStore has rooms (ConcurrentHashMap<String, Room>) and sensors (ConcurrentHashMap<String, Sensor>).
Sensor POJO has: id, type, status, currentValue, roomId.

Today I need Room Deletion with safety logic.

Requirements:
1. Add DELETE /{roomId} to RoomResource.java:
   - If roomId does not exist in DataStore → return 404 with JSON error
   - Check if any Sensor in DataStore.getSensors().values() has roomId == this roomId
   - If sensors found → throw new RoomNotEmptyException(roomId, count_of_sensors)
   - If no sensors → remove from DataStore.getRooms(), return 204 No Content

2. Create RoomNotEmptyException.java in com.smartcampus.exception:
   - Extends RuntimeException
   - Constructor takes (String roomId, int sensorCount)
   - Stores both fields with getters

3. Create RoomNotEmptyExceptionMapper.java in com.smartcampus.exception:
   - Implements ExceptionMapper<RoomNotEmptyException>
   - Annotated with @Provider
   - toResponse() returns:
     - HTTP 409 Conflict
     - JSON body: {"error": "Room cannot be deleted", "reason": "Room still has X active sensor(s) assigned", "roomId": "<id>"}

4. Register BOTH RoomNotEmptyExceptionMapper AND RoomResource in SmartCampusApplication.getClasses()
   (wire the mapper now so the 409 is fully demoable today — do not defer to Day 9)

5. Provide curl commands to test both: successful deletion (204) and blocked deletion (409)

Give me complete files: updated RoomResource.java, RoomNotEmptyException.java, RoomNotEmptyExceptionMapper.java, updated SmartCampusApplication.java
```

---

## 🗓️ Day 5 — Sensor Resource + Integrity Checks

**Goal:** Register sensors with room validation. GET all sensors.

**Covers:** Part 3.1 (10 marks)

**Deliverables:**
- `SensorResource.java` at `/api/v1/sensors`
- `POST /api/v1/sensors` — validates roomId exists, else throws `LinkedResourceNotFoundException`
- `GET /api/v1/sensors` — returns all sensors
- `LinkedResourceNotFoundException.java` + mapper returning 422

---

### 📋 Prompt for AI Agent — Day 5

```
Continuing my Smart Campus JAX-RS project. Days 1–4 are complete.

Today I need the Sensor Resource with integrity checks.

Requirements:
1. Create SensorResource.java in com.smartcampus.resource at @Path("/sensors"):

   a) GET / → GET /api/v1/sensors
      - Returns all Sensor objects from DataStore as JSON array (status 200)
      - Empty array if none exist
   
   b) POST / → POST /api/v1/sensors
      - @Consumes(APPLICATION_JSON), @Produces(APPLICATION_JSON)
      - Accepts Sensor JSON body
      - VALIDATION: Check that sensor.getRoomId() is not null AND exists in DataStore.getRooms()
        - If roomId is null or not found → throw new LinkedResourceNotFoundException(sensor.getRoomId())
      - Auto-generate UUID id if not provided
      - Set default status to "ACTIVE" if status is null or empty
      - Add sensor to DataStore.getSensors()
      - Add sensor's id to the corresponding Room's sensorIds list: DataStore.getRooms().get(roomId).getSensorIds().add(sensorId)
      - Return 201 Created with the created Sensor

   c) GET /{sensorId} → GET /api/v1/sensors/{sensorId}
      - Return sensor by ID or 404 JSON error if not found

2. Create LinkedResourceNotFoundException.java in com.smartcampus.exception:
   - Extends RuntimeException
   - Constructor takes (String missingRoomId)

3. Create LinkedResourceNotFoundExceptionMapper.java:
   - Implements ExceptionMapper<LinkedResourceNotFoundException>
   - @Provider annotated
   - Returns HTTP 422 Unprocessable Entity with JSON:
     {"error": "Dependency validation failed", "reason": "The specified roomId does not exist in the system", "roomId": "<id>"}

4. Register SensorResource and LinkedResourceNotFoundExceptionMapper in SmartCampusApplication

5. Provide curl commands: create sensor with valid roomId (201), create sensor with invalid roomId (422)

Give me complete files: SensorResource.java, LinkedResourceNotFoundException.java, LinkedResourceNotFoundExceptionMapper.java, updated SmartCampusApplication.java
```

---

## 🗓️ Day 6 — Sensor Filtered Retrieval

**Goal:** Add `?type=` query parameter filtering to sensor list endpoint (case-insensitive).

**Covers:** Part 3.2 (10 marks)

**Deliverables:**
- `GET /api/v1/sensors?type=CO2` returns filtered results
- `@QueryParam("type")` used in SensorResource
- Filtering is **case-insensitive** (`CO2`, `co2`, `Co2` all work)
- If `type` param absent → return all sensors (existing behaviour)

> ✅ **Fix applied:** Filtering must use `.equalsIgnoreCase()`. The rubric explicitly separates "Functional" (case-sensitive) from "Excellent/Very Good" (case-insensitive) — this is a free mark upgrade.

---

### 📋 Prompt for AI Agent — Day 6

```
Continuing my Smart Campus JAX-RS project. Days 1–5 are complete.
SensorResource exists with GET all, POST, GET by ID.

Today I need to add filtered retrieval using a query parameter.

Requirements:
1. Modify the GET / method in SensorResource.java to accept an optional @QueryParam("type") String type:
   - If type is null or empty → return all sensors (existing behaviour)
   - If type is provided → filter DataStore.getSensors().values() where:
       sensor.getType().equalsIgnoreCase(type)
   - The filtering MUST be case-insensitive so that ?type=CO2, ?type=co2, and ?type=Co2 all return
     the same results. This is required by the marking rubric.
   - Return filtered list (can be empty []) with status 200

2. Also confirm GET /{sensorId} is present (return 200 with sensor or 404)

3. Provide curl commands to test:
   - GET all sensors (no filter)
   - GET sensors filtered by type=Temperature
   - GET sensors filtered by type=CO2
   - GET sensors filtered by type=co2 (should return same results as CO2)
   - GET sensors with unknown type (should return empty array [])

Give me the updated complete SensorResource.java file only.
```

---

## 🗓️ Day 7 — Sub-Resource Locator Pattern

**Goal:** Implement the sub-resource locator in SensorResource delegating to SensorReadingResource.

**Covers:** Part 4.1 (10 marks)

**Deliverables:**
- Sub-resource locator method in `SensorResource` for path `{sensorId}/readings`
- `SensorReadingResource.java` stub class created and returned
- The sensorId context is passed into the SensorReadingResource instance

---

### 📋 Prompt for AI Agent — Day 7

```
Continuing my Smart Campus JAX-RS project. Days 1–6 are complete.

Today I need to implement the Sub-Resource Locator pattern for sensor readings.

Requirements:
1. In SensorResource.java, add a sub-resource locator method:
   - @Path("{sensorId}/readings")
   - Method name: getSensorReadings(...)
   - Takes @PathParam("sensorId") String sensorId
   - Does NOT have @GET, @POST etc — it is a locator only
   - Validates that the sensorId exists in DataStore.getSensors()
     - If not found → return a 404 response (use javax.ws.rs.WebApplicationException with Response.status(404))
   - Returns: new SensorReadingResource(sensorId)

2. Create SensorReadingResource.java in com.smartcampus.resource:
   - NOT annotated with @Path (path is resolved via the locator)
   - Has a private String sensorId field
   - Constructor: public SensorReadingResource(String sensorId)
   - Add a stub GET / method returning empty list [] for now (we'll implement fully on Day 8)
   - Add a stub POST / method returning 201 for now

3. Inject ResourceContext if needed for JAX-RS container to manage the sub-resource

4. Provide curl commands to test navigation to the sub-resource:
   - GET /api/v1/sensors/{sensorId}/readings (should return [] for now)

Give me complete files: updated SensorResource.java and SensorReadingResource.java
```

---

## 🗓️ Day 8 — Sensor Readings History + Side Effects

**Goal:** Full reading management — log history and update parent sensor's `currentValue`.

**Covers:** Part 4.2 (10 marks)

**Deliverables:**
- `GET /api/v1/sensors/{sensorId}/readings` — return reading history
- `POST /api/v1/sensors/{sensorId}/readings` — add reading + update `currentValue` on Sensor
- `SensorUnavailableException.java` — thrown if sensor is in MAINTENANCE status
- Mapper returning 403 Forbidden

---

### 📋 Prompt for AI Agent — Day 8

```
Continuing my Smart Campus JAX-RS project. Days 1–7 are complete.
SensorReadingResource exists with stubs for GET and POST.
DataStore has sensorReadings: ConcurrentHashMap<String, List<SensorReading>>
SensorReading POJO has: id (String), timestamp (long), value (double)

Today I need to fully implement the readings endpoints and the MAINTENANCE status guard.

Requirements:
1. Fully implement SensorReadingResource.java:

   a) GET / (produces APPLICATION_JSON):
      - Fetch list from DataStore.getSensorReadings().getOrDefault(sensorId, new ArrayList<>())
      - Return 200 with the list

   b) POST / (consumes + produces APPLICATION_JSON):
      - First, retrieve the Sensor from DataStore.getSensors() by sensorId
      - Check sensor.getStatus():
        - If status equals "MAINTENANCE" (case-insensitive) → throw new SensorUnavailableException(sensorId)
      - Accept SensorReading body
      - Auto-generate UUID for reading.id if not provided
      - Set reading.timestamp to System.currentTimeMillis() if timestamp is 0 or null
      - Add reading to DataStore.getSensorReadings() list for this sensorId
        - If no list exists yet, create one first
      - *** SIDE EFFECT ***: Update the parent Sensor's currentValue: sensor.setCurrentValue(reading.getValue())
      - Return 201 Created with the reading as body

2. Create SensorUnavailableException.java in com.smartcampus.exception:
   - Extends RuntimeException
   - Constructor takes (String sensorId)

3. Create SensorUnavailableExceptionMapper.java:
   - Implements ExceptionMapper<SensorUnavailableException>
   - @Provider annotated
   - Returns HTTP 403 Forbidden with JSON:
     {"error": "Sensor unavailable", "reason": "Sensor is currently in MAINTENANCE and cannot accept readings", "sensorId": "<id>"}

4. Register SensorUnavailableExceptionMapper in SmartCampusApplication.getClasses()

5. Provide curl commands:
   - POST a reading to an ACTIVE sensor (201)
   - GET readings for a sensor (200)
   - POST a reading to a MAINTENANCE sensor (403)
   - Verify currentValue updated by GET /api/v1/sensors/{sensorId}

Give me complete files: SensorReadingResource.java, SensorUnavailableException.java, SensorUnavailableExceptionMapper.java, updated SmartCampusApplication.java
```

---

## 🗓️ Day 9 — Exception Mappers + Global Safety Net

**Goal:** Plug all remaining exception gaps. Add catch-all 500 mapper.

**Covers:** Part 5.1–5.4 (25 marks)

**Deliverables:**
- All 4 exception mappers verified: RoomNotEmpty(409), LinkedResourceNotFound(422), SensorUnavailable(403), Global(500)
- Consistent JSON error format across all mappers
- Global `ExceptionMapper<Throwable>` catching unhandled errors

---

### 📋 Prompt for AI Agent — Day 9

```
Continuing my Smart Campus JAX-RS project. Days 1–8 are complete.
I have these exception mappers already registered: RoomNotEmptyExceptionMapper (409), LinkedResourceNotFoundExceptionMapper (422), SensorUnavailableExceptionMapper (403).

Today I need to finalize all exception handling and add the global safety net.

Requirements:
1. Create GlobalExceptionMapper.java in com.smartcampus.exception:
   - Implements ExceptionMapper<Throwable>
   - @Provider annotated
   - toResponse() must:
     - Log the exception using java.util.logging.Logger (log message + exception class name)
     - Return HTTP 500 Internal Server Error with JSON body:
       {"error": "Internal Server Error", "message": "An unexpected error occurred. Please contact support."}
     - CRITICAL: Must NOT expose the stack trace or exception message in the response body

2. Review and fix all existing exception mappers to ensure:
   - All return Content-Type: application/json
   - All JSON bodies use consistent structure: {"error": "...", "reason": "...", ...}
   - RoomNotEmptyExceptionMapper → 409 Conflict ✓
   - LinkedResourceNotFoundExceptionMapper → 422 Unprocessable Entity ✓
   - SensorUnavailableExceptionMapper → 403 Forbidden ✓

3. Create a simple ErrorResponse.java POJO in com.smartcampus.exception (optional but clean):
   - Fields: error (String), reason (String), detail (String)
   - All-args constructor + getters
   - Use this in all mappers for consistent serialization

4. Register GlobalExceptionMapper in SmartCampusApplication.getClasses()

5. Provide a curl command that triggers a 500 (e.g., if you manually add a null somewhere) and show expected response

Give me complete files: GlobalExceptionMapper.java, ErrorResponse.java (if creating), updated SmartCampusApplication.java, and the revised versions of all 3 existing mappers if any changes are needed.
```

---

## 🗓️ Day 10 — Logging Filter + README + Polish + Submission Prep

**Goal:** Add request/response logging filter, write README with all 10 report answers, final test.

**Covers:** Part 5.5 (5 marks) + Submission requirements

**Deliverables:**
- `LoggingFilter.java` implementing both `ContainerRequestFilter` and `ContainerResponseFilter`
- `README.md` with API overview, build instructions, 5+ curl commands, and **all 10 report Q&A answers**
- Full end-to-end smoke test of all endpoints
- GitHub repo ready, video demo checklist

> ✅ **Fix applied:** README prompt now includes all 10 report questions. The original plan was missing Part 5.5 (logging filters vs inline logging). All 10 are listed below.

---

### 📋 Prompt for AI Agent — Day 10

```
Continuing my Smart Campus JAX-RS project. Days 1–9 are complete. All resources and mappers exist.

Today is the final day — I need the logging filter, README, and submission polish.

Requirements:
1. Create LoggingFilter.java in com.smartcampus.filter:
   - Implements BOTH ContainerRequestFilter AND ContainerResponseFilter
   - Annotated with @Provider
   - Uses java.util.logging.Logger (Logger.getLogger(LoggingFilter.class.getName()))
   - ContainerRequestFilter.filter(ContainerRequestContext):
     - Log: "→ [METHOD] URI" e.g. "→ GET /api/v1/rooms"
   - ContainerResponseFilter.filter(ContainerRequestContext, ContainerResponseContext):
     - Log: "← [STATUS_CODE] URI" e.g. "← 200 /api/v1/rooms"
   - Both logs at INFO level

2. Register LoggingFilter in SmartCampusApplication.getClasses()

3. Generate a complete README.md file (this will be submitted as the report) containing:

   ## Smart Campus API
   ### Overview
   [Brief description of the API and its purpose]
   
   ### How to Build & Run
   Step-by-step from clone to running server
   
   ### API Endpoints
   Table of all endpoints with method, path, description
   
   ### Sample curl Commands (at least 5)
   Include curl for: create room, get rooms, create sensor, get sensors filtered, post reading, get readings, delete room
   
   ### Report: Answers to Coursework Questions
   Answer ALL 10 questions from the spec (every answer must be clearly labelled):

   - Part 1.1: JAX-RS resource lifecycle — explain the difference between per-request (default) and
     singleton scope, and the impact on in-memory data synchronisation and thread safety for
     ConcurrentHashMap usage in DataStore.

   - Part 1.2: What is HATEOAS and why does it benefit client developers compared to static API docs?

   - Part 2.1: Full room objects vs IDs only in list responses — analyse bandwidth, payload size,
     and client-side processing trade-offs.

   - Part 2.2: Is DELETE idempotent in this implementation? What happens on the first DELETE call
     vs repeated DELETE calls to the same roomId?

   - Part 3.1: What happens at the JAX-RS framework level when a client sends text/plain (or
     application/xml) to an endpoint annotated with @Consumes(APPLICATION_JSON)?
     What HTTP status code is returned and why?

   - Part 3.2: @QueryParam filtering vs path-based filtering (/sensors/type/CO2) — why are query
     parameters the correct design choice for optional collection filters?

   - Part 4.1: Benefits of the Sub-Resource Locator pattern compared to placing all endpoint logic
     in a single monolithic controller class.

   - Part 5.2: Why is HTTP 422 Unprocessable Entity more semantically accurate than 404 Not Found
     when a request body references a roomId that does not exist?

   - Part 5.4: What specific security risks are introduced by exposing Java stack traces in API
     error responses? Name at least three concrete pieces of information an attacker can extract
     (e.g. internal package paths, library versions, SQL query structure, file system paths).

   - Part 5.5: Why is implementing logging in a ContainerRequestFilter/ContainerResponseFilter
     superior to adding Logger calls inline in every individual resource method?
     Consider DRY, consistency, and maintenance risk.

4. Give me the complete LoggingFilter.java and the complete README.md content.

5. Also give me a final checklist:
   - [ ] mvn clean package succeeds
   - [ ] Server starts on localhost:8080
   - [ ] All 5+ curl commands work as expected
   - [ ] Location header present on POST /api/v1/rooms response
   - [ ] ?type= filtering is case-insensitive (test with mixed case)
   - [ ] GitHub repo is public
   - [ ] README.md is in the repo root and contains all 10 report answers
   - [ ] Video demo recorded (max 10 min, face + voice, Postman tests shown)
   - [ ] PDF report submitted to Blackboard
```

---

## 📌 Quick Reference — All curl Commands (Final)

```bash
# 1. Discovery
curl -X GET http://localhost:8080/api/v1

# 2. Create a Room (check Location header in response)
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Library Quiet Study","capacity":50}'

# 3. Get all Rooms
curl -X GET http://localhost:8080/api/v1/rooms

# 4. Get Room by ID
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301

# 5. Create a Sensor (valid roomId)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"CO2","status":"ACTIVE","currentValue":400.5,"roomId":"<your-room-id>"}'

# 6. Create a Sensor (invalid roomId → 422)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"Temperature","roomId":"FAKE-999"}'

# 7. Get all Sensors
curl -X GET http://localhost:8080/api/v1/sensors

# 8. Get Sensors filtered by type (case-insensitive — test both CO2 and co2)
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
curl -X GET "http://localhost:8080/api/v1/sensors?type=co2"

# 9. Post a Sensor Reading
curl -X POST http://localhost:8080/api/v1/sensors/<sensorId>/readings \
  -H "Content-Type: application/json" \
  -d '{"value":450.0}'

# 10. Get Sensor Readings
curl -X GET http://localhost:8080/api/v1/sensors/<sensorId>/readings

# 11. Delete a Room (no sensors → 204)
curl -X DELETE http://localhost:8080/api/v1/rooms/<roomId>

# 12. Delete a Room with Sensors (→ 409)
curl -X DELETE http://localhost:8080/api/v1/rooms/<roomId-with-sensors>
```

---

## ⚠️ Critical Rules (Do NOT Violate)

| Rule | Penalty |
|------|---------|
| Must use JAX-RS (Jersey) only | ZERO marks if Spring Boot used |
| No database (SQL etc.) — use HashMap/ArrayList only | ZERO marks |
| Must be on public GitHub (no ZIP) | ZERO marks |
| README with build instructions + 5 curl examples | Loses 20% of total |
| Video demo is mandatory | -20% per task without it |
| All 10 report answers must be in README.md | Loses 20% of total |

---

## 📋 Changes Made vs Original Plan

| # | Day | What changed | Why |
|---|-----|-------------|-----|
| 1 | Day 3 | POST /rooms now returns a `Location` header via `Response.created(uri)` | Rubric video demo requirement explicitly says "show 201 Created & Location header" |
| 2 | Day 4 | `RoomNotEmptyExceptionMapper` registered immediately (not deferred to Day 9) | Ensures 409 is fully demoable from Day 4 onward; video can be recorded after Day 9 |
| 3 | Day 6 | Filtering uses `.equalsIgnoreCase()` | Rubric separates "Functional" (case-sensitive) from "Very Good/Excellent" (case-insensitive) |
| 4 | Day 10 | README prompt now lists all 10 report questions (Part 5.5 was missing) | Original plan had only 9; Part 5.5 logging Q was omitted |

---

*Plan generated for 5COSC022W coursework — University of Westminster, 2025/26*
