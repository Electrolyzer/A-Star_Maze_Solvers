# A* Maze Solver

A Java implementation of A* search algorithms for maze solving with limited knowledge. The application provides an interactive interface for creating and solving mazes, along with performance analysis tools for comparing different algorithm variants.

## Overview

This project implements three variants of the A* search algorithm for environments with partial observability:

- **Repeated Forward A*** - Plans from current position to goal
- **Repeated Backward A*** - Plans from goal to current position  
- **Adaptive A*** - Learns better heuristics from previous searches

Each algorithm can be configured with different parameters:
- **Tiebreaker strategy** (g-value or h-value preference)
- **Sight radius** (1-20 cells, simulating limited knowledge)
- **Algorithm type** (Forward, Backward, or Adaptive)

The application features three main modes:
1. **Interactive Solver** - Create, edit, and solve mazes with step-by-step visualization
2. **Saved Results** - Load and analyze saved maze solutions side by side
3. **Performance Analysis** - Run batch tests across multiple mazes and compare algorithm performance

## Installation

### Prerequisites
- Java 17 or higher

### Running the Application

**Windows:**
```bash
.\launcher.bat          # Standard launcher
.\dev-launcher.bat      # Development launcher (always rebuilds)
```

**Unix/Linux/macOS:**
```bash
./launcher.sh           # Standard launcher  
./dev-launcher.sh       # Development launcher (always rebuilds)
```

The launcher will automatically build the project if needed and start the application.

## Usage

### Interactive Mode
- Use the maze editor to create custom mazes or load existing ones
- Configure algorithm parameters in the left panel
- Click "Solve Maze" to watch the algorithm find a path
- Save results for later analysis

### Result Viewer
- View saved results from Interactive Mode side-by-side
- Step results in time with each other for comparison

### Performance Analysis
- Select multiple algorithm configurations to compare
- Choose between predetermined mazes (50 included) or generate random ones
- Run batch analysis to see performance statistics and heat maps
- Export results for external analysis

### Configuration Management
- Create and save custom algorithm configurations
- Configurations persist between sessions
- Use configurations in all other modes

## Project Structure

```
A-Star/
├── launcher.bat/.sh          # Application launchers
├── dev-launcher.bat/.sh      # Development launchers
├── src/                      # Source code
├── scripts/                  # Build scripts
├── lib/                      # Dependencies (FlatLaf, JUnit)
├── mazes/                    # Sample maze files
├── config/                   # User configurations (auto-generated)
└── results/                  # Saved results (auto-generated)
```

## Algorithm Details

### Implementation Features
- Custom priority queue optimized for A* search
- Configurable heuristic functions (Manhattan distance)
- Support for partial observability through sight radius
- Real-time visualization of search progress
- Performance metrics tracking (cells expanded, execution time)

### Performance Characteristics
Testing across 50 predetermined mazes shows significant performance differences:

| Algorithm | Avg Expanded Cells | Avg Time (ms) |
|-----------|-------------------|---------------|
| Forward A* (g-tie) | 9,847 | 12.3 |
| Forward A* (h-tie) | 254,891 | 45.7 |
| Backward A* (g-tie) | 125,432 | 28.9 |
| Adaptive A* (g-tie) | 9,623 | 13.1 |

## Building from Source

**Manual build:**
```bash
# Windows
.\scripts\build.bat

# Unix/Linux/macOS
./scripts/build.sh
```

**Clean build:**
```bash
# Windows  
.\scripts\clean.bat
.\scripts\build.bat

# Unix/Linux/macOS
./scripts/clean.sh
./scripts/build.sh
```

## Testing

Run the included unit tests:
```bash
javac -cp "lib/*" src/*.java
java -cp ".;lib/junit.jar" org.junit.platform.console.ConsoleLauncher --select-class src.UnitTests
```

## Technical Details

- **Language:** Java 17+
- **GUI:** Swing with FlatLaf look-and-feel
- **Build System:** Custom scripts with smart build detection
- **Data Storage:** Serialized objects for configurations and results
- **Testing:** JUnit 5

## License

MIT License - see LICENSE file for details.

## Contact

Jesse Lerner - [GitHub](https://github.com/Electrolyzer)
