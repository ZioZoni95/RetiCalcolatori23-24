# Hotelier

Sistema client/server per la consultazione e la recensione di hotel (progetto di Reti di Calcolatori).
Java 21, Maven, Jackson, Lanterna (TUI) e Swing (GUI).

## Struttura della repo

```
.
├── pom.xml                  progetto Maven padre (moduli: common, server, client)
├── common/                  codice condiviso da client e server
│   └── src/main/java/hotelier/{model,protocol,rmi,config,util}
├── server/                  server Hotelier  →  server/target/hotelier-server.jar
│   └── src/main/java/hotelier/server/        nucleo (NIO, ranking, RMI, multicast)
│       └── ui/                               interfacce: headless, TUI, GUI
├── client/                  client Hotelier  →  client/target/hotelier-client.jar
│   └── src/main/java/hotelier/client/        nucleo (TCP, RMI, multicast)
│       └── ui/                               interfacce: CLI, TUI, GUI
├── data/
│   ├── server/              Hotels.json, Users.json, Reviews.json, ServerConfig.json
│   └── client/              ClientConfig.json
└── .github/workflows/ci.yml build + test a ogni push e pull request
```

(`Esercizi_Slides/`, se presente in locale, non fa parte del progetto ed è ignorato da git.)

## Build ed esecuzione

```bash
mvn verify        # compila, esegue i test e produce i due jar eseguibili
```

Eseguire **dalla radice della repo** (le cartelle dati di default sono relative):

```bash
java -jar server/target/hotelier-server.jar [--ui headless|tui|gui] [cartella-dati]   # default: data/server
java -jar client/target/hotelier-client.jar [--ui cli|tui|gui]      [cartella-config] # default: data/client
```

Il server va avviato prima dei client. Senza `--ui` viene scelta l'interfaccia migliore disponibile:
**GUI** se c'è uno schermo, altrimenti **TUI** se c'è un terminale interattivo, altrimenti **headless/CLI**.

## Interfacce

| | Server | Client |
|---|---|---|
| **headless / CLI** | log degli eventi su console | riga di comando (comandi sotto) |
| **TUI** (terminale, Lanterna) | dashboard a schermo intero: stato, statistiche, utenti collegati, log eventi; pulsanti *Avvia/Ferma*, *Ricalcola ranking*, *Esci* | menu a sinistra, risultati al centro, notifiche in basso; form per login, ricerche e recensioni |
| **GUI** (Swing) | configurazione (porte, indirizzi, intervallo ranking), Avvia/Ferma, dashboard, tabelle utenti e hotel, log | login/registrazione, ricerca con tabella e dettaglio, recensioni, classifiche locali, notifiche |

TUI: frecce e Invio nei menu, Tab per cambiare riquadro. Tutte le interfacce usano lo stesso nucleo
(`ServerRuntime` per il server, `HotelierClient` per il client): cambia solo la presentazione.

### Comandi della CLI
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
Gli argomenti vanno tra virgolette; i punteggi sono interi da 0 a 5. Dopo il login viene chiesto l'elenco delle città
di interesse (ognuna tra virgolette) per ricevere le variazioni delle loro classifiche.

## Architettura

| Canale | Uso |
|--------|-----|
| **TCP** (NIO, un selector + pool di worker) | login, logout, ricerca hotel, inserimento recensioni, badge |
| **RMI** | registrazione utenti; callback con le variazioni delle classifiche locali delle città di interesse |
| **UDP multicast** | notifica a tutti i client loggati quando cambia il primo hotel di una città |

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

### Dati e password
`Hotels.json` è obbligatorio; `Users.json` e `Reviews.json` vengono creati vuoti se mancano; i file di configurazione
vengono creati con i valori di default se mancano. Le password sono salvate come hash PBKDF2-HMAC-SHA256 con sale
(i vecchi file con password in chiaro vengono convertiti al primo avvio) e sono case sensitive.

## CI
`.github/workflows/ci.yml` esegue `mvn verify` (Java 21) a ogni push e pull request e pubblica i due jar e i report dei test come artefatti.
I test della GUI funzionano anche senza display (si prova il pannello Swing, non la finestra) e quelli della TUI usano il terminale virtuale di Lanterna.
