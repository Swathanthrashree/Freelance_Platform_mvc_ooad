CREATE DATABASE IF NOT EXISTS freelance_platform;
USE freelance_platform;

-- Drop existing tables if they exist to avoid conflicts
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS freelancer_skills;
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Freelancer', 'Client') NOT NULL,
    rating DECIMAL(3,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create projects table
CREATE TABLE projects (
    P_ID INT PRIMARY KEY AUTO_INCREMENT,
    Client_ID INT NOT NULL,
    Title VARCHAR(100) NOT NULL,
    Description TEXT NOT NULL,
    Budget DECIMAL(10,2) NOT NULL,
    Deadline DATE NOT NULL,
    P_Status INT DEFAULT 0,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Client_ID) REFERENCES users(id)
);

-- Create bids table
CREATE TABLE bids (
    Bid_ID INT PRIMARY KEY AUTO_INCREMENT,
    P_ID INT NOT NULL,
    Freelancer_ID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    Timeframe DATE NOT NULL,
    Bid_Status VARCHAR(20) DEFAULT 'Pending',
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (P_ID) REFERENCES projects(P_ID),
    FOREIGN KEY (Freelancer_ID) REFERENCES users(id)
);

-- Create freelancer_skills table
CREATE TABLE freelancer_skills (
    Freelancer_ID INT NOT NULL,
    Skill_Name VARCHAR(50) NOT NULL,
    Proficiency_Level VARCHAR(20) NOT NULL,
    PRIMARY KEY (Freelancer_ID, Skill_Name),
    FOREIGN KEY (Freelancer_ID) REFERENCES users(id)
);

-- Create invoices table
CREATE TABLE invoices (
    Invoice_ID INT PRIMARY KEY AUTO_INCREMENT,
    P_ID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    Status VARCHAR(20) DEFAULT 'Unpaid',
    Issue_Date DATE NOT NULL,
    FOREIGN KEY (P_ID) REFERENCES projects(P_ID)
); 