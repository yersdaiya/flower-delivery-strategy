package flowerdelivery.pricing;

public class FakeFactorProvider implements ExternalFactorProvider {
    private double multiplier = 2.0;

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public double getSurgeMultiplier() {
        return multiplier;
    }
}