public class Command {
    String key;
    int value;

    Command(String key, int value) {
        this.key = key.substring(1);                                // use right single letter as key
        this.value = value;
    }

    public String toString() {
        return String.format("Key: %s%nValue: %d", key, value);     // used for debugging in terminal
    }
}