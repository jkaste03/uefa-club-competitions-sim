# UEFA Club Competitions Simulator (in development)

Dette prosjektet simulerer UEFA Champions League, Europa League og Europa Conference League ved hjelp av klubbdata (og Elo-rangeringer?).

## Struktur

Prosjektet er delt inn i flere pakker:

- `com.example`: Inneholder hovedklassen `UefaCCSim`.
- `com.example.rounds`: Inneholder klasser som representerer forskjellige runder i turneringene.
- `com.example.clubs`: Inneholder klasser som representerer klubber og land.
- `com.example.api`: Inneholder klassen `ClubEloAPI` som henter og lagrer Elo-rangeringer for klubber.

## Hvordan kjøre

1. Klon prosjektet til din lokale maskin.
2. Åpne prosjektet i din favoritt-IDE.
3. Kjør `UefaCCSim`-klassen som inneholder `main`-metoden.

```bash
git clone <repository-url>
cd uefa_cc_sim
# Åpne prosjektet i IDE og kjør UefaCCSim.java
```

## Avhengigheter

Prosjektet bruker følgende avhengigheter:

- Jackson: For å lese JSON-data.
- Java Standard Library: For grunnleggende funksjonalitet.

## Lisens

Dette prosjektet er lisensiert under MIT-lisensen.