package tw.nolions.coffeebeanslife.model;

public class temperature {
    private float beam, stove, environment;

    public temperature() {
        setBeam(0);
        setStove(0);
        setEnvironment(0);
    }

    public void setting(float beam, float stove, float environment) {
        setBeam(beam);
        setStove(stove);
        setEnvironment(environment);
    }

    public void setBeam(float temp) {
        this.beam = temp;
    }

    public float getBeam() {
        return beam;
    }

    public void setStove(float temp) {
        this.stove = temp;
    }

    public float getStove() {
        return stove;
    }

    public void setEnvironment(float temp) {
        this.environment = temp;
    }

    public float getEnvironment() {
        return this.environment;
    }
}
