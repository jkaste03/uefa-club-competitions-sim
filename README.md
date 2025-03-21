# UEFA Club Competitions Simulator (in development)

This project simulates the UEFA Champions League, Europa League, and Conference League. The goal is to develop a Java Maven program that simulates these competitions and calculates probabilities based on the frequency of different events. For example:

- Average coefficient points earned per club or country.
- Probabilities for clubs to reach specific tournament rounds.
- Various other statistics derived from simulated match events.

**Note:** This project is still under active development and is far from complete. For me, this is a demanding project that will take considerable time to complete. It is one of my most ambitious projects. Keep that in mind (;

## Structure

The project follows Maven conventions and is organized into several packages:

- **`com.github.jkaste03.uefa_cc_sim`**  
  Contains the main class `UefaCCSim`.

- **`com.github.jkaste03.uefa_cc_sim.enums`**  
  Contains enums representing various competition data.

- **`com.github.jkaste03.uefa_cc_sim.model`**  
  Contains classes representing different tournament rounds, clubs, and other models.

- **`com.github.jkaste03.uefa_cc_sim.service`**  
  Contains services for loading data and fetching Elo ratings for clubs.

- **`com.github.jkaste03.uefa_cc_sim.threads`**  
  Contains the `SimulationThread` class for running simulations in parallel.

- **`com.github.jkaste03.uefa_cc_sim.data`**  
  Contains data files (CSV and JSON) used in the simulations.

## How to Run

1. Clone the repository to your local machine.
2. Open the project in your preferred IDE.
3. Build the project using Maven:
   ```bash
   mvn clean install
   ```
4. Run the main class:
   ```bash
   mvn exec:java -Dexec.mainClass="com.github.jkaste03.uefa_cc_sim.UefaCCSim"
   ```

## Dependencies

- **Gson:** For reading JSON data.
- **JUnit:** For unit testing.
- **Java Standard Library:** For basic functionality.

## License

This project is licensed under the MIT License.
