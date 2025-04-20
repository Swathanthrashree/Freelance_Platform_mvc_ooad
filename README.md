# Freelance Platform

A JavaFX application for connecting freelancers with clients.

## Prerequisites

- Java 11 or higher
- Maven
- MySQL

## Setup

1. Install MySQL if you haven't already
2. Create the database and tables:

```sql
CREATE DATABASE freelance_platform;
USE freelance_platform;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    user_type ENUM('FREELANCER', 'CLIENT') NOT NULL
);

CREATE TABLE projects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    budget DECIMAL(10, 2) NOT NULL,
    deadline DATE NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL,
    client_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES users(id)
);
```

3. Update the database configuration in `src/main/resources/database.properties` if needed

## Building and Running

1. Build the project:
```
mvn clean package
```

2. Run the application:
```
mvn javafx:run
```

## Features

- User registration and login
- Client dashboard
- Freelancer dashboard
- Project creation and management
- Project bidding
- User profiles

## Project Structure

- `model/` - Data models
- `dao/` - Data Access Objects for database operations
- `controller/` - Business logic
- `view/` - JavaFX UI components
- `util/` - Utility classes 