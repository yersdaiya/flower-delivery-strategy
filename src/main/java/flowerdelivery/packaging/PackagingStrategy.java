package flowerdelivery.packaging;

public interface PackagingStrategy {
    String getPackagingDescription();
    double calculatePackagingCost();
}