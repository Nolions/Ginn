package tw.nolions.coffeebeanslife.model;

public class Temperature {
    private Long seconds;
    private float temp;

    public Temperature(float temp, Long seconds) {
        setTemp(temp);
        setSeconds(seconds);
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getTemp() {
        return this.temp;
    }

    public void setSeconds(Long seconds) {
        this.seconds = seconds;
    }

    public Long getSeconds() {
        return this.seconds;
    }
}
