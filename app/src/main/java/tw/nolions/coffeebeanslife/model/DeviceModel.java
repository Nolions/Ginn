package tw.nolions.coffeebeanslife.model;

public class DeviceModel {
    private String name, address;

    public DeviceModel(String name, String address) {
        this.setName(name);
        this.setAddress(address);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }
}
