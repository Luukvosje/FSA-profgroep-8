# FSA-profgroep-8

## CarRentalProject

![Logo](Logo.png)

A comprehensive car rental API built with Kotlin and Ktor, featuring user management, car listings, and rental booking functionality.

## Table of Contents

- [Installation](#installation)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Usage](#usage)

## Installation

### Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java 11 or higher** - Required to run the Kotlin application
- **PostgreSQL** - Database server for data persistence
- **Git** - For cloning the repository

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd FSA-profgroep-8
```

### Step 2: Database Setup

1. **Install PostgreSQL** (if not already installed):
   - Windows: Download from [postgresql.org](https://www.postgresql.org/download/windows/)
   - macOS: `brew install postgresql`
   - Linux: `sudo apt-get install postgresql postgresql-contrib`

2. **Create Database**:
   ```sql
   CREATE DATABASE your_db;
   CREATE USER your_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE your_db TO your_user;
   ```

### Step 3: Configuration Setup

#### 3.1 Application Configuration

Edit `/src/main/resources/application.yaml` and update the JWT and RDW details:

```yaml
ktor:
  application:
    modules:
      - com.profgroep8.ApplicationKt.module
  deployment:
    port: 8080
  jwt:
    secret: "your_secret"
    issuer: "com.profgroep8.carrental"
    audience: "car_rental_users"
    realm: "CarRentalAPI"
  rdw:
    apiKey: "your_rdw_api_key"
```

#### 3.2 Database Configuration

Edit `/src/main/kotlin/repositories/DatabaseFactoryImpl.kt` and update the database connection details:

```kotlin
// Update these values to match your PostgreSQL setup
private val database = Database.connect(
    url = "jdbc:postgresql://localhost:5432/your_database_name",
    driver = "org.postgresql.Driver",
    user = "your_database_user",
    password = "your_database_password"
)
```

#### 3.3 External API Keys

- **RDW API Key**: Required for fetching car information by license plate
  - Get your API key from [RDW Open Data](https://opendata.rdw.nl/)
  - Update the `rdw.apiKey` value in `application.yaml`

- **JWT Secret**: Used for signing and verifying authentication tokens
  - Generate a secure random string for production
  - Update the `jwt.secret` value in `application.yaml`

### Step 4: Build and Run the Application

1. **Navigate to the API directory**:
   ```bash
   cd RMC-API
   ```

2. **Build the project**:
   ```bash
   gradlew build
   ```

3. **Run the application**:
   ```bash
   gradlew run
   ```

4. **Verify Installation**:
   - The API will be available at `http://localhost:8080`
   - Swagger UI at `http://localhost:8080/swagger`
   - OpenAPI spec at `http://localhost:8080/openapi/documentation.yaml`

### Step 5: Environment Configuration

The application uses the following configuration (found in `application.yaml`):

- **Port**: 8080
- **JWT Secret**: Configured for authentication
- **RDW API Key**: For vehicle registration data

## Getting Started

1. **Register a new user** using `POST /users/register`
2. **Login** using `POST /users/login` to get a JWT token
3. **Use the JWT token** in the Authorization header for protected endpoints
4. **Explore the API** using the Swagger UI at `http://localhost:8080/swagger`

## API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication
Most endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### User Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `POST` | `/users/register` | Register a new user account | None |
| `POST` | `/users/login` | Login and get JWT token | None |
| `GET` | `/users/me` | Get current user information | Required |
| `GET` | `/users/{id}/bonuspoints` | Get user bonus points | Required |
| `PUT` | `/users/{id}/bonuspoints` | Update user bonus points | Required |

#### User Registration
```http
POST /users/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+31612345678",
  "address": "Main Street 123",
  "zipcode": "1234AB",
  "city": "Amsterdam",
  "countryISO": "NL"
}
```

#### User Login
```http
POST /users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Car Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/cars` | Get all cars | Required |
| `POST` | `/cars` | Create a new car | Required |
| `GET` | `/cars/{carID}` | Get car by ID | Required |
| `PUT` | `/cars/{carID}` | Update car | Required |
| `DELETE` | `/cars/{carID}` | Delete car | Required |
| `GET` | `/cars/license/{plate}` | Get car by license plate from RDW | Required |
| `POST` | `/cars/license/{plate}` | Create car from RDW data | Required |
| `POST` | `/cars/{carID}/calculate` | Calculate car costs | Required |
| `POST` | `/cars/{carID}/image` | Upload car image | Required |
| `GET` | `/cars/search` | Search cars by keyword | Required |
| `POST` | `/cars/filter` | Filter cars by criteria | Required |
| `GET` | `/cars/user/{userID}` | Get cars by user ID | Required |

#### Create Car
```http
POST /cars
Authorization: Bearer <token>
Content-Type: application/json

{
  "licensePlate": "12-ABC-3",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2020,
  "fuelType": 1,
  "price": 2500
}
```

#### Search Cars
```http
GET /cars/search?keyword=toyota
Authorization: Bearer <token>
```

#### Filter Cars
```http
POST /cars/filter
Authorization: Bearer <token>
Content-Type: application/json

{
  "brand": "Toyota",
  "minPrice": 1000,
  "maxPrice": 5000,
  "fuelType": 1
}
```

### Rental Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| `GET` | `/rentals` | Get all rentals (Only rentals where user is involved) | Required |
| `POST` | `/rentals` | Create a new rental | Required |
| `GET` | `/rentals/{id}` | Get rental by ID | Required (Renter or Car Owner) |
| `PUT` | `/rentals/{id}` | Update rental | Required (Renter or Car Owner) |
| `DELETE` | `/rentals/{id}` | Delete rental | Required (Renter or Car Owner) |
| `PUT` | `/rentals/{id}/end` | End rental | Required (Renter or Car Owner) |
| `GET` | `/rentals/{id}/locations` | Get rental locations | Required (Renter or Car Owner) |
| `PUT` | `/rentals/{rentalID}/locations/{locationID}` | Update rental location | Required (Renter or Car Owner) |

#### Get Single Rental
```http
GET /rentals/{id}
Authorization: Bearer <token>
```

#### Create Rental
```http
POST /rentals
Authorization: Bearer <token>
Content-Type: application/json

{
  "carID": 1,
  "startLocation": {
    "date": "2024-01-15T10:00:00Z",
    "longitude": 4.9041,
    "latitude": 52.3676
  },
  "endLocation": {
    "date": "2024-01-20T18:00:00Z",
    "longitude": 4.9041,
    "latitude": 52.3676
  }
}
```

#### Get Rental Locations
```http
GET /rentals/{id}/locations
Authorization: Bearer <token>
```

### Data Models

#### User
- `userID`: Integer (unique identifier)
- `fullName`: String
- `email`: String (email format)
- `phone`: String
- `address`: String
- `zipcode`: String
- `city`: String
- `countryISO`: String
- `points`: Integer (bonus points)

#### Car
- `carID`: Integer (unique identifier)
- `licensePlate`: String
- `brand`: String
- `model`: String
- `year`: Integer
- `fuelType`: Integer
- `price`: Integer (in cents)
- `userID`: Integer (owner)

#### Rental
- `rentalID`: Integer (unique identifier)
- `userID`: Integer
- `carID`: Integer
- `startRentalLocation`: RentalLocation
- `endRentalLocation`: RentalLocation
- `state`: Integer

#### RentalLocation
- `rentalLocationID`: Integer (unique identifier)
- `date`: DateTime
- `longitude`: Float
- `latitude`: Float

### Error Responses

All endpoints may return error responses in the following format:

```json
{
  "error": "Error Type",
  "details": "Detailed error message"
}
```

Common HTTP status codes:
- `200`: Success
- `201`: Created
- `400`: Bad Request
- `401`: Unauthorized
- `404`: Not Found
- `409`: Conflict

## Usage

### Development

1. **Run in development mode**:
   ```bash
   gradlew run
   ```

2. **Run tests**:
   ```bash
   gradlew test
   ```

3. **Build for production**:
   ```bash
   gradlew build
   ```

### Production Deployment

1. **Build the JAR file**:
   ```bash
   gradlew shadowJar
   ```

2. **Run the application**:
   ```bash
   java -jar build/libs/RMC-API-0.0.1-all.jar
   ```

### API Testing

Use the provided HTTP request files in `src/test/http-requests/` or access the Swagger UI at `http://localhost:8080/swagger` for interactive API testing.

### Database Management

The application uses Exposed ORM with PostgreSQL. Database tables are created automatically on startup using `SchemaUtils.create()`

### External Services

- **RDW API**: Used for fetching vehicle registration data
- **JWT Authentication**: Secure token-based authentication
- **File Upload**: Car image uploads stored in `uploads/cars/` directory
