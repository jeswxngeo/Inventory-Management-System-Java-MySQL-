# Sales & Inventory Tracker

A simple Java-based console application for small businesses to manage product inventory and sales efficiently using MySQL.

## 📌 Features

- Add and view products
- Record sales and automatically update inventory
- Generate basic daily/weekly sales reports using SQL `JOIN` and `GROUP BY`
- Store all records in a MySQL database using JDBC


## 🛠 Tech Stack

- Java (Console-based)
- MySQL (Database)
- JDBC (Java Database Connectivity)


## 🗂 Project Structure

Sales_Inventory_Tracker/
├── dao/ # Database access logic (ProductDAO, SaleDAO)
├── model/ # Data models (Product, Sale)
├── utils/ # DBConnection utility
├── Main.java # Entry point for running the application
└── schema.sql # SQL file to create database tables



## 🧪 Sample SQL Table Setup

```sql
CREATE DATABASE IF NOT EXISTS sales_inventory;

USE sales_inventory;

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    quantity INT,
    price DOUBLE
);

CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    quantity_sold INT,
    sale_date DATE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
