# UEFA Club Competitions Simulator (in development)

This project simulates the UEFA Champions League, Europa League, and Conference League. The goal is to develop a Java Maven program that simulates these competitions and calculates probabilities based on the frequency of different events. For example:

- Average coefficient points earned per club or country.
- Probabilities for clubs to reach specific tournament rounds.
- Various other statistics derived from simulated match events.

**Note:** This project is still under active development and is far from complete.
**Important notes on match outcomes:**

I do not plan to factor in club strength in this simulation. Instead, match outcomes are determined randomly. Although Elo data is available, the logic to generate realistic match outcomes has not been implemented – this functionality is left for others to expand upon. I acknowledge that not incorporating team strength results in nothing close to realistic outcomes, but given my mathematical limitations, I have chosen to focus on other aspects of development.

## Structure

The project follows Maven conventions and is organized into several packages:

- **`com.github.jkaste03.uefa_cc_sim`**  
  Contains the main class `UefaCCSim`.

- **`com.github.jkaste03.uefa_cc_sim.rounds`**  
  Contains classes representing different tournament rounds.

- **`com.github.jkaste03.uefa_cc_sim.clubs`**  
  Contains classes representing clubs, including their rankings and associated data.

- **`com.github.jkaste03.uefa_cc_sim.api`**  
  Contains the `ClubEloAPI` class which fetches and stores Elo ratings for clubs.

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

- **Jackson:** For reading JSON data.
- **Java Standard Library:** For basic functionality.

## License

This project is licensed under the MIT License.
