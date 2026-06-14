package model;

public class Product {
    private int id;
    private String productCode;
    private String name;
    private String category;
    private int stock;
    private String unit;
    private double purchasePrice;
    private double price;
    private int minimumStock;
    private String description;
    private boolean isActive;

    public Product() {}

    public Product(int id, String productCode, String name, String category, int stock, String unit, double purchasePrice, double price, int minimumStock, String description, boolean isActive) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.unit = unit;
        this.purchasePrice = purchasePrice;
        this.price = price;
        this.minimumStock = minimumStock;
        this.description = description;
        this.isActive = isActive;
    }

    public Product(int id, String productCode, String name, String category, int stock, String unit, double purchasePrice, double price, int minimumStock, String description) {
        this(id, productCode, name, category, stock, unit, purchasePrice, price, minimumStock, description, true);
    }

    public Product(String productCode, String name, String category, int stock, String unit, double purchasePrice, double price, int minimumStock, String description) {
        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.unit = unit;
        this.purchasePrice = purchasePrice;
        this.price = price;
        this.minimumStock = minimumStock;
        this.description = description;
    }

    public Product(int id, String productCode, String name, double price, int stock) {
        this(id, productCode, name, "Lainnya", stock, "Pcs", 0.0, price, 0, "");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name + " [" + productCode + "] - Rp" + String.format("%,.0f", price);
    }
}

