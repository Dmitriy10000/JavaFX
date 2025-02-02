# Oxygen Mask Control Application

## Overview

This JavaFX-based application is designed to manage an oxygen mask, record sensor data, export reports, and track historical statistics. The system communicates with an Arduino-controlled mask via a COM port.

## Features

- **Real-time monitoring** of sensor data from the oxygen mask
- **Data storage** in a local SQLite database
- **User roles:** User, Doctor and Engineer, each with specific access levels
- **Graphical visualization** of recorded sensor data
- **Export functionality** to save reports in a structured format (XLS file)
- **Resizable UI** with dynamic scaling for different window sizes
- **Calibration settings** in the Engineer Menu, allowing users to adjust coefficients for sensor calibration
- **Valve control sliders** to manage the oxygen mask valves
- **Min/Max valve opening settings** to define the operational range of the valves
- **Full multilingual support** (English, Russian, Kazakh) with real-time language switching on any page, preserving language preferences across navigation

## Requirements

- **Java Development Kit (JDK):** [Download](https://www.oracle.com/java/technologies/downloads/)

## Installation
1. Download the latest release: [Releases](https://github.com/Dmitriy10000/OxygenMaskControl/releases)
2. Run the application:
   ```sh
   java -jar JavaFX.jar
   ```

## Configuration

- The application stores data in:
  ```
  AppData/Roaming/HealthMonitor/database.sqlite
  ```
- COM port settings can be adjusted in `ComPortController.java`.

## Usage

1. **Connect the oxygen mask** to the system via USB (Arduino-based controller).
2. **Start the application** and select the appropriate user role (Doctor or Engineer).
3. **Monitor real-time data** or navigate to historical statistics.
4. **Export reports** as needed in XLS format.
5. **Use the Engineer Menu** to fine-tune sensor calibration and valve movement restrictions.
6. **Switch the application language** at any time, and the setting will be preserved when navigating between pages.

## Screenshots

### Login Screen
![Login Screen](./screenshots/login.png)

### Account Creation
![Account Creation](./screenshots/signup.png)

### Main Dashboard
![Main Dashboard](./screenshots/dashboard.png)

### Data Export
![Data Export](./screenshots/export.png)

### Graph Visualization
![Graph Visualization](./screenshots/graph1.png)
![Resizable Graph](./screenshots/graph2.png)

### User Profile
![User Profile](./screenshots/profile.png)

### Engineer Settings
![Engineer Settings](./screenshots/engineer.png)

### Doctor Menu (Group Creation)
![Group Creation](./screenshots/doctor.png)

## Contributing

If you wish to contribute to this project, feel free to submit a pull request!

## License

MIT License. See `LICENSE` for details.
