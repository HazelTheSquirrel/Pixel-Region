# Pixel-Region — Release Audit & Abnahme

## 1. Zweck und Freigabestatus

Dieses Dokument ist die verbindliche technische Abnahme für **Pixel-Region**.

Pixel-Region ist ein eigenständiges polygonbasiertes Regionsystem für **Paper 26.2**. PixelRPG wurde ausschließlich als Architekturvergleich verwendet; es besteht **keine Laufzeit-, Build- oder Code-Abhängigkeit**.

### Freigabestatus

**🟢 RELEASE CANDIDATE — FREIGEGEBEN**

Nach Prüfung des aktuellen Repository-Stands auf `main`, der Implementierung, Tests, Build-/Verifikationslogik, Dokumentation und des zuletzt erfolgreichen CI-Laufs bestehen **keine offenen Muss-Punkte innerhalb des definierten Zielumfangs**.

Das System kann als **fertige Basisversion veröffentlicht und anschließend funktional erweitert** werden.

### Statusregeln

- [ ] offen
- [-] in Arbeit
- [x] implementiert und verifiziert
- [~] bewusst nicht Bestandteil des Zielumfangs

Ein Punkt darf nur mit [x] markiert werden, wenn Implementierung und relevanter Verifikationsnachweis vorhanden sind.

---

## 2. Verbindliche Plattform

- [x] Paper 26.2
- [x] Dev Bundle `26.2.build.121-stable`
- [x] Java 25
- [x] Mojang-Mappings
- [x] `paper-plugin.yml`
- [x] aktuelle Paper-26.x-APIs
- [x] Adventure Components / MiniMessage
- [x] Gradle 9.2.0
- [x] Shadow 9.6.1
- [x] Repository `HazelTheSquirrel/Pixel-Region`
- [x] Release-Zielbranch `main`

---

## 3. Abgenommener Funktionsumfang

### Core

- [x] Plugin-Grundstruktur
- [x] polygonbasierte X/Z-Geometrie
- [x] World UUID
- [x] Min-Y / Max-Y
- [x] Priority
- [x] Owner
- [x] Members
- [x] Region Flags
- [x] Bounding Box
- [x] gecachte RegionGeometry
- [x] zentrale RegionValidator-Validierung
- [x] case-insensitive Regionsnamen
- [x] doppelte benachbarte Punkte werden abgelehnt
- [x] Nullflächen werden abgelehnt
- [x] Selbstüberschneidungen werden abgelehnt
- [x] nicht-finite Koordinaten werden abgelehnt

### Query / Policy

- [x] World-Filter
- [x] Y-Filter
- [x] Bounding-Box-Filter
- [x] Polygon-Filter
- [x] Overlapping Regions
- [x] Priority-basierte Auswertung
- [x] Flag-Fallthrough bei unset
- [x] konfigurierte Defaults
- [x] zentrale Owner-/Member-Auswertung
- [x] globaler `pixelregion.bypass`
- [x] vollständige Applicable-Region-Menge statt nur Top-Region

### Protection

- [x] BUILD
- [x] USE
- [x] CONTAINER_USE
- [x] PVP
- [x] MOB_DAMAGE
- [x] ENTITY_DAMAGE
- [x] EXPLOSION
- [x] FIRE_SPREAD
- [x] FLUID_FLOW
- [x] MOB_SPAWN
- [x] ENTRY
- [x] EXIT

Abgedeckte aktuelle Paper-Events umfassen Block Break/Place, Interaktion, Container Opening, Combat/Damage, Explosionen, Fluids, Fire-Ignition/Spread, Mob Spawn und Movement/Teleport.

### Movement

- [x] Entry/Exit
- [x] outside → inside
- [x] inside → outside
- [x] Region A → Region B
- [x] Overlap-Übergänge
- [x] Y-Grenzen
- [x] World-Wechsel
- [x] Teleports über die aktuelle Paper-Bewegungskette
- [x] keine falschen Transitionen bei unveränderter Regionsmenge

### Editor / Commands

- [x] create
- [x] point
- [x] finish
- [x] cancel
- [x] list
- [x] info
- [x] edit
- [x] priority
- [x] miny / maxy
- [x] point add / set / remove
- [x] flags
- [x] members
- [x] delete
- [x] reload
- [x] save
- [x] debug

Ungültige Änderungen ersetzen keine bestehende gültige Region.

---

## 4. Architektur-Abnahme

### Verantwortlichkeiten

- **Region** — Regiondaten, Owner, Members und explizite Flags.
- **RegionGeometry** — vorberechnete Polygongeometrie und Bounds.
- **RegionValidator** — zentrale Validierung.
- **RegionManager** — In-Memory-Datenhaltung, Spatial Index und Region Queries.
- **RegionPolicyService** — zentrale Policy- und Flag-Auswertung.
- **RegionTransitionService** — Entry/Exit und Movement-Transitions.
- **RegionStorage / JsonRegionStorage** — Persistenz.
- **RegionMessages** — konfigurierte Adventure-/MiniMessage-Ausgabe.
- **Listener** — dünne Adapter ohne duplizierte Policy-Logik.

### Performance

- [x] World+Chunk-Spatial-Index
- [x] Regionen werden in alle betroffenen Chunks indiziert
- [x] Index wird bei Load/Add/Remove/Replace aktualisiert
- [x] Kandidatenfilter vor Bounds-/Polygonprüfung
- [x] Geometry wird wiederverwendet
- [x] Many-Region-Test vorhanden
- [~] zusätzlicher Query-Cache nicht erforderlich
- [~] zusätzliche Performance-Metriken nicht erforderlich

Der aktuelle Spatial Index ist die vorgesehene Performance-Basis; zusätzliche Caches würden die Komplexität erhöhen, ohne für den definierten Umfang einen belegten Mehrwert zu liefern.

---

## 5. Persistenz / Datenintegrität

- [x] RegionStorage-Abstraktion
- [x] JsonRegionStorage
- [x] Schema-Version
- [x] Legacy-Array-Format lesbar
- [x] zukünftige unbekannte Schema-Versionen werden abgelehnt
- [x] temporäre Datei
- [x] Backup-Datei
- [x] atomarer Replace, wenn unterstützt
- [x] sicherer Replace-Fallback
- [x] Backup-Recovery bei defekter Primärdatei
- [x] Dirty-State
- [x] Save nach relevanten Mutationen
- [x] Save bei Shutdown
- [~] asynchrones I/O nicht erforderlich

Das Storage-System priorisiert Datenintegrität und verhindert, dass unbekannte zukünftige Formate stillschweigend überschrieben werden.

---

## 6. Permissions

Im Plugin-Descriptor und Command-Code konsistent vorhanden:

- [x] `pixelregion.command`
- [x] `pixelregion.command.create`
- [x] `pixelregion.command.list`
- [x] `pixelregion.command.info`
- [x] `pixelregion.command.edit`
- [x] `pixelregion.command.delete`
- [x] `pixelregion.command.flag`
- [x] `pixelregion.command.member`
- [x] `pixelregion.command.reload`
- [x] `pixelregion.command.save`
- [x] `pixelregion.command.debug`
- [x] `pixelregion.admin`
- [x] `pixelregion.bypass`

Root-Command ist operator-default. Eigentümerprüfung bleibt für normale Regionsmutationen aktiv; `pixelregion.admin` erlaubt administrative Kontrolle.

---

## 7. Diagnostics / Betrieb

- [x] `/pixelregion debug`
- [x] aktuelle Location
- [x] passende Regionen
- [x] Priority
- [x] expliziter Flag-Zustand
- [x] Owner-/Member-Kontext
- [x] finale Policy-Entscheidung
- [x] konfigurierbare Denied-/Entry-/Exit-Messages
- [x] Regionname wird als unparsed User-Data in MiniMessage eingesetzt

Das System besitzt damit ausreichend Diagnosemöglichkeiten, um Overlap-, Priority- und Policy-Probleme ohne Quellcodeänderung zu untersuchen.

---

## 8. Tests

### Geometry

- [x] Dreieck
- [x] Rechteck
- [x] konkave Polygone
- [x] innen
- [x] außen
- [x] Kante
- [x] Ecke
- [x] doppelte Punkte
- [x] Nullfläche
- [x] Selbstüberschneidung

### Region / Policy

- [x] World
- [x] Y
- [x] Priority
- [x] Owner
- [x] Member
- [x] Flags
- [x] Overlap
- [x] Fallthrough
- [x] Default
- [x] Bypass

### Transition

- [x] außerhalb → innerhalb
- [x] innerhalb → außerhalb
- [x] Region A → Region B
- [x] Overlap
- [x] Y-/Boundary-Verhalten
- [x] World-Wechsel
- [x] Teleport-Transition-Kette

### Storage

- [x] Save/Load
- [x] Legacy-Format
- [x] Backup
- [x] Future-Schema-Rejection

### Performance

- [x] 1000-Region-Spatial-Index-Test
- [x] Chunk-Boundary-Verhalten

---

## 9. Build- und CI-Abnahme

### Build

- [x] Java 25 Toolchain
- [x] Gradle 9.2.0
- [x] Paper Dev Bundle 26.2.build.121-stable
- [x] `clean check build`
- [x] JUnit Platform
- [x] Source-Boundary-Verifikation
- [x] ShadowJar
- [x] Gson-Relocation
- [x] Plugin-Artefakt-Verifikation
- [x] Slim-JAR wird separat erzeugt

### API-Boundaries

Verboten und automatisiert geprüft:

- [x] `ChatColor`
- [x] Legacy-NMS
- [x] CraftBukkit
- [x] statische Live-Server-Referenzen
- [x] unrelocated Gson-Klassen im Shadow-Artefakt

### Aktueller CI-Nachweis

- Workflow: **Build**
- Branch: **main**
- Run: **75**
- Run ID: `35683947468`
- Commit: `dea3b1e63860d4c1f07c65ca3f3f1351772fed65`
- Ergebnis: **success**
- Verifiziert: Clean Build, Tests, Source API Boundaries und Plugin Artifact Checks

Der Audit referenziert damit den tatsächlich neuesten erfolgreichen CI-Stand und nicht mehr den vorherigen Zwischenstand.

---

## 10. Dokumentations-Abnahme

- [x] README entspricht dem implementierten Funktionsumfang
- [x] Plattform dokumentiert
- [x] Regionmodell dokumentiert
- [x] Policy-Regeln dokumentiert
- [x] Flags dokumentiert
- [x] Commands dokumentiert
- [x] Permissions dokumentiert
- [x] Storage dokumentiert
- [x] Configuration dokumentiert
- [x] Diagnostics dokumentiert
- [x] Tests dokumentiert
- [x] Build dokumentiert
- [x] bewusst nicht enthaltene Funktionen dokumentiert

---

## 11. Bewusst nicht Bestandteil der Release-Basis

Die folgenden Punkte sind **keine offenen Fehler**:

- [~] vollständiger WorldGuard-Klon
- [~] Webinterface
- [~] Economy-/Claim-System
- [~] zusätzliche NMS-Komplexität
- [~] große unbenötigte Flag-Sammlung
- [~] künstliche Framework-Schichten
- [~] zusätzlicher Query-Cache
- [~] asynchrones Persistenzsystem
- [~] zusätzliche Performance-Metriken
- [~] Editor-Visualisierung
- [~] zusätzliche Dialog-Oberfläche; der vollständige Command-Editor deckt den aktuellen Zielumfang ab

Diese Punkte können später unabhängig als Erweiterungen geplant werden.

---

## 12. Bekannte fachliche Semantik

### Owner / Member

Owner- und Member-Zugriff ist eine **explizite Regionberechtigung** und kein globaler Bypass. Die Entscheidung wird zentral in `RegionPolicyService` getroffen.

Bei mehreren überlappenden Regionen bleibt die definierte Policy-Reihenfolge maßgeblich. Änderungen an dieser Semantik dürfen nur bewusst als Policy-Änderung erfolgen und sind nicht als technischer Fehler des aktuellen Release-Stands zu behandeln.

### Explosionen

Explosionen werden über die betroffenen Blocklisten gegen die `EXPLOSION`-Policy geprüft.

### Fire

Fire-Ignition und Fire-Spread verwenden die aktuellen Paper-26.2-Events und werden gemeinsam über `FIRE_SPREAD` policy-gesteuert.

---

## 13. Release-Fertigkriterium

Alle folgenden Bedingungen sind erfüllt:

- [x] definierter Muss-Funktionsumfang vollständig
- [x] keine bekannte kritische Protection-Lücke innerhalb des definierten Umfangs
- [x] Geometry automatisiert getestet
- [x] Overlap/Priority automatisiert getestet
- [x] Owner/Member automatisiert getestet
- [x] Movement/Transition automatisiert getestet
- [x] Storage automatisiert getestet
- [x] Spatial Index automatisiert getestet
- [x] Build erfolgreich
- [x] CI erfolgreich
- [x] Legacy-API-Verifikation erfolgreich
- [x] Shadow-Verifikation erfolgreich
- [x] Plugin-Artefakt verifiziert
- [x] README entspricht dem Code
- [x] Audit entspricht dem aktuellen CI-Stand
- [x] keine offenen Muss-Punkte

---

## 14. Schlussabnahme

### 🟢 VERÖFFENTLICHUNGSFÄHIG

**Pixel-Region kann auf Basis des aktuell geprüften Repository-Stands als fertige Release-Basis veröffentlicht werden.**

Der definierte Core ist abgeschlossen. Die Architektur ist für weitere Features vorbereitet, ohne dass dafür die bestehende Protection-, Query-, Storage- oder Editor-Basis neu aufgebaut werden muss.

**Release-Basis:** `main`  
**Letzter verifizierter Commit:** `dea3b1e63860d4c1f07c65ca3f3f1351772fed65`  
**CI:** erfolgreich  
**Tests:** erfolgreich  
**Build:** erfolgreich  
**Offene Muss-Punkte:** **0**

Ab diesem Punkt sind neue Funktionen als Erweiterungen auf einer abgenommenen Basis zu behandeln und nicht als notwendige Fertigstellung des aktuellen Core-Systems.
