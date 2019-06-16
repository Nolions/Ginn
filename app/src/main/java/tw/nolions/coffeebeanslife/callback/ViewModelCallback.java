package tw.nolions.coffeebeanslife.callback;

public interface ViewModelCallback {
    void updateTargetTemp(String temp);
    void firstCrack();
    void secondCrack();
    void actionBean(boolean action);
}
