# Shenime per relacionin e diplomes

> Ky skedar mbush gradualisht materialin per relacionin. Pas cdo hapi te punes, ketu shtohet shpjegimi konceptual i asaj qe u ndertua dhe *pse* u ndertua ashtu. Ne fund, keto seksione behen kapitujt e relacionit.

---

## 1. Infrastruktura (Docker: Kafka + PostgreSQL)

**Cfare u be:** U ngrit infrastruktura baze me `docker-compose.yml`: nje broker Kafka 4.0 ne KRaft mode dhe nje databaze PostgreSQL 16.

**Pse Docker:** Docker lejon qe e gjithe infrastruktura te pershkruhet si kod (Infrastructure as Code) dhe te ngrihet me nje komande te vetme (`docker compose up -d`) ne cdo makine, pa instalime manuale. Kjo garanton qe mjedisi i zhvillimit eshte i riprodhueshem.

**Pse Kafka pa ZooKeeper (KRaft):** Deri ne versionin 3.x, Kafka varej nga Apache ZooKeeper per menaxhimin e metadata-ve te cluster-it (control plane): cilat topike ekzistojne, kush eshte leader i cdo partitioni, cilet brokers jane gjalle. Nga versioni 4.0, ky rol eshte zhvendosur brenda vete Kafka-s permes protokollit KRaft (Kafka Raft), bazuar ne algoritmin e konsensusit Raft. Ne konfigurimin tone, `KAFKA_PROCESS_ROLES: broker,controller` do te thote qe i njejti proces luan te dy rolet: broker (ruan te dhenat) dhe controller (mban metadata-t). Kjo thjeshton ndjeshem arkitekturen: nje sistem i shperndare me pak ne stack.

**Konfigurimi kryesor i Kafka-s:**
- `KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093` — kuorumi i Raft ka vetem nje votues (single-node, i mjaftueshem per zhvillim).
- `replication-factor: 1` — me nje broker te vetem nuk ka ku te replikohen te dhenat; ne prodhim do te ishte 3.
- Porti `9092` ekspozohet nga container-i te makina lokale qe sherbimet Spring Boot te lidhen me `localhost:9092`.

---

## 2. Dizajni i topikeve dhe i celesave

**Topiket:** `scrape-requests`, `product-snapshots`, `price-events` — nje topik per cdo lloj ngjarjeje ne sistem. Topiku ne Kafka eshte nje log i emertuar, i pandryshueshem (append-only), i ndare ne partitione.

**Partitionet (3 per topik):** Partitioni eshte njesia e paralelizmit dhe e renditjes. Me 3 partitione, deri ne 3 scraper workers ne te njejtin consumer group mund te punojne paralelisht, ku secili merr nje partition.

**Celesi = productId:** Kafka garanton renditjen e mesazheve vetem brenda nje partitioni. Duke perdorur `productId` si key, `hash(productId) % 3` con te gjitha mesazhet e te njejtit produkt te i njejti partition — keshtu snapshot-et e nje produkti perpunohen gjithmone ne renditjen e sakte kohore, cka eshte kritike per detektimin korrekt te ndryshimeve te cmimit.

---

## 3. Scheduler Service

*(do te plotesohet)*

---

## 4. Scraper Service

*(do te plotesohet)*

---

## 5. Change Detector Service

*(do te plotesohet)*

---

## 6. API Service

*(do te plotesohet)*
