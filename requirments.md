# 5COSC022W — Client-Server Architectures: Coursework (2025/26)

**University of Westminster — School of Computer Science and Engineering**

| Field | Detail |
|---|---|
| Module Leader | Hamed Hamzeh |
| Unit | Coursework |
| Weighting | 60% of final grade |
| Qualifying Mark | 30% |
| Description | REST API design, development and implementation |
| Handed Out | February 2026 |
| Due Date | **24th April 2026, 13:00** |

---

## Learning Outcomes Covered

- **LO1** — Gain a thorough understanding of RESTful principles and their application in API design.
- **LO2** — Acquire familiarity with the JAX-RS framework as a tool for building RESTful APIs in Java.

---

## Expected Deliverables

- A **public GitHub repository** containing all parts of the project
- A **video demonstration** (max 10 minutes) of Postman tests — uploaded directly to BlackBoard
- A **Report** written in the `README.md` file on GitHub (PDF format, answers to questions only)
- **Electronic submission** on BlackBoard via the provided link

> **Feedback:** Written feedback within 15 working days.

---

## Assessment Regulations

### Late Submission Penalty
- **Within 24 hours / 1 working day** of deadline: **−10 marks**. Work in the 40–49% range is capped at 40%.
- **More than 24 hours late**: **Zero mark**, unless a valid Mitigating Circumstances form has been submitted and accepted.

---

## 1. Introduction & Scenario

**Scenario:** You have been appointed as the Lead Backend Architect for the university's "Smart Campus" initiative. What began as a pilot project for tracking individual temperature sensors has evolved into a comprehensive campus-wide infrastructure project. The university now requires a robust, scalable, and highly available RESTful API to manage thousands of Rooms and the diverse array of Sensors (e.g., CO2 monitors, occupancy trackers, smart lighting controllers) contained within them.

This system will be built as a high-performance web service using **JAX-RS (Jakarta RESTful Web Services)**. Your goal is to provide a seamless interface for campus facilities managers and automated building systems to interact with campus data.

**Objective:** This coursework simulates a real-world development task. It assesses your proficiency in RESTful architectural patterns, the implementation of resource nesting, and the design of a resilient error-handling strategy using JAX-RS. Industry-standard practices are expected, including appropriate HTTP status codes, meaningful JSON responses, and a logical resource hierarchy.

---

## Core Data Models (POJOs)

```java
public class Room {
    private String id;          // Unique identifier, e.g., "LIB-301"
    private String name;        // Human-readable name, e.g., "Library Quiet Study"
    private int capacity;       // Maximum occupancy for safety regulations
    private List<String> sensorIds = new ArrayList<>(); // IDs of sensors in this room

    // Constructors, getters, setters...
}

public class Sensor {
    private String id;            // Unique identifier, e.g., "TEMP-001"
    private String type;          // Category, e.g., "Temperature", "Occupancy", "CO2"
    private String status;        // Current state: "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue;  // The most recent measurement recorded
    private String roomId;        // Foreign key linking to the Room

    // Constructors, getters, setters...
}

public class SensorReading {
    private String id;        // Unique reading event ID (UUID recommended)
    private long timestamp;   // Epoch time (ms) when the reading was captured
    private double value;     // The actual metric value recorded by the hardware

    // Constructors, getters, setters...
}
```

---

## 2. Coursework Tasks

---

### Part 1: Service Architecture & Setup *(10 Marks)*

#### 1.1 — Project & Application Configuration *(5 Marks)*

- Bootstrap a Maven project integrating a JAX-RS implementation (e.g., Jersey) and a lightweight servlet container or embedded server.
- Implement a subclass of `javax.ws.rs.core.Application` and use the `@ApplicationPath("/api/v1")` annotation to establish your API's versioned entry point.

> **Report Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

#### 1.2 — The "Discovery" Endpoint *(5 Marks)*

- Implement a root "Discovery" endpoint at `GET /api/v1`.
- This should return a JSON object providing: versioning info, administrative contact details, and a map of primary resource collections (e.g., `"rooms" -> "/api/v1/rooms"`).

> **Report Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

---

### Part 2: Room Management *(20 Marks)*

#### 2.1 — Room Resource Implementation *(10 Marks)*

Develop a `SensorRoomResource` class to manage the `/api/v1/rooms` path:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/rooms` | Return a comprehensive list of all rooms |
| `POST` | `/api/v1/rooms` | Create a new room; return appropriate feedback on success |
| `GET` | `/api/v1/rooms/{roomId}` | Fetch detailed metadata for a specific room |

> **Report Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

#### 2.2 — Room Deletion & Safety Logic *(10 Marks)*

- Implement `DELETE /api/v1/rooms/{roomId}` for room decommissioning.
- **Business Logic Constraint:** A room **cannot** be deleted if it still has active sensors assigned to it. If attempted, the service must block the request and return a custom error response (see Part 5 — 409 Conflict).

> **Report Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

---

### Part 3: Sensor Operations & Linking *(20 Marks)*

#### 3.1 — Sensor Resource & Integrity *(10 Marks)*

Implement `SensorResource` to manage the `/api/v1/sensors` collection:

- **`POST /`** — When a new sensor is registered, your logic must verify that the `roomId` specified in the request body actually exists in the system.

> **Report Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?

#### 3.2 — Filtered Retrieval & Search *(10 Marks)*

- Enhance `GET /api/v1/sensors` to support an optional query parameter named `type`.
  - Example: `GET /api/v1/sensors?type=CO2`
  - If provided, the response must filter the list to only include matching sensors.

> **Report Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

---

### Part 4: Deep Nesting with Sub-Resources *(20 Marks)*

A key requirement is to maintain a historical log of readings for every sensor on campus.

#### 4.1 — The Sub-Resource Locator Pattern *(10 Marks)*

- In your `SensorResource` class, implement a **sub-resource locator** method for the path `{sensorId}/readings`.
- This method should return an instance of a dedicated `SensorReadingResource` class.

> **Report Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?

#### 4.2 — Historical Data Management *(10 Marks)*

Within `SensorReadingResource`, implement:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Fetch the reading history for that specific sensor |
| `POST` | `/` | Append a new reading for that specific sensor |

- **Side Effect:** A successful `POST` to a reading must trigger an update to the `currentValue` field on the corresponding **parent Sensor** object to ensure data consistency.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging *(30 Marks)*

The API must be "leak-proof" — it should **never** return a raw Java stack trace or a default server error page.

#### 5.1 — Resource Conflict — 409 *(5 Marks)*

- **Scenario:** Attempting to delete a Room that still has Sensors assigned to it.
- **Task:** Create a custom `RoomNotEmptyException`. Implement an Exception Mapper that returns **HTTP 409 Conflict** with a JSON body explaining that the room is currently occupied by active hardware.

#### 5.2 — Dependency Validation — 422 Unprocessable Entity *(10 Marks)*

- **Scenario:** A client attempts to `POST` a new Sensor with a `roomId` that does not exist.
- **Task:** Create a `LinkedResourceNotFoundException`. Use an Exception Mapper to return **HTTP 422 Unprocessable Entity** (or 400 Bad Request).

> **Report Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

#### 5.3 — State Constraint — 403 Forbidden *(5 Marks)*

- **Scenario:** A sensor currently marked with the status `"MAINTENANCE"` cannot accept new readings.
- **Task:** Create a `SensorUnavailableException`. Map this to **HTTP 403 Forbidden** when a `POST` reading is attempted on such a sensor.

#### 5.4 — The Global Safety Net — 500 *(5 Marks)*

- **Task:** Implement a catch-all `ExceptionMapper<Throwable>`. This mapper must intercept any unexpected runtime errors (e.g., `NullPointerException`, `IndexOutOfBoundsException`) and return a generic **HTTP 500 Internal Server Error**.

> **Report Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

#### 5.5 — API Request & Response Logging Filters *(5 Marks)*

- **Task:** Implement API observability by creating a custom filter class that implements both `ContainerRequestFilter` and `ContainerResponseFilter`.
  - Use `java.util.logging.Logger` to log the HTTP method and URI from `ContainerRequestContext` for every incoming request.
  - Log the final HTTP status code from `ContainerResponseContext` for every outgoing response.

> **Report Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

---

## 3. Submission & Marking Breakdown

### Professional Practice (Mandatory Prerequisites)

**GitHub Hosting:** The project must be hosted in a **public GitHub repository**. Your `README.md` must contain:
- An overview of your API design.
- Explicit, step-by-step instructions on how to build the project and launch the server.
- At least **five sample `curl` commands** demonstrating successful interactions with different parts of the API.

**Video Demonstration:** A maximum **10-minute video** demonstrating Postman tests. Must be uploaded directly to the BlackBoard submission link. You **must be present on camera and speak clearly**. No coding portion needed.

**Report:** PDF format, containing **only answers to the questions** in each part. No test cases or introduction needed. Must be written in the `README.md` on GitHub.

---

### Mark Allocation Per Task

| Component | Weight per Task |
|---|---|
| Coding | 50% |
| Video Demonstration | 30% |
| Report (Question Answers) | 20% |

*Example: For a 10-mark task — 5 marks for coding, 3 marks for video demo, 2 marks for report answer.*

### Overall Marks Breakdown

| Part | Description | Marks |
|---|---|---|
| Part 1 | Setup & Discovery | 10 |
| Part 2 | Room Management | 20 |
| Part 3 | Sensors & Filtering | 20 |
| Part 4 | Sub-Resources | 20 |
| Part 5 | Error Handling & Logging | 30 |
| **Total** | | **100** |

---

### Critical Rules & Constraints

> ⚠️ **Technology Stack:** You **must only use JAX-RS**. Using Spring Boot or similar frameworks will result in an **immediate ZERO** for the entire coursework.

> ⚠️ **No ZIP Files:** Submitting a ZIP file instead of hosting the app on GitHub will result in a **ZERO** for the entire coursework.

> ⚠️ **No Databases:** You are **not allowed** to use any database technology (SQL Server, etc.). You must only use in-memory data structures such as `HashMap` or `ArrayList`. Violation results in a **ZERO**.

> ⚠️ **Video is Mandatory:** Failing to submit a recorded video will result in a **20% loss of marks** on each task that requires video recording.

---

## 4. Marking Rubric

### Part 1: Setup & Discovery *(10 Marks)*

#### 1.1 — Architecture & Configuration *(5 Marks)* | Video: N/A

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Expert Setup: Maven project is flawlessly configured with JAX-RS (e.g., Jersey). Application subclass correctly uses `@ApplicationPath("/api/v1")`. Report analyses JAX-RS lifecycle (Request-scoped vs. Singleton) and provides strategies for synchronising in-memory data. |
| **Very Good (60–69%)** | Strong Setup: Project builds correctly. Report accurately explains resource lifecycle and mentions thread-safety for maps/lists, though synchronization details are basic. |
| **Good (50–59%)** | Functional: Base API entry point works. Report explains the default lifecycle correctly but provides limited detail on the impact of concurrency and race conditions. |
| **Satisfactory (40–49%)** | Basic: Entry point established but naming is inconsistent. Report answer is brief, lacking clear connection between the JAX-RS lifecycle and data integrity. |
| **Fail (<30%)** | Non-Functional: Fails to bootstrap the server or activate JAX-RS. Report answer is missing or technically incorrect. |

#### 1.2 — Discovery Endpoint *(5 Marks)* | Video: N/A

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Rich Metadata: `GET /api/v1` returns a complete JSON object including versioning, contact info, and resource maps. Report: Exceptional justification of HATEOAS, clearly articulating the benefits of self-documenting APIs over static docs. |
| **Very Good (60–69%)** | Complete: Returns the required JSON metadata. Report correctly identifies HATEOAS benefits for client developers. |
| **Good (50–59%)** | Sufficient: Discovery endpoint is functional but missing some metadata fields. Report shows basic understanding of hypermedia links in RESTful design. |
| **Satisfactory (40–49%)** | Partial: Endpoint exists but format is not valid JSON or lacks resource maps. Report answer is superficial regarding HATEOAS. |
| **Fail (<30%)** | Broken: Discovery endpoint is missing or returns an error. Report question is ignored. |

---

### Part 2: Room Management *(20 Marks)*

#### 2.1 — Room Implementation *(10 Marks)* | Video: POST a room (show 201 Created & Location header), then GET by new ID

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Clean CRUD: `/rooms` perfectly handles GET (list), POST (create), and GET `{id}` (detail). Report: Analysis of ID-only vs. full-object returns, considering bandwidth and payload overhead. |
| **Very Good (60–69%)** | Standard CRUD: All room endpoints are functional. Report: Good understanding of trade-offs between network bandwidth and client-side processing. |
| **Good (50–59%)** | Functional: Room management works, but JSON feedback on POST is minimal. Report: Identifies basic pros/cons of list return formats. |
| **Satisfactory (40–49%)** | Incomplete: One endpoint (e.g., detail fetch) is missing or buggy. Report: Answer is brief and lacks technical depth. |
| **Fail (<30%)** | Fail: Cannot create or retrieve rooms. Report answer demonstrates a lack of understanding. |

#### 2.2 — Deletion & Safety Logic *(10 Marks)* | Video: DELETE a room (show success), then show 409 Conflict for rooms with sensors

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Robust Integrity: `DELETE {id}` works perfectly and successfully blocks deletion of rooms with active sensors. Report: Justification of idempotency, explaining the exact server state across multiple identical DELETE calls. |
| **Very Good (60–69%)** | Correct Logic: Business constraint is enforced. Report correctly identifies if the operation is idempotent and provides a sound justification. |
| **Good (50–59%)** | Functional: Deletion logic is present but fails to return a custom error body. Report: Basic explanation of idempotency provided. |
| **Satisfactory (40–49%)** | Partial: Room deletion works but the "orphan" check is missing or flawed. Report: Confused or incomplete answer regarding idempotency. |
| **Fail (<30%)** | Broken: Room deletion is not implemented or allows data orphans. Report answer is missing. |

---

### Part 3: Sensors & Filtering *(20 Marks)*

#### 3.1 — Sensor Integrity *(10 Marks)* | Video: POST sensor — show error for non-existent roomId vs. success for valid ID

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Solid Validation: `POST /sensors` validates that the `roomId` exists before registration. Report: Clearly explains the technical consequences (415 Unsupported Media Type) of content-type mismatches in JAX-RS. |
| **Very Good (60–69%)** | Accurate: Foreign key validation is correct. Report accurately describes how JAX-RS handles `@Consumes` mismatches. |
| **Good (50–59%)** | Good: Validation logic is present. Report: Basic understanding of media type enforcement, though may miss specific JAX-RS exception handling details. |
| **Satisfactory (40–49%)** | Basic: Sensor registration works but `roomId` validation is missing. Report: Vague answer regarding `@Consumes`. |
| **Fail (<30%)** | Fail: Sensor registration is broken or ignores JSON payload requirements. Report question is unanswered. |

#### 3.2 — Filtered Retrieval *(10 Marks)* | Video: `GET /sensors?type=...` — change parameter and show dynamic list update

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Dynamic Search: `GET /sensors` supports optional `@QueryParam` filtering by type flawlessly. Report: Insightful contrast between QueryParams and PathParams, justifying why query strings are superior for collection filtering. |
| **Very Good (60–69%)** | Correct Search: Filtering logic works as intended. Report clearly explains the difference between path and query parameters for search. |
| **Good (50–59%)** | Functional: Filtering works but the implementation is case-sensitive or slightly rigid. Report: Adequate explanation of why query parameters are used. |
| **Satisfactory (40–49%)** | Minimal: Endpoint exists but filtering logic is incomplete or ignored. Report lacks comparative analysis between the two designs. |
| **Fail (<30%)** | Broken: No search/filter capability. Report answer is missing or incorrect. |

---

### Part 4: Sub-Resources *(20 Marks)*

#### 4.1 — Sub-Resource Locator *(10 Marks)* | Video: Navigate `/sensors/{id}/readings` — show nested structure in Postman

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Architectural Mastery: Implements a sub-resource locator for `{sensorId}/readings` that returns a separate `SensorReadingResource` class. Report: Detailed discussion on managing complexity and delegation in large APIs. |
| **Very Good (60–69%)** | Correct Pattern: Implementation follows the sub-resource locator pattern correctly. Report: Good explanation of why separate classes help maintainability. |
| **Good (50–59%)** | Functional: Pattern is implemented, but class delegation is slightly messy. Report: Basic understanding of resource nesting and architectural benefits. |
| **Satisfactory (40–49%)** | Partial: Locator is functional but improperly nested or logic is still in the main controller. Report: Superficial answer. |
| **Fail (<30%)** | Fail: Sub-resource locator pattern ignored. Report answer is missing. |

#### 4.2 — Historical Data Management *(10 Marks)* | Video: Navigate `/sensors/{id}/readings` — show nested structure in Postman

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Consistent Data: `SensorReadingResource` supports GET (history) and POST (new reading). POST triggers a flawless update to the parent Sensor's `currentValue`. |
| **Very Good (60–69%)** | Accurate: Reading management works, and the side effect on the parent sensor is correctly implemented. |
| **Good (50–59%)** | Good: Functionality is complete, but side-effect logic (updating parent) is slightly inefficient. |
| **Satisfactory (40–49%)** | Basic: GET/POST for readings work, but the `currentValue` update on the parent is missing. |
| **Fail (<30%)** | Broken: Cannot manage reading history or post new events. |

---

### Part 5: Error Handling *(30 Marks)*

#### 5.1 — Specific Exceptions *(20 Marks)* | Video: Show 422 (Unprocessable) or 403 (Forbidden) with JSON bodies

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Leak-Proof API: Flawless `ExceptionMapper` implementations for 409 (Conflict), 422 (Unprocessable Entity), and 403 (Forbidden). All return structured JSON error bodies. Report: Analysis of why 422 is superior to 404 for payload reference issues. |
| **Very Good (60–69%)** | Robust: All three specific mappers work with appropriate status codes and JSON feedback. Report: Accurate semantic justification for using 422. |
| **Good (50–59%)** | Functional: Most mappers work, but error messages are generic or one mapper is missing. Report: Basic understanding of 422 vs 404. |
| **Satisfactory (40–49%)** | Minimal: Error mapping is inconsistent; some scenarios still result in default server pages. Report: Answer is weak. |
| **Fail (<30%)** | Fail: No custom exceptions or mappers. Report answer is missing. |

#### 5.2 — Global Safety Net *(10 Marks)* | Video: Trigger a 500 error — prove NO stack trace is visible in response

| Grade | Criteria |
|---|---|
| **Excellent (70%+)** | Security Expert: Catch-all `ExceptionMapper<Throwable>` returns a clean 500 status. Report: Analysis of cybersecurity risks, detailing how stack traces expose internal paths, library versions, and logic flaws. |
| **Very Good (60–69%)** | Secure: Global mapper is active and intercepts unexpected errors. Report correctly identifies the risks of technical information disclosure to attackers. |
| **Good (50–59%)** | Functional: Global mapper works but error body is slightly too technical. Report: General understanding of security risks associated with stack traces. |
| **Satisfactory (40–49%)** | Partial: Mapper exists but fails to intercept all `Throwable` types. Report: Answer is brief and lacks specifics on what an attacker gathers. |
| **Fail (<30%)** | Fail: No global mapper; raw stack traces are exposed to consumers. Report answer is missing. |

---

*End of Document — 5COSC022W Smart Campus Coursework Specification & Rubric*