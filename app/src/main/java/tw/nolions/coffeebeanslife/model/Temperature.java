package tw.nolions.coffeebeanslife.model;

public class Temperature {
    private int seconds;
    private float temp;

    public Temperature(float temp, int seconds) {
        setTemp(temp);
        setSeconds(seconds);
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getTemp() {
        return this.temp;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return this.seconds;
    }
}
