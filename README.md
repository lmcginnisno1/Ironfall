# Ironfall

Ironfall is a 2D factory automation and economic strategy game built using the libGDX framework. Players extract raw resources, transport them via conveyor networks to a central hub, and manage a dynamic market economy to fund factory expansion.

## Core Features

- **Resource Extraction:** Automated mining structures harvest raw ores from the game world.
- **Logistics Networks:** Conveyor belts route physical items across the map grid.
- **Centralized Core Storage:** A unified inventory system that collects and manages transported materials.
- **Market Interface:** Multi-functional slider configurations and quick-liquidation controls that translate physical inventory into game currency.
- **Persistent Currency Systems:** Real-time HUD tracking for credit tracking and resource values.

## Platforms

- `core`: Shared module containing all core simulation logic, rendering controllers, and UI states.
- `lwjgl3`: Desktop execution platform leveraging Lightweight Java Game Library 3 (LWJGL3).

## Running the Game

### Prerequisites
To run the pre-built application or compile from source, you must have Java Development Kit (JDK) 17 or higher installed on your system.

### Option 1: Download Pre-built Binaries
Compiled executables are provided for stable tag releases.
1. Navigate to the [releases](https://github.com/lmcginnisno1/Ironfall/releases) page of this repository.
2. Download the runnable JAR file (```Ironfall-<version>.jar```).
3. Execute the binary from your terminal:
   ```java -jar Ironfall-<version>.jar```

### Option 2: Build from Source
To compile and execute the project locally using the Gradle wrapper:

1. Clone the repository and navigate to the project root directory.
2. Ensure the Gradle execution script is executable (Linux/macOS):
   ```chmod +x gradlew```
3. Compile the standalone runnable JAR:
   ```./gradlew lwjgl3:jar```
4. Run the compiled application located in the build directory:
   ```java -jar lwjgl3/build/libs/Ironfall-<verison>.jar```

   Alternatively, you can build and launch the environment directly using:
   ```./gradlew lwjgl3:run```

## Development and Gradle Configuration

This project manages dependencies and build lifecycle automation via Gradle. The wrapper scripts (`gradlew` and `gradlew.bat`) ensure predictable execution environments without requiring a local Gradle install.

### Essential Development Tasks

- `clean`: Removes generated build directories and compiled class caches across all modules.
- `lwjgl3:run`: Compiles dependency trees and launches the application desktop module immediately.
- `lwjgl3:jar`: Bundles application assets and compiled class signatures into a singular, distributable JAR package.
- `idea` / `eclipse`: Generates localized metadata targets for importing the project layout into specific Integrated Development Environments.
