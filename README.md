# Hotelier

Sistema client/server per la consultazione e la recensione di hotel (progetto di Reti di Calcolatori).
Java 21, Maven, Jackson.

## Architettura

| Canale | Uso |
|--------|-----|
| **TCP** (NIO, un selector + pool di worker) | login, logout, ricerca hotel, inserimento recensioni, badge |
| **RMI** | registrazione utenti; callback con le variazioni delle classifiche locali delle città di interesse |
| **UDP multicast** | notifica a tutti i client loggati quando cambia il primo hotel di una città |

```
src/main/java/hotelier
├── model      Hotel, Ratings, Review, User, Badge, CityRanking (record immutabili)
├── protocol   Message (messaggi JSON) e Wire (framing su TCP)
├── rmi        interfacce remote ServerService / ClientCallback
├── config     ServerConfig, ClientConfig
├── util       Json, JsonStore (scrittura atomica), PasswordHasher (PBKDF2), Result
├── server     ServerMain, NioServer, Connection, Session, repository/servizi, RankingService, RmiServer, MulticastNotifier
└── client     ClientMain, Cli, CommandParser, TcpClient, RmiClient, MulticastListener
```

### Protocollo TCP
Ogni messaggio è `[4 byte: lunghezza N, big endian][N byte: JSON UTF-8]` (massimo 1 MiB).
Il JSON ha il campo `type` (`login`, `logout`, `search_hotel`, `search_city`, `insert_review`, `badge`
per le richieste; `success`, `failure`, `hotel_result`, `hotel_list_result`, `badge_result` per le risposte).
Il server risponde in ordine a ogni richiesta, anche se il client ne invia più di una senza attendere.

### Ranking
`rank = qualità × (0.5 + 0.3 × quantità + 0.2 × attualità)`, nell'intervallo [0, 5], con
qualità = media tra punteggio complessivo e media dei criteri, quantità = `n / (n + 10)`,
attualità = media di `0.5^(giorni / 90)` sulle recensioni. Gli hotel senza recensioni hanno rank 0.
Il calcolo è ripetuto ogni `rankingInterval` secondi (vedi `RankCalculator`, `RankingService`).

### Badge
Recensore (0–1 recensioni), Recensore Esperto (2), Contribuente (3), Contribuente Esperto (4), Super Contribuente (≥ 5).

### Password
Salvate come hash PBKDF2-HMAC-SHA256 con sale. I file `Users.json` con password in chiaro vengono
convertiti automaticamente al primo avvio del server. Le password sono case sensitive.

## Build ed esecuzione

```bash
mvn verify                      # compila, esegue i test, produce target/hotelier.jar

java -cp target/hotelier.jar hotelier.server.ServerMain [cartella-dati]   # default: resources
java -cp target/hotelier.jar hotelier.client.ClientMain [cartella-config] # default: resources
```

La cartella dati contiene `Hotels.json` (obbligatorio), `Users.json` e `Reviews.json` (creati vuoti se mancano),
`ServerConfig.json` e `ClientConfig.json` (creati con i valori di default se mancano).
Il server va avviato prima dei client.

## Comandi del client

```
register "username" "password"
login "username" "password"
searchHotel "nomeHotel" "città"
searchAllHotels "città"
insertReview "nomeHotel" "città" "GlobalScore" "CleaningScore" "PositionScore" "ServicesScore" "QualityScore"
showMyBadges
showLocalRanks
help | logout | exit
```
Gli argomenti vanno tra virgolette; i punteggi sono interi da 0 a 5. Dopo il login il client chiede le città di
interesse (ognuna tra virgolette) per ricevere le variazioni delle loro classifiche.

## CI
`.github/workflows/ci.yml` esegue `mvn verify` (Java 21) a ogni push e pull request e pubblica il jar e i report dei test come artefatti.
