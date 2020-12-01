public class Command {
    String key;
    long value;

    Command(String key, long value) {
        this.key = key.substring(1);                                // use right single letter as key
        this.value = value;
    }

    public String toString() {
        return String.format("Key: %s%nValue: %d", key, value);     // used for debugging in terminal
    }
}