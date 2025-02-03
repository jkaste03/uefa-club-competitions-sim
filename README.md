# UEFA Club Competitions Simulator (in development)

This project simulates the UEFA Champions League, Europa League, and Europa Conference League using club data (and Elo ratings?).

## Structure

The project is divided into several packages:

- `com.example`: Contains the main class `UefaCCSim`.
- `com.example.rounds`: Contains classes representing different rounds in the tournaments.
- `com.example.clubs`: Contains classes representing clubs and countries.
- `com.example.api`: Contains the class `ClubEloAPI` which fetches and stores Elo ratings for clubs.

## How to Run

1. Clone the project to your local machine.
2. Open the project in your favorite IDE.
3. Run the `UefaCCSim` class which contains the `main` method.

```bash
git clone <repository-url>
cd uefa_cc_sim
# Open the project in IDE and run UefaCCSim.java
```

## Dependencies

The project uses the following dependencies:

- Jackson: For reading JSON data.
- Java Standard Library: For basic functionality.

## License

This project is licensed under the MIT License.