package flowerdelivery.payment;

public class PaymentRules {
    public static final int MAX_AMOUNT = 500_000;

    public void validate(String cardNumber, int amount) {
        if (amount <= 0 || amount > MAX_AMOUNT)
            throw new PaymentException("Amount must be between 1 and " + MAX_AMOUNT);
        if (cardNumber == null || !cardNumber.matches("\\d{16}"))
            throw new PaymentException("Card number must contain 16 digits");
        if (cardNumber.endsWith("0000"))
            throw new PaymentException("Card declined by the bank");
    }

    public String lastFour(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }
}