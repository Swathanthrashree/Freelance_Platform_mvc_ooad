-- Drop existing tables if they exist
DROP TABLE IF EXISTS proposals;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

-- Recreate users table with correct structure
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Freelancer', 'Client') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recreate projects table
CREATE TABLE projects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    budget DECIMAL(10,2),
    client_id INT,
    status ENUM('Open', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Open',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES users(id)
);

-- Recreate proposals table
CREATE TABLE proposals (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT,
    freelancer_id INT,
    amount DECIMAL(10,2),
    description TEXT,
    status ENUM('Pending', 'Accepted', 'Rejected') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (freelancer_id) REFERENCES users(id)
); 