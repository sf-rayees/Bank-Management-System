import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

class App {
    private static final String url = "jdbc:mysql://localhost:3306/bank";
    private static final String username = "root";
    private static final String password = "YOUR_PASSWORD";
    private Connection connection;
    private Scanner scanner;

    public App(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;

    }

    public void register() {
        scanner.nextLine();
        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (user_exist(email)) {
            System.out.println("User Already Exists for this Email Address!!");
            return;
        }
        String register_query = "INSERT INTO User(user, email, password) VALUES(?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(register_query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Registration Successfull!");
            } else {
                System.out.println("Registration Failed!");
            }
        } catch (SQLException e) {
            System.out.println(e);
            ;
        }
    }

    public String login() {
        scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        String login_query = "SELECT * FROM User WHERE email = ? AND password = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(login_query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return email;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean user_exist(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long open_account(String email) {
        if (!account_exist(email)) {
            String open_account_query = "INSERT INTO Accounts(acc_num, name, email, balance, pin) VALUES(?, ?, ?, ?, ?)";
            scanner.nextLine();
            System.out.print("Enter Full Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Initial Amount: ");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            System.out.print("Enter Security Pin: ");
            String pin = scanner.nextLine();
            try {
                long acc_num = generateAccountNumber();
                PreparedStatement preparedStatement = connection.prepareStatement(open_account_query);
                preparedStatement.setLong(1, acc_num);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, email);
                preparedStatement.setDouble(4, balance);
                preparedStatement.setString(5, pin);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    return acc_num;
                } else {
                    throw new RuntimeException("Account Creation failed!!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Account Already Exist");

    }

    public long getAccount_number(String email) {
        String query = "SELECT acc_num from Accounts WHERE email = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("acc_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Account Number Doesn't Exist!");
    }

    private long generateAccountNumber() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement
                    .executeQuery("SELECT acc_num from accounts ORDER BY acc_num DESC LIMIT 1");
            if (resultSet.next()) {
                long last_account_number = resultSet.getLong("acc_num");
                return last_account_number + 1;
            } else {
                return 10000100;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 10000100;
    }

    public boolean account_exist(String email) {
        String query = "SELECT acc_num from Accounts WHERE email = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    public void credit_money(long acc_num) throws SQLException {
        scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (acc_num != 0) {
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM Accounts WHERE acc_num = ? and pin = ? ");
                preparedStatement.setLong(1, acc_num);
                preparedStatement.setString(2, pin);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String credit_query = "UPDATE Accounts SET balance = balance + ? WHERE acc_num = ?";
                    PreparedStatement preparedStatement1 = connection.prepareStatement(credit_query);
                    preparedStatement1.setDouble(1, amount);
                    preparedStatement1.setLong(2, acc_num);
                    int rowsAffected = preparedStatement1.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Rs." + amount + " credited Successfully");
                        connection.commit();
                        connection.setAutoCommit(true);
                        return;
                    } else {
                        System.out.println("Transaction Failed!");
                        connection.rollback();
                        connection.setAutoCommit(true);
                    }
                } else {
                    System.out.println("Invalid Security Pin!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(true);
    }

    public void debit_money(long acc_num) throws SQLException {
        scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();
        try {
            connection.setAutoCommit(false);
            if (acc_num != 0) {
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM Accounts WHERE acc_num = ? and pin = ? ");
                preparedStatement.setLong(1, acc_num);
                preparedStatement.setString(2, pin);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    double current_balance = resultSet.getDouble("balance");
                    if (amount <= current_balance) {
                        String debit_query = "UPDATE Accounts SET balance = balance - ? WHERE acc_num = ?";
                        PreparedStatement preparedStatement1 = connection.prepareStatement(debit_query);
                        preparedStatement1.setDouble(1, amount);
                        preparedStatement1.setLong(2, acc_num);
                        int rowsAffected = preparedStatement1.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Rs." + amount + " debited Successfully");
                            connection.commit();
                            connection.setAutoCommit(true);
                            return;
                        } else {
                            System.out.println("Transaction Failed!");
                            connection.rollback();
                            connection.setAutoCommit(true);
                        }
                    } else {
                        System.out.println("Insufficient Balance!");
                    }
                } else {
                    System.out.println("Invalid Pin!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(true);
    }

    public void transfer_money(long sender_account_number) throws SQLException {
        scanner.nextLine();
        System.out.print("Enter Receiver Account Number: ");
        long receiver_account_number = scanner.nextLong();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();
        try {
            connection.setAutoCommit(false);
            if (sender_account_number != 0 && receiver_account_number != 0) {
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM Accounts WHERE acc_num = ? AND pin = ? ");
                preparedStatement.setLong(1, sender_account_number);
                preparedStatement.setString(2, pin);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    double current_balance = resultSet.getDouble("balance");
                    if (amount <= current_balance) {

                        // Write debit and credit queries
                        String debit_query = "UPDATE Accounts SET balance = balance - ? WHERE acc_num = ?";
                        String credit_query = "UPDATE Accounts SET balance = balance + ? WHERE acc_num = ?";

                        // Debit and Credit prepared Statements
                        PreparedStatement creditPreparedStatement = connection.prepareStatement(credit_query);
                        PreparedStatement debitPreparedStatement = connection.prepareStatement(debit_query);

                        // Set Values for debit and credit prepared statements
                        creditPreparedStatement.setDouble(1, amount);
                        creditPreparedStatement.setLong(2, receiver_account_number);
                        debitPreparedStatement.setDouble(1, amount);
                        debitPreparedStatement.setLong(2, sender_account_number);
                        int rowsAffected1 = debitPreparedStatement.executeUpdate();
                        int rowsAffected2 = creditPreparedStatement.executeUpdate();
                        if (rowsAffected1 > 0 && rowsAffected2 > 0) {
                            System.out.println("Transaction Successful!");
                            System.out.println("Rs." + amount + " Transferred Successfully");
                            connection.commit();
                            connection.setAutoCommit(true);
                            return;
                        } else {
                            System.out.println("Transaction Failed");
                            connection.rollback();
                            connection.setAutoCommit(true);
                        }
                    } else {
                        System.out.println("Insufficient Balance!");
                    }
                } else {
                    System.out.println("Invalid Security Pin!");
                }
            } else {
                System.out.println("Invalid account number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(true);
    }

    public void getBalance(long acc_num) {
        scanner.nextLine();
        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();
        try {
            PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT balance FROM Accounts WHERE acc_num = ? AND pin = ?");
            preparedStatement.setLong(1, acc_num);
            preparedStatement.setString(2, pin);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                System.out.println("Balance: " + balance);
            } else {
                System.out.println("Invalid Pin!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String email;
        long acc_num;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner scanner = new Scanner(System.in);
            App obj = new App(connection, scanner);
            while (true) {
                System.out.println("*** WELCOME TO BANKING SYSTEM ***");
                System.out.println();
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.println("Enter your choice: ");
                int choice1 = scanner.nextInt();
                switch (choice1) {
                    case 1:
                        obj.register();
                        break;
                    case 2:
                        email = obj.login();
                        if (email != null) {
                            System.out.println();
                            System.out.println("User Logged In!");
                            if (!obj.account_exist(email)) {
                                System.out.println();
                                System.out.println("1. Open a new Bank Account");
                                System.out.println("2. Exit");
                                if (scanner.nextInt() == 1) {
                                    acc_num = obj.open_account(email);
                                    System.out.println("Account Created Successfully");
                                    System.out.println("Your Account Number is: " + acc_num);
                                } else {
                                    break;
                                }

                            }
                            acc_num = obj.getAccount_number(email);
                            int choice2 = 0;
                            while (choice2 != 5) {
                                System.out.println();
                                System.out.println("1. Debit Money");
                                System.out.println("2. Credit Money");
                                System.out.println("3. Transfer Money");
                                System.out.println("4. Check Balance");
                                System.out.println("5. Log Out");
                                System.out.println("Enter your choice: ");
                                choice2 = scanner.nextInt();
                                switch (choice2) {
                                    case 1:
                                        obj.debit_money(acc_num);
                                        break;
                                    case 2:
                                        obj.credit_money(acc_num);
                                        break;
                                    case 3:
                                        obj.transfer_money(acc_num);
                                        break;
                                    case 4:
                                        obj.getBalance(acc_num);
                                        break;
                                    case 5:
                                        break;
                                    default:
                                        System.out.println("Enter Valid Choice!");
                                        break;
                                }
                            }

                        } else {
                            System.out.println("Incorrect Email or Password!");
                        }
                    case 3:
                        System.out.println("THANK YOU FOR USING BANKING SYSTEM!!!");
                        System.out.println("Exiting System!");
                        return;
                    default:
                        System.out.println("Enter Valid Choice");
                        break;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}