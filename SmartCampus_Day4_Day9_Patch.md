# 🔧 Patch: Day 4 & Day 9 — Corrected Sections
### Replace these two sections in `SmartCampus_10Day_Plan_Corrected.md`

---

## 🗓️ Day 4 — Room Deletion + Safety Logic

**Goal:** Delete rooms, but block deletion if sensors are assigned to it.

**Covers:** Part 2.2 (10 marks)

**Deliverables:**
- `DELETE /api/v1/rooms/{roomId}` implemented
- Explicit **404 returned via `WebApplicationException`** when roomId is not found — **not thrown as a plain exception** so it bypasses the global 500 mapper
- `RoomNotEmptyException.java` custom exception
- `RoomNotEmptyExceptionMapper.java` returning 409 JSON response — **wired on this day**
- Room with sensors → 409; room without sensors → 204; room not found → 404

> ✅ **Fix applied (Gap 1):** The 404 "room not found" branch now explicitly uses `throw new WebApplicationException(Response.status(404).entity(...).type(MediaType.APPLICATION_JSON).build())` so that the global `ExceptionMapper<Throwable>` on Day 9 does **not** accidentally intercept and swallow it as a 500. A plain `return Response.status(404)...build()` also works — the key constraint is to never throw a raw unchecked exception for this branch.

> ✅ **Fix applied (original):** The mapper is registered on Day 4 (not deferred to Day 9), so you can demo the 409 response immediately and the video can be recorded at any point after Day 4.

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

   IMPORTANT — follow this exact order and pattern:

   a) Look up the room: Room room = DataStore.getRooms().get(roomId)
   b) If room is null → do NOT throw a plain RuntimeException.
      Instead, build and return (or throw as WebApplicationException) a 404 response:
        Map<String, String> err = new HashMap<>();
        err.put("error", "Room not found");
        err.put("roomId", roomId);
        throw new WebApplicationException(
            Response.status(Response.Status.NOT_FOUND)
                    .entity(err)
                    .type(MediaType.APPLICATION_JSON)
                    .build()
        );
      This is critical: WebApplicationException is NOT caught by ExceptionMapper<Throwable>,
      so the global 500 mapper will never intercept a deliberate 404.

   c) Count sensors assigned to this room:
        long count = DataStore.getSensors().values().stream()
                        .filter(s -> roomId.equals(s.getRoomId()))
                        .count();
   d) If count > 0 → throw new RoomNotEmptyException(roomId, (int) count)
   e) If count == 0 → DataStore.getRooms().remove(roomId), return Response.noContent().build() (204)

2. Create RoomNotEmptyException.java in com.smartcampus.exception:
   - Extends RuntimeException
   - Constructor takes (String roomId, int sensorCount)
   - Stores both fields with getters

3. Create RoomNotEmptyExceptionMapper.java in com.smartcampus.exception:
   - Implements ExceptionMapper<RoomNotEmptyException>
   - Annotated with @Provider
   - toResponse() returns:
     - HTTP 409 Conflict
     - Content-Type: application/json
     - JSON body: {"error": "Room cannot be deleted", "reason": "Room still has X active sensor(s) assigned", "roomId": "<id>"}

4. Register BOTH RoomNotEmptyExceptionMapper AND RoomResource in SmartCampusApplication.getClasses()
   (wire the mapper now so the 409 is fully demoable today — do not defer to Day 9)

5. Provide curl commands to test all three branches:
   - DELETE a room that does not exist → 404
   - DELETE a room that has sensors → 409
   - DELETE a room with no sensors → 204

Give me complete files: updated RoomResource.java, RoomNotEmptyException.java, RoomNotEmptyExceptionMapper.java, updated SmartCampusApplication.java
```

---

## 🗓️ Day 9 — Global Safety Net + Exception Consistency Audit

**Goal:** Add the catch-all 500 mapper and audit all existing mappers for consistency. This day does NOT need to re-create the 403 mapper — that was fully built and registered on Day 8.

**Covers:** Part 5.1–5.4 (25 marks)

**Deliverables:**
- `GlobalExceptionMapper.java` — `ExceptionMapper<Throwable>` → 500
- `ErrorResponse.java` POJO for consistent JSON across all mappers
- All 4 mappers updated to use `ErrorResponse` and return `application/json`
- Clarity note: `SensorUnavailableExceptionMapper` (403) was **created and registered on Day 8** — today's job is audit + consistency only, not re-creation

> ✅ **Fix applied (Gap 2):** The Day 9 goal and prompt now clearly state that the 403 mapper already exists from Day 8. The prompt instructs the AI to *review and update* the existing mapper (not create it from scratch), so the agent won't duplicate it or get confused. Day 9's scope is: (1) add the 500 global net, (2) introduce `ErrorResponse` POJO, (3) retrofit all 3 existing mappers to use it consistently.

---

### 📋 Prompt for AI Agent — Day 9

```
Continuing my Smart Campus JAX-RS project. Days 1–8 are complete.

The following exception mappers are ALREADY CREATED AND REGISTERED in SmartCampusApplication:
  - RoomNotEmptyExceptionMapper        → HTTP 409  (created Day 4)
  - LinkedResourceNotFoundExceptionMapper → HTTP 422  (created Day 5)
  - SensorUnavailableExceptionMapper   → HTTP 403  (created Day 8)

Today's job is:
  1. Add the global 500 safety net
  2. Introduce a shared ErrorResponse POJO
  3. Retrofit the 3 existing mappers to use it (do NOT recreate them from scratch — update them)

Requirements:

1. Create ErrorResponse.java in com.smartcampus.exception:
   - Fields: error (String), reason (String), detail (String)
   - All-args constructor: public ErrorResponse(String error, String reason, String detail)
   - Getters only (no setters needed)
   - This will be used by all 4 mappers for consistent JSON serialization

2. Create GlobalExceptionMapper.java in com.smartcampus.exception:
   - Implements ExceptionMapper<Throwable>
   - Annotated with @Provider
   - Uses java.util.logging.Logger to log: the exception class name and message (server-side only)
   - toResponse() returns:
     - HTTP 500 Internal Server Error
     - Content-Type: application/json
     - Body (using ErrorResponse): {"error": "Internal Server Error", "reason": "An unexpected error occurred. Please contact support.", "detail": ""}
     - CRITICAL: Must NOT include the exception message, stack trace, or any internal detail in the response body

   IMPORTANT NOTE ON WebApplicationException:
   JAX-RS runtimes (Jersey) do NOT pass WebApplicationException through ExceptionMapper<Throwable>
   by default — they are handled directly. This means the deliberate 404s built with
   WebApplicationException in Day 4 (and elsewhere) are safe and will NOT be intercepted by
   GlobalExceptionMapper. You do not need to add any special handling for this.

3. Update the 3 existing mappers to use ErrorResponse:
   - RoomNotEmptyExceptionMapper:
       return Response.status(409)
           .entity(new ErrorResponse("Room cannot be deleted",
               "Room still has " + e.getSensorCount() + " active sensor(s) assigned",
               e.getRoomId()))
           .type(MediaType.APPLICATION_JSON).build();

   - LinkedResourceNotFoundExceptionMapper:
       return Response.status(422)
           .entity(new ErrorResponse("Dependency validation failed",
               "The specified roomId does not exist in the system",
               e.getMissingRoomId()))
           .type(MediaType.APPLICATION_JSON).build();

   - SensorUnavailableExceptionMapper:
       return Response.status(403)
           .entity(new ErrorResponse("Sensor unavailable",
               "Sensor is currently in MAINTENANCE and cannot accept readings",
               e.getSensorId()))
           .type(MediaType.APPLICATION_JSON).build();

4. Register GlobalExceptionMapper in SmartCampusApplication.getClasses()
   (the other 3 are already registered — do not remove them)

5. Provide a curl command that triggers a 500:
   - Suggest a temporary null dereference in any resource method to prove the global mapper fires
   - Show the expected response body (generic, no stack trace)

Give me complete files:
  - GlobalExceptionMapper.java (new)
  - ErrorResponse.java (new)
  - Updated RoomNotEmptyExceptionMapper.java
  - Updated LinkedResourceNotFoundExceptionMapper.java
  - Updated SensorUnavailableExceptionMapper.java
  - Updated SmartCampusApplication.java (GlobalExceptionMapper added to getClasses())
```

---

## 📋 Summary of Changes in This Patch

| Gap | Day | What was wrong | What's fixed |
|-----|-----|----------------|--------------|
| Gap 1 | Day 4 | 404 branch could be caught by the future global 500 mapper if thrown as a plain exception | Prompt now explicitly uses `WebApplicationException` with a built `Response` — Jersey bypasses `ExceptionMapper<Throwable>` for these, guaranteeing a clean 404 |
| Gap 2 | Day 9 | Day 9 summary/prompt implied all 4 mappers were being created on that day, causing potential confusion about the 403 mapper already built on Day 8 | Prompt now explicitly states which mappers already exist, instructs the AI to *update* (not re-create) them, and clarifies Day 9 scope as: global net + `ErrorResponse` + consistency audit |
