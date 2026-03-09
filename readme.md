# Procedure

1. create a database named `billing` in your mysql.
2. After that, migrate billing.sql in the `billing` database.
3. Then run the following command on the terminal to start the java project.

```
javac -cp ".;lib\mysql-connector-j-9.6.0.jar" Main.java DatabaseHelper.java BillingDashboard.java && java -cp ".;lib\mysql-connector-j-9.6.0.jar" Main
```

## Requirements

- Java (jdk 8 or higher)
- mySql server

### Note: mySql server should be up and running and should be on port 3306
