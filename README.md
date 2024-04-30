# Bank Management System

This is a simple bank management system developed in Java and database MySQL to manage the operations of a bank. The system provides functionalities such as creating and managing customer accounts, depositing and withdrawing funds, transferring money between accounts.

## Features

- **User Registeration**
- **User Login**
- **Account Setup**
- **Security PIN**
- **Debit Money**
- **Credit Money**
- **Transfer Money**
- **Check Balance**

## Technologies Used

- Java: The core logic of the Flight Booking System is implemented in Java programming language, providing robustness and platform independence.
- MySQL Database: The system utilizes a MySQL database to store flight information, user data, and booking details, ensuring data integrity and scalability.
- JDBC (Java Database Connectivity): JDBC is used to establish a connection between the Java application and the MySQL database, enabling seamless interaction with the database.

## Installation

- Install Visual Studio Code.
- Install Java Extension Pack in Visual Studio Code.
- Install MySQL.
- create a database named "bank".
- Create a table with name "user" and with the following details:
  <br>
   ```html
  CREATE TABLE `user` (
  `user` varchar(225) NOT NULL,
  `email` varchar(225) NOT NULL,
  `password` varchar(225) NOT NULL,
  PRIMARY KEY (`email`)
  )
- Create a table with name "accounts" and with the following details:
   <br>
    ```html
  CREATE TABLE `accounts` (
  `acc_num` bigint NOT NULL,
  `name` varchar(225) NOT NULL,
  `email` varchar(225) NOT NULL,
  `pin` char(4) NOT NULL,
  `balance` decimal(10,2) NOT NULL,
  PRIMARY KEY (`acc_num`),
  UNIQUE KEY `email_UNIQUE` (`email`)
  )
- Download Connector/J and set it up.
- Create a Java Project.
- Download and replace the project App.java present in file/src with the Repository App.java.
- Run the Java Application: You can run your Java application directly from Visual Studio Code. Open the Java file containing your main method, right-click, and select "Run Java".

  ## Developer: Sheikh Faisal Rayees
