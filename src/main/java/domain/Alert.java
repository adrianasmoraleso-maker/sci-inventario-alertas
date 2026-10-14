package domain;

public class Alert {
    private String type;
    private String productCode;
    private String lotCode;
    private String message;

    public Alert(String type, String productCode, String lotCode, String message) {
        this.type = type;
        this.productCode = productCode;
        this.lotCode = lotCode;
        this.message = message;
    }

    public String getType() { return type; }
    public String getProductCode() { return productCode; }
    public String getLotCode() { return lotCode; }
    public String getMessage() { return message; }
}
