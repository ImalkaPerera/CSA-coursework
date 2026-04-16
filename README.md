# Smart Campus API

## Overview
Smart Campus API is a JAX-RS REST service for managing rooms, sensors, and sensor readings in a university campus environment.

## How to Build and Run
### Build
```bash
mvn clean package
```

### Run
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
| GET | /api/v1/sensors | List all sensors |
| POST | /api/v1/sensors | Create a sensor |
| GET | /api/v1/sensors/{sensorId} | Get a sensor by ID |

## Sample curl Commands
### Discover API
```bash
curl -X GET http://localhost:8080/api/v1
```

### Create a room
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Library Quiet Study","capacity":50}'
```

### List rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### Create a sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type":"CO2","status":"ACTIVE","currentValue":400.5,"roomId":"<room-id>"}'
```

### List sensors
```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

## Report Answers
### Part 1.1
TODO

### Part 1.2
TODO

### Part 2.1
TODO

### Part 2.2
TODO

### Part 3.1
TODO

### Part 3.2
TODO

### Part 4.1
TODO

### Part 5.2
TODO

### Part 5.4
TODO

### Part 5.5
TODO
