// src/AlgorithmConfiguration.java
package src;

import java.io.Serializable;

/**
 * Common interface for algorithm configurations used across the application.
 * This allows different UI components to share configuration data.
 */
public interface AlgorithmConfiguration extends Serializable {
    String getName();
    void setName(String name);
    String getAlgorithmType();
    void setAlgorithmType(String algorithmType);
    char getTiebreaker();
    void setTiebreaker(char tiebreaker);
    int getSightRadius();
    void setSightRadius(int sightRadius);
}
