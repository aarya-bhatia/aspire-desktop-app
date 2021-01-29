package app;

public class Axis implements Comparable<Axis> {
    private String name;
    private int score;

    public Axis(String name) {
        this.setName(name);
        this.setScore(0);
    }

    public void update(int change) {
        score += change;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("[Axis: %s = %d]", name, score);
    }

    @Override
    public int compareTo(Axis o) {
        return Integer.compare(this.getScore(), o.getScore());
    }
}
