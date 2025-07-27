# A* Maze Solver - Interactive Algorithm Comparison Tool

A comprehensive Java implementation of A* search algorithms with interactive visualization and performance analysis capabilities. This project demonstrates advanced algorithmic concepts, GUI development, and data analysis techniques.

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Build](https://img.shields.io/badge/Build-Passing-green.svg)

## Project Overview

This project implements and compares three variants of the A* search algorithm for maze solving:

- **Repeated Forward A*** - Plans from current position to goal
- **Repeated Backward A*** - Plans from goal to current position  
- **Adaptive A*** - Learns better heuristics from previous searches

The application features two main components:
1. **Interactive Maze Editor** - Create, edit, and solve mazes step-by-step
2. **Data Collector** - Run large-scale performance analysis across multiple mazes

## Key Features

### Algorithm Implementation
- Three A* variants with configurable parameters
- Partial maze observability with sight radius simulation
- Tiebreaker strategies (g-value vs h-value preference)
- Step-by-step visualization of search process

### Interactive Tools
- **Maze Editor**: Create custom mazes with intuitive click-to-edit interface
- **Real-time Visualization**: Watch algorithms solve mazes step-by-step
- **Algorithm Comparison**: Run multiple algorithms on the same maze
- **Save/Load System**: Persist results for later analysis

### Performance Analysis
- **Batch Processing**: Analyze performance across 50+ predetermined mazes
- **Statistical Analysis**: Compare expanded cells, execution time, success rates
- **Heat Maps**: Visualize exploration patterns across different algorithms
- **Export Capabilities**: Save results and visualizations

## Technology Stack

- **Language**: Java 17+
- **GUI Framework**: Swing with FlatLaf modern look-and-feel
- **Testing**: JUnit 5
- **Build System**: Custom scripts with organized build directory (Windows batch + Unix shell)
- **Data Structures**: Custom priority queue implementation
- **Algorithms**: A* search with Manhattan distance heuristic

## Quick Start

### Prerequisites
- Java 17 or higher
- Windows (batch files) or Unix/Linux (shell scripts)

### Installation & Running

**Quick Start (Windows):**
```bash
# Clone the repository
git clone https://github.com/Electrolyzer/A-Star_Maze_Solvers.git
cd A-Star_Maze_Solvers

# Use the interactive launcher (recommended)
.\launcher.bat
```

**Manual Commands (Windows):**
```bash
# Build the project
.\build.bat

# Run the interactive maze editor
.\run-mazetester.bat

# Run the data collector for performance analysis
.\run-datacollector.bat

# Test the build system
.\test-build.bat

# Clean build files
.\clean.bat
```

**Unix/Linux/macOS:**
```bash
# Clone the repository
git clone https://github.com/Electrolyzer/A-Star_Maze_Solvers.git
cd A-Star_Maze_Solvers

# Build the project
./build.sh

# Run the interactive maze editor
./run-mazetester.sh

# Run the data collector for performance analysis
./run-datacollector.sh
```

## Usage Guide

### Interactive Maze Editor
1. **Create/Edit Mazes**: Use the editing tools to place start/end points and walls
2. **Configure Algorithm**: Select algorithm type, tiebreaker, and sight radius
3. **Solve**: Watch the algorithm solve the maze step-by-step
4. **Save Results**: Export solving sessions for later analysis

### Data Collector
1. **Select Algorithms**: Choose which A* variants to compare
2. **Configure Parameters**: Set maze size, count, and algorithm parameters
3. **Run Analysis**: Process multiple mazes and collect performance data
4. **View Results**: Analyze performance tables, statistics, and heat maps

## Algorithm Performance

The project includes comprehensive performance testing across 50 predetermined mazes. Key metrics include:

- **Expanded Cells**: Number of nodes explored during search
- **Execution Time**: Algorithm runtime in milliseconds
- **Success Rate**: Percentage of successfully solved mazes
- **Memory Usage**: Exploration pattern analysis via heat maps

### Sample Results
| Algorithm | Avg Expanded Cells | Avg Time (ms) | Success Rate |
|-----------|-------------------|---------------|--------------|
| Forward A* (g) | 9,847 | 12.3 | 100% |
| Forward A* (h) | 254,891 | 45.7 | 100% |
| Backward A* (g) | 125,432 | 28.9 | 100% |
| Adaptive A* (g) | 9,623 | 13.1 | 100% |

## Architecture

### Core Components
- **MazeSolver**: Implements the three A* algorithm variants
- **MazeGenerator**: Creates random solvable mazes
- **PriorityQueue**: Custom implementation optimized for A* search
- **DataCollectorUI**: Comprehensive analysis interface
- **MazeSolverTester**: Interactive maze editing and solving

### Design Patterns
- **Strategy Pattern**: Interchangeable algorithm implementations
- **Observer Pattern**: UI updates during algorithm execution
- **Factory Pattern**: Maze generation with different parameters
- **Command Pattern**: Undo/redo functionality in maze editor

## Testing

The project includes comprehensive unit tests that verify algorithm correctness:

```bash
# Run unit tests (if IDE doesn't auto-run)
javac -cp "lib/*" src/*.java
java -cp ".;lib/junit.jar" org.junit.platform.console.ConsoleLauncher --select-class src.UnitTests
```

Tests validate that each algorithm produces expected results on predetermined mazes, ensuring implementation correctness.

## Performance Optimizations

- **Efficient Data Structures**: Custom priority queue with O(log n) operations
- **Memory Management**: Reusable data structures across search iterations
- **Heuristic Learning**: Adaptive A* improves performance over time
- **Sight Radius Optimization**: Configurable observation distance for realistic scenarios

## Educational Value

This project demonstrates:
- **Algorithm Design**: Implementation of classic AI search algorithms
- **Data Structure Optimization**: Custom priority queue for performance
- **GUI Development**: Complex Swing interfaces with custom rendering
- **Performance Analysis**: Statistical comparison of algorithm variants
- **Software Engineering**: Clean architecture, testing, and documentation

## Future Enhancements

- [ ] Additional search algorithms (Dijkstra, Greedy Best-First)
- [ ] 3D maze support
- [ ] Network-based multiplayer maze solving
- [ ] Machine learning integration for heuristic optimization
- [ ] Web-based interface using Java-to-JavaScript compilation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## Contact

**Jesse** - [GitHub Profile](https://github.com/Electrolyzer)

Project Link: [https://github.com/Electrolyzer/A-Star_Maze_Solvers](https://github.com/Electrolyzer/A-Star_Maze_Solvers)

---

*This project showcases advanced Java programming, algorithm implementation, and software engineering practices suitable for technical interviews and portfolio demonstrations.*
