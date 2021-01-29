package app;

public class CPT {

    public static final String[] names = {"Verbal", "Logical", "Visual", "Musical", "Bodily", "Interpersonal", "Intrapersonal", "Naturalist"};

    public static final int n = names.length;

    public static Axis[] createAxis() {
        Axis[] arr = new Axis[names.length];
        int index = 0;
        for (String name : names) {
            arr[index++] = new Axis(name);
        }
        return arr;
    }

}
