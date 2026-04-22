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
By default JAX-RS creates a brand new instance of each resource class for every incoming request, so there is no shared state sitting inside the resource objects themselves. That is actually quite nice because you don't have to worry about one request messing with another request's fields. The catch is that the actual data — rooms, sensors, readings — obviously needs to survive between requests, so I put all of that into a singleton `DataStore` class. The maps inside `DataStore` are `ConcurrentHashMap`s, which handle basic thread safety like puts and gets without me having to manually synchronize everything. One thing worth noting though is that `ConcurrentHashMap` only makes individual operations atomic. If I needed to do something like "read a value, check it, then write back", that whole sequence isn't automatically safe — I'd need extra synchronization for that kind of compound logic.

### Part 1.2
HATEOAS is basically the idea that an API should tell you what you can do next by including links in its responses. So when you hit the root endpoint of my API, the JSON that comes back has links pointing to `/api/v1/rooms` and `/api/v1/sensors`. The nice thing about this is that a client doesn't need to know all the URLs upfront or have them hardcoded. They can just follow the links. Compared to just reading a static API doc, this is way more flexible because if I change a URL on the server side, the links update automatically and the client doesn't break (as long as it follows links instead of building URLs itself).

### Part 2.1
I went with returning the full room objects (name, capacity, sensor IDs, etc.) rather than just IDs. The main reason is that if I only returned IDs, the client would have to make a separate GET request for each room just to see its name or how many sensors it has, and that adds up fast. With full objects the client gets everything in one shot. Sure, the payload is a bit bigger, but for a small dataset like this it really doesn't matter. If this was a production system with millions of rooms it might be worth thinking about pagination or partial responses, but for this coursework it makes way more sense to just send everything.

### Part 2.2
Yeah, DELETE is idempotent here. The first time you DELETE a room (assuming it has no sensors attached) it gets removed and you get back a 204. If you send the exact same DELETE again, the room is already gone so you get a 404 — but the important thing is the server state hasn't changed the second time around. That's what idempotent means, really. It's not about getting the same response code every time, it's about the effect on the server being the same no matter how many times you repeat the request. After the first call the room is gone, and every call after that just confirms it's still gone.

### Part 3.1
If someone sends a request with `Content-Type: text/plain` or `application/xml` to one of my endpoints that has `@Consumes(APPLICATION_JSON)`, the request never even reaches my code. Jersey intercepts it at the framework level and sends back a 415 Unsupported Media Type error. Basically the framework looks at the incoming content type, sees it doesn't match what the method says it accepts, and rejects it right there. My resource method doesn't run at all. It's not a business logic failure — it's the framework saying "I literally can't parse this for you."

### Part 3.2
I used `@QueryParam("type")` for filtering sensors rather than putting it in the path like `/sensors/type/CO2`. Query parameters make a lot more sense here because the filter is optional — if you don't include `?type=`, you just get all sensors back. With a path-based approach you'd need a completely separate route for the unfiltered case, which is messy. Also if I ever wanted to add more filters later (like by status or by room), query parameters chain together naturally (`?type=CO2&status=ACTIVE`) whereas path segments would get really ugly really fast (`/sensors/type/CO2/status/ACTIVE`). Query params are just the right tool for optional filters on a collection.

### Part 4.1
Instead of cramming all the sensor and reading logic into one massive class, I used a sub-resource locator. `SensorResource` handles sensor-level stuff (create, list, filter), and when someone hits `/{sensorId}/readings`, it delegates to a separate `SensorReadingResource` class. This keeps things clean — each class has one job. It's easier to read, easier to test, and if I need to change how readings work I only touch one file instead of scrolling through a 300-line mega-controller trying to find the right method.

### Part 5.2
When a client tries to create a sensor with a `roomId` that doesn't exist, I return 422 instead of 404. The reason is that 404 means the endpoint itself wasn't found, like the URL is wrong. But in this case the URL is perfectly fine — `POST /api/v1/sensors` exists. The problem is that the data inside the request body references a room that isn't there. The server can parse the JSON just fine, it just can't process the request because the referenced room is missing. That's exactly what 422 is for — "I understood what you sent me, but it doesn't make sense semantically."

### Part 5.1
If a client tries to delete a room that still has sensors assigned to it, the API returns 409 Conflict. That status fits because the request itself is valid, but it clashes with the current server state: the room cannot be removed until the linked sensors are dealt with first. A 409 tells the client that the resource exists and the method is allowed, but the operation cannot be completed because of a state conflict.

### Part 5.3
When a sensor is marked `MAINTENANCE`, it should not accept new readings. I mapped that to 403 Forbidden because the server understands the request, but the sensor's current state blocks the action. It is not a missing resource problem and it is not a parsing problem; it is an authorization-style refusal caused by business rules. The client would need to change the sensor status before the reading can be posted.

### Part 5.4
Sending back stack traces in error responses is a really bad idea from a security standpoint. If an attacker triggers an error and gets a full stack trace, they can see things like the internal package structure (e.g. `com.smartcampus.store.DataStore`), which libraries and versions you're using (Jersey 2.39.1, Jackson, etc.), file paths on the server, method names, and sometimes even hints about your data layer. All of that makes it way easier for someone to craft targeted attacks because they basically get a map of your application internals for free. That's why in my project the `GlobalExceptionMapper` only returns a generic "something went wrong" message and the actual exception details stay in the server logs.

### Part 5.5
I implemented logging as a `ContainerRequestFilter` and `ContainerResponseFilter` rather than sprinkling `LOGGER.info()` calls inside every single resource method. The filter approach means every request and response gets logged automatically, in the same format, without me having to remember to add logging to each new endpoint. It's the DRY principle in action — if I want to change the log format or add more info to the logs, I do it in one place. With inline logging I'd have to update every method individually, and chances are I'd forget one and end up with inconsistent logs.

## Final Checklist
- [ ] `mvn clean package` succeeds
- [ ] Server starts on `localhost:8080`
- [ ] All 5+ curl commands work as expected
- [ ] `Location` header present on `POST /api/v1/rooms` response
- [ ] `?type=` filtering is case-insensitive (test with mixed case)
- [ ] GitHub repo is public
- [ ] `README.md` is in the repo root and contains all 12 report answers
- [ ] Video demo recorded (max 10 min, face + voice, Postman tests shown)
- [ ] PDF report submitted to Blackboard
