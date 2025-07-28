// src/AlgorithmConfig.java
package src;

/**
 * Algorithm configuration class implementing the common interface
 */
public class AlgorithmConfig implements AlgorithmConfiguration{
    private static final long serialVersionUID = 1L;
    private String name;
    private String algorithmType;
    private char tiebreaker;
    private int sightRadius;

    public AlgorithmConfig(String name, String algorithmType, char tiebreaker, int sightRadius) {
        this.name = name;
        this.algorithmType = algorithmType;
        this.tiebreaker = tiebreaker;
        this.sightRadius = sightRadius;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAlgorithmType() { return algorithmType; }
    public void setAlgorithmType(String algorithmType) { this.algorithmType = algorithmType; }
    public char getTiebreaker() { return tiebreaker; }
    public void setTiebreaker(char tiebreaker) { this.tiebreaker = tiebreaker; }
    public int getSightRadius() { return sightRadius; }
    public void setSightRadius(int sightRadius) { this.sightRadius = sightRadius; }

    @Override
    public String toString() {
        String algorithmAbbrev = switch (algorithmType) {
            case "Forward" -> "f";
            case "Backward" -> "b";
            case "Adaptive" -> "a";
            default -> "?";
        };
        return String.format("%s (a:%s, t:%c, r:%d)", name, algorithmAbbrev, tiebreaker, sightRadius);
    }
}
