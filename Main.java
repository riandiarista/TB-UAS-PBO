import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

// Interface untuk mendefinisikan kontrak dasar manajemen inventory
interface InventoryManagement {
    void addSupply(String type, int quantity, double price, String idSupplier);  // Create
    void displayInventory();  // Read
    double calculateTotalValue();  // Read/Perhitungan
}

// Superclass untuk mendefinisikan atribut dasar dari sebuah supply
class Supply {
    protected static int count = 0;  // Variabel statis untuk menghitung supply
    protected int supplyNumber;
    protected String type;
    protected int quantity;
    protected double price;
    protected String addedDate;
    protected String idSupplier;

    // Konstruktor untuk inisialisasi data supply
    public Supply(String type, int quantity, double price, String idSupplier) {
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.idSupplier = idSupplier;
        this.addedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()); // Manipulasi Date & String
        this.supplyNumber = ++count;
    }

    // Getter untuk supply number
    public int getSupplyNumber() {
        return supplyNumber;
    }

    // Getter dan Setter untuk atribut lainnya
    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public String getIdSupplier() {
        return idSupplier;
    }

    // Menghitung nilai total supply (Quantity * Price)
    public double getTotalValue() {
        return quantity * price;
    }
}

// Subclass yang meng-extend kelas Supply
class BBMSupply extends Supply {
    public BBMSupply(String type, int quantity, double price, String idSupplier) {
        super(type, quantity, price, idSupplier);  // Memanggil konstruktor superclass
    }

    // Metode untuk mengubah tipe supply menjadi huruf kapital
    public String capitalizeType() {
        return type.toUpperCase();  // Manipulasi String
    }

    // Override metode toString untuk tampilan lebih spesifik
    @Override
    public String toString() {
        return "Type: " + capitalizeType() + ", Quantity: " + quantity + ", Price: " + price + ", Supplier ID: " + idSupplier + ", Added Date: " + addedDate;
    }
}

// Kelas untuk mengatur koneksi database menggunakan JDBC
class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/BBM";  // URL database
    private static final String USER = "postgres";  // Username database
    private static final String PASSWORD = "riandi123";  // Password database

    // Mendapatkan koneksi ke database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);  // JDBC koneksi
    }
}

// Implementasi dari interface InventoryManagement
class BBMInventoryDatabase implements InventoryManagement {
    private ArrayList<Supply> inventory;  // Collection Framework (ArrayList)

    // Konstruktor untuk inisialisasi inventory
    public BBMInventoryDatabase() {
        inventory = new ArrayList<>();  // Inisialisasi ArrayList untuk menyimpan supply
    }

    // Menambahkan supply ke inventory dan database (Create)
    @Override
    public void addSupply(String type, int quantity, double price, String idSupplier) {
        BBMSupply newSupply = new BBMSupply(type, quantity, price, idSupplier);  // Membuat objek BBMSupply baru
        inventory.add(newSupply);  // Menambahkan supply ke inventory

        // SQL query untuk insert data ke database
        String sql = "INSERT INTO supply (type, quantity, price, id_supplier) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, price);
            stmt.setString(4, idSupplier);
            stmt.executeUpdate();  // Eksekusi query
            System.out.println("Supply added to database.");
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Menampilkan inventory dari database (Read)
    @Override
    public void displayInventory() {
        String sql = "SELECT * FROM supply";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {  // Query untuk mengambil data dari database

            System.out.println("+--------------+----------+----------+----------+----------------------+-------------------+");
            System.out.println("| Supply No.   | Type     | Quantity | Price    | Added Date           | Supplier ID       |");
            System.out.println("+--------------+----------+----------+----------+----------------------+-------------------+");
            while (rs.next()) {
                System.out.printf("| %-12d | %-8s | %-8d | %-8.2f | %-20s | %-17s |\n",
                        rs.getInt("supply_number"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getTimestamp("added_date"),
                        rs.getString("id_supplier"));
            }
            System.out.println("+--------------+----------+----------+----------+----------------------+-------------------+");
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Menghitung total nilai inventory (Perhitungan matematika)
    @Override
    public double calculateTotalValue() {
        double total = 0;
        String sql = "SELECT quantity, price FROM supply";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {  // Query untuk menghitung total value

            while (rs.next()) {
                total += rs.getInt("quantity") * rs.getDouble("price");
            }
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }

        return total;  // Mengembalikan total nilai
    }

    // Mengupdate supply di database (Update)
    public void updateSupply(int supplyNumber, String idSupplier, int newQuantity, double newPrice) {
        String sql = "UPDATE supply SET quantity = ?, price = ? WHERE supply_number = ? AND id_supplier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setDouble(2, newPrice);
            stmt.setInt(3, supplyNumber);
            stmt.setString(4, idSupplier);
            int rowsAffected = stmt.executeUpdate();  // Eksekusi query untuk update

            if (rowsAffected > 0) {
                System.out.println("Supply updated in database.");
            } else {
                System.out.println("No supply found to update.");
            }
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Menghapus supply dari database (Delete)
    public void deleteSupply(int supplyNumber, String idSupplier) {
        String sql = "DELETE FROM supply WHERE supply_number = ? AND id_supplier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplyNumber);
            stmt.setString(2, idSupplier);
            int rowsAffected = stmt.executeUpdate();  // Eksekusi query untuk delete

            if (rowsAffected > 0) {
                System.out.println("Supply deleted from database.");
                resetSupplyNumber();  // Reset supply number setelah penghapusan
            } else {
                System.out.println("No supply found to delete.");
            }
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Mereset nomor supply setelah penghapusan
    private void resetSupplyNumber() {
        String sql = "SELECT MAX(supply_number) FROM supply";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {  // Query untuk mencari supply number terakhir

            if (rs.next()) {
                int maxSupplyNumber = rs.getInt(1);
                Supply.count = maxSupplyNumber;  // Update count ke nomor supply terakhir
            }
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Mereset semua data dalam tabel supply (Reset)
    public void resetAll() {
        String sql = "DELETE FROM supply";  // Menghapus semua data supply dari tabel
        String resetCountSql = "ALTER SEQUENCE supply_supply_number_seq RESTART WITH 1";  // Mereset urutan nomor supply

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);  // Menghapus semua data
            stmt.executeUpdate(resetCountSql);  // Mereset urutan nomor supply
            System.out.println("All supply data has been reset.");
        } catch (SQLException e) {  // Exception handling untuk error database
            System.out.println("Database error: " + e.getMessage());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BBMInventoryDatabase inventory = new BBMInventoryDatabase();
        boolean running = true;

        // Program utama untuk menerima input dan menjalankan fungsi
        while (running) {
            try {
                System.out.println("\n--- BBM Supply Management ---");
                System.out.println("1. Add Supply");
                System.out.println("2. Display Inventory");
                System.out.println("3. Update Supply");
                System.out.println("4. Delete Supply");
                System.out.println("5. Calculate Total Value");
                System.out.println("6. Reset All Data");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter type of supply: ");
                        String type = scanner.nextLine();
                        System.out.print("Enter quantity (liter): ");
                        int quantity = scanner.nextInt();
                        System.out.print("Enter price per liter: ");
                        double price = scanner.nextDouble();
                        System.out.print("Enter supplier ID: ");
                        String idSupplier = scanner.next();
                        inventory.addSupply(type, quantity, price, idSupplier);  // Menambahkan supply
                        break;
                    case 2:
                        inventory.displayInventory();  // Menampilkan inventory
                        break;
                    case 3:
                        System.out.print("Enter supply number to update: ");
                        int updateSupplyNumber = scanner.nextInt();
                        System.out.print("Enter supplier ID: ");
                        String updateIdSupplier = scanner.next();
                        System.out.print("Enter new quantity: ");
                        int newQuantity = scanner.nextInt();
                        System.out.print("Enter new price per unit: ");
                        double newPrice = scanner.nextDouble();
                        inventory.updateSupply(updateSupplyNumber, updateIdSupplier, newQuantity, newPrice);  // Mengupdate supply
                        break;
                    case 4:
                        System.out.print("Enter supply number to delete: ");
                        int supplyNumber = scanner.nextInt();
                        System.out.print("Enter supplier ID: ");
                        String deleteIdSupplier = scanner.next();
                        inventory.deleteSupply(supplyNumber, deleteIdSupplier);  // Menghapus supply
                        break;
                    case 5:
                        double totalValue = inventory.calculateTotalValue();  // Menghitung total nilai inventory
                        System.out.println("Total Inventory Value: " + totalValue);
                        break;
                    case 6:
                        inventory.resetAll();  // Reset semua data
                        break;
                    case 7:
                        running = false;
                        System.out.println("Thank you for using the application!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            } catch (Exception e) {  // Exception handling untuk input yang salah
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        }

        scanner.close();
    }
}
