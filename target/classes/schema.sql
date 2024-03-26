CREATE TABLE USERS (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       firstName VARCHAR(50) NOT NULL,
                       lastName VARCHAR(50) NOT NULL,
                       birthDate DATE NOT NULL,
                       city VARCHAR(50) NOT NULL,
                       country VARCHAR(50) NOT NULL,
                       avatar VARCHAR(255) NOT NULL,
                       company VARCHAR(100) NOT NULL,
                       jobPosition VARCHAR(100) NOT NULL,
                       mobile VARCHAR(20) NOT NULL,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       role VARCHAR(10) CHECK (role IN ('ADMIN', 'USER'))
);
