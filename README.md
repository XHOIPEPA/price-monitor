# Price Monitor — Event-Driven Price Monitoring me Kafka

> Tema e diplomes: **Monitorim çmimesh i drejtuar nga ngjarjet (event-driven), me web scraping dhe Apache Kafka** (Java / Spring Boot, arkitekture microservices).

## Qellimi i sistemit

Sistemi kontrollon rregullisht (p.sh. cdo 10 minuta) nje liste produktesh online, zbulon nese ka ndryshuar cmimi ose disponueshmeria (stock), e ruan historikun ne databaze dhe e ekspozon permes nje REST API.

## Arkitektura

```
Scheduler ──▶ [scrape-requests] ──▶ Scraper Workers ──▶ [product-snapshots] ──▶ Change Detector ──▶ PostgreSQL
                                                                                      │
                                                                                      ▼ (vetem kur ka ndryshim)
                                                                              [price-events] ──▶ Alert Service (opsional)

PostgreSQL ◀── API Service (REST) ◀── klientet / dashboard (opsional)
```

Emrat ne kllapa katrore `[...]` jane **topike Kafka**.

## Sherbimet

| Sherbimi | Teknologjia | Roli |
|---|---|---|
| `scheduler-service` | Spring Boot | Cdo X minuta krijon detyra scraping dhe i publikon te `scrape-requests` |
| `scraper-service` | Spring Boot + Jsoup | Konsumon `scrape-requests`, ben scraping (cmim, stock, titull), publikon te `product-snapshots`. Shkallëzohet horizontalisht (shume instanca ne te njejtin consumer group) |
| `change-detector-service` | Spring Boot + JPA | Konsumon `product-snapshots`, krahason me gjendjen ne DB, ruan historikun, publikon `price-events` vetem kur ka ndryshim |
| `api-service` | Spring Boot REST | Ekspozon `/products` dhe `/products/{id}/history` duke lexuar nga DB |
| `alert-service` | Spring Boot | (Opsionale) Konsumon `price-events` dhe dergon njoftime |

## Topiket Kafka

| Topiku | Producer | Consumer | Permbajtja |
|---|---|---|---|
| `scrape-requests` | Scheduler | Scraper workers | `{productId, url, scheduledAt}` |
| `product-snapshots` | Scraper workers | Change Detector | `{productId, price, currency, inStock, scrapedAt}` |
| `price-events` | Change Detector | Alert Service | Ndryshimet e detektuara |

**Key i mesazheve = `productId`** — keshtu te gjitha mesazhet e te njejtit produkt bien te i njejti partition dhe perpunohen ne renditje.

## Pse Kafka?

- **Loose coupling** — sherbimet nuk e njohin njera-tjetren, njohin vetem topiket.
- **Asinkronizem** — Scheduler nuk pret qe scraping te mbaroje.
- **Durability** — mesazhet ruhen ne disk; nese nje sherbim bie, i merr kur rikthehet.
- **Shkallëzim** — shume scraper workers ne te njejtin consumer group ndajne partitionet automatikisht.

## Si ta nisesh lokalisht

```bash
# 1. Ngri infrastrukturen (Kafka + PostgreSQL)
docker compose up -d

# 2. Krijo topiket (vetem heren e pare)
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create --topic scrape-requests --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create --topic product-snapshots --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create --topic price-events --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092

# 3. Nis sherbimet (secili ne terminalin e vet, ose me nje script)
#    (do te plotesohet kur te krijohen sherbimet)
```

## Struktura e repos

```
price-monitor/
├── docker-compose.yml          # Kafka (KRaft) + PostgreSQL
├── docs/                       # Shenime per relacionin e diplomes
├── scheduler-service/
├── scraper-service/
├── change-detector-service/
├── api-service/
└── alert-service/              # opsional
```

## Statusi i punes

- [x] Infrastruktura me Docker (Kafka 4.0 KRaft + PostgreSQL)
- [ ] Scheduler service
- [ ] Scraper service
- [ ] Change detector service
- [ ] API service
- [ ] Alert service (opsionale)
- [ ] Dashboard (opsionale, nese del koha)
