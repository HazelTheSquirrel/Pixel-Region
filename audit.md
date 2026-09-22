# Pixel-Region — Audit & verbindlicher Buildplan

## Zweck

Dieses Dokument ist die verbindliche technische Arbeitsgrundlage für Pixel-Region.

Pixel-Region ist ein eigenständiges polygonbasiertes Regionsystem für Paper 26.2. PixelRPG wurde ausschließlich als Architekturvorlage geprüft; es besteht keine Laufzeit-, Build- oder Code-Abhängigkeit.

Statusregeln:
- [ ] offen
- [-] in Arbeit
- [x] implementiert und erfolgreich verifiziert
- [~] bewusst nicht Bestandteil des Zielumfangs
- Ein Punkt darf erst mit [x] markiert werden, wenn Implementierung und relevanter Build-/Test-/Funktionsnachweis erfolgreich sind.

## 1. Verbindliche Plattform

- [x] Paper 26.2
- [x] Dev Bundle 26.2.build.121-stable
- [x] Java 25
- [x] Mojang-Mappings
- [x] paper-plugin.yml
- [x] aktuelle Paper-26.x-APIs
- [x] Adventure Components
- [x] Gradle + ShadowJar
- [x] Repository HazelTheSquirrel/Pixel-Region
- [x] Zielbranch main

## 2. Initiales Audit

### Core

- [x] Paper-Plugin-Grundstruktur
- [x] X/Z-Polygon-Geometrie
- [x] Polygonvalidierung
- [x] Prüfung auf doppelte benachbarte Punkte
- [x] Nullflächenprüfung
- [x] Selbstüberschneidungsprüfung
- [x] Bounding Box
- [x] World UUID
- [x] Min-Y / Max-Y
- [x] Priority
- [x] Owner
- [x] Members
- [x] Region Flags
- [x] JSON-Persistenz
- [x] Region-Suche
- [x] Priority-basierte Flag-Auflösung
- [x] Block Break / Place
- [x] Interact
- [x] PvP
- [x] Mob-/Entity-Damage
- [x] Explosion
- [x] Fluid
- [x] Fire
- [x] Mob Spawn
- [x] Container Use
- [x] Entry/Exit
- [x] Editor-Session
- [x] Edit bestehender Regionen
- [x] Adventure Components / MiniMessage
- [x] aktuelle Paper BasicCommand API
- [x] Build- und Shadow-Verifikation

### Architektur

- [x] RegionGeometry wird pro Region gecacht und nicht pro Query neu erzeugt.
- [x] RegionPolicyService trennt Policy-Auswertung von Regiondaten.
- [x] RegionManager.applicableRegions(...) liefert die vollständige priorisierte Regionsmenge.
- [x] Owner/Member-Entscheidungen liegen zentral im RegionPolicyService.
- [x] Query arbeitet über World+Chunk-Spatial-Index und prüft danach Y, Bounds und Polygon.
- [x] World+Chunk-Index wird bei Load/Add/Remove/Replace korrekt aktualisiert.
- [x] Movement/Transition vergleicht vollständige Region-Mengen statt nur der Top-Region.
- [x] ENTRY/EXIT werden für konkrete Übergangsmengen ausgewertet.
- [x] Teleports laufen über die gemeinsame aktuelle PlayerMoveEvent-Kette.
- [x] Config-Defaults werden als zentrale Fallback-Policy geladen.
- [x] Config-Messages werden über RegionMessages zentral verwendet.
- [x] RegionStorage / JsonRegionStorage abstrahieren Persistenz.
- [x] Dirty-State und persistente Mutationen sind implementiert.
- [x] Backup, temporäre Datei und atomarer Replace-Fallback sind implementiert.

## 3. Zielumfang

### Muss

- [x] Polygonregionen
- [x] Weltbindung
- [x] vertikaler Bereich
- [x] Overlapping Regions
- [x] Priority
- [x] Flags
- [x] Owner und Members
- [x] zentrale Policy-Auswertung
- [x] Schutz der wichtigsten Vanilla-Interaktionen
- [x] Entry/Exit
- [x] JSON Storage
- [x] Region bearbeiten
- [x] robuste Polygonvalidierung
- [x] performante Queries
- [x] saubere Commands
- [x] Konfiguration
- [x] Tests
- [x] sichere Persistenz
- [x] reproduzierbarer Build und CI

### Bewusst kein Ziel

- [~] vollständiger WorldGuard-Klon
- [~] Webinterface
- [~] Economy-/Claim-System
- [~] unnötige NMS-Nutzung
- [~] riesige Flag-Sammlung ohne konkreten Bedarf
- [~] künstliche Framework-Komplexität
- [~] Query-Cache zusätzlich zum Chunk-Index; der aktuelle Spatial-Index ist ausreichend und wird automatisiert getestet.
- [~] asynchrone Persistenz; Regionänderungen werden als kleine immutable/serialisierte Snapshots synchron und sicher geschrieben.
- [~] zusätzliche Performance-Metriken ohne konkreten Diagnosebedarf.
- [~] Editor-Visualisierung; der Kerneditor ist vollständig ohne zusätzliche Live-Rendering-Infrastruktur.

## 4. Zielarchitektur

Paper Event → Protection Adapter → Region Query → Applicable Regions → Policy Evaluator → ALLOW/DENY

Tatsächliche Verantwortlichkeiten:
- Region: Regiondaten, Owner/Members und explizite Flags.
- RegionGeometry: vorbereitete Polygongeometrie und Bounds.
- RegionValidator: zentrale Datenvalidierung.
- RegionManager: Speicherung im Speicher, Spatial Index und Applicable-Region-Abfragen.
- RegionQuery: unveränderlicher Query-Wert.
- RegionPolicyService: zentrale Action-/Flag-Entscheidung.
- RegionTransitionService: Entry/Exit/Teleport-Behandlung.
- RegionStorage / JsonRegionStorage: Persistenz.
- RegionMessages: konfigurierte Adventure-/MiniMessage-Ausgabe.
- Listener: dünne Protection-Adapter, die an Policy/Transition delegieren.

## 5. Verbindlicher Buildplan

### Phase 1 — Core stabilisieren

- [x] Geometry Cache: RegionGeometry und Bounds wiederverwenden.
- [x] Geometry wird bei Polygonänderungen durch validierten Region-Ersatz korrekt erneuert.
- [x] Tests für inside/outside/boundary/invalid.
- [x] RegionValidator einführen.
- [x] UUID, Name, World, Y, Polygon, Priority, Flags, Owner und Members validieren.
- [x] Name auf sinnvolles Format und case-insensitive Eindeutigkeit bringen.

Abschluss: CI-Build und automatisierte Geometry-/Validator-/Region-Tests erfolgreich.

### Phase 2 — Query und Policy

- [x] ApplicableRegions als zentrale Overlap-Abfrage.
- [x] World, Y, Bounds und Polygon in der Query.
- [x] Ergebnis nach Priority sortiert.
- [x] RegionManager ist nicht die primäre Policy-Schicht; Listener delegieren an RegionPolicyService.
- [x] RegionPolicyService einführen.
- [x] höchste explizite Flag-Entscheidung gewinnt.
- [x] unset fällt auf niedrigere Regionen.
- [x] kein Treffer fällt auf konfigurierten Default zurück.
- [x] Owner/Member-Regel explizit und zentral definiert.

Abschluss: Single-, Overlap-, Priority-, Fallback-, Owner- und Member-Tests erfolgreich.

### Phase 3 — Performance

- [x] Spatial-/Chunk-Index nach World + Chunk.
- [x] Add, Remove, Edit und Reload aktualisieren den Index.
- [x] Regionen über Chunkgrenzen korrekt behandeln.
- [x] erst Kandidaten, dann Bounds, dann Polygon prüfen.
- [~] Query-Cache nicht zusätzlich erforderlich; der Chunk-Index reduziert die Kandidatenmenge bereits.
- [x] relevante Indexänderungen werden bei Mutation/Reload durchgeführt.

Abschluss: korrekte Query-Ergebnisse und Many-Region-Test erfolgreich.

### Phase 4 — Protection

- [x] Alle Listener delegieren an Query/Policy.
- [x] keine duplizierte Priority-/Member-Logik in Listenern.
- [x] MOB_SPAWN implementiert.
- [x] CONTAINER_USE implementiert.
- [x] Fire-Ignition und Fire-Spread werden durch getrennte aktuelle Paper-26.2-Events geprüft und gemeinsam über FIRE_SPREAD policy-gesteuert.
- [x] Explosion-Semantik ist dokumentiert: betroffene Blockliste wird gegen EXPLOSION gefiltert.
- [x] Protection-Flags sind zentral konfigurierbar.

Abschluss: Build, API-Verifikation und automatisierte Policy-Tests erfolgreich.

### Phase 5 — Movement

- [x] RegionTransitionService.
- [x] entered/exited/retained aus vollständigen Regionsmengen berechnen.
- [x] ENTRY/EXIT nicht nur über Top-Region bestimmen.
- [x] normale Teleports werden über PlayerMoveEvent/PlayerTeleportEvent berücksichtigt.
- [x] World-Wechsel werden durch World UUID in der Query berücksichtigt.
- [x] gleiche Region vor/nach Bewegung erzeugt keinen falschen Transition-Event.
- [x] Transition-Set-Berechnung ist automatisiert getestet.

Abschluss: Transition-, Overlap- und Boundary-Tests erfolgreich.

### Phase 6 — Editor

- [x] bestehendes create/point/finish/cancel erhalten.
- [x] edit bestehender Regionen.
- [x] Punkte hinzufügen, entfernen und ändern.
- [x] Änderungen werden erst nach erfolgreicher Validierung übernommen.
- [x] Priority bearbeiten.
- [x] Min-Y/Max-Y bearbeiten.
- [x] Flags bearbeiten.
- [x] Members bearbeiten.
- [~] Visualisierung nicht Bestandteil des Zielumfangs.
- [x] aktuelle Paper-26.2-Dialog-API wurde geprüft; für den aktuellen vollständigen Command-Editor ist keine zusätzliche Dialog-Implementierung erforderlich.

Abschluss: vollständiger Create/Edit-Flow ist implementiert; Mutationen werden sofort persistiert.

### Phase 7 — Storage

- [x] RegionStorage-Abstraktion.
- [x] JsonRegionStorage als konkrete Implementierung.
- [x] schemaVersion im Dateiformat.
- [x] unbekannte zukünftige Versionen werden abgelehnt und nicht überschrieben.
- [x] Backup-Datei.
- [x] sichere temporäre Speicherung und Replace-Strategie.
- [x] Dirty-State.
- [x] Save bei wichtigen Mutationen und Shutdown.
- [~] Async I/O nicht erforderlich; kleine serialisierte Region-Snapshots werden sicher synchron gespeichert.
- [x] Legacy-Array-Format bleibt lesbar.
- [x] Backup-Recovery bei defekter Primärdatei.
- [x] Storage-Tests für Save/Load/Legacy/Backup/Future-Schema.

Abschluss: automatisierte Storage-Tests und CI-Build erfolgreich.

### Phase 8 — Permissions

- [x] pixelregion.command
- [x] pixelregion.command.create
- [x] pixelregion.command.list
- [x] pixelregion.command.info
- [x] pixelregion.command.edit
- [x] pixelregion.command.delete
- [x] pixelregion.command.flag
- [x] pixelregion.command.member
- [x] pixelregion.command.reload
- [x] pixelregion.command.save
- [x] pixelregion.command.debug
- [x] pixelregion.admin
- [x] pixelregion.bypass

Abschluss: Permission-Katalog und Checks sind im Paper-Plugin-Descriptor und Command-Code konsistent.

### Phase 9 — Diagnostics

- [x] /pixelregion debug.
- [x] Location anzeigen.
- [x] passende Regionen anzeigen.
- [x] Priority anzeigen.
- [x] explizites Flag anzeigen.
- [x] Owner/Member-Kontext anzeigen.
- [x] finale Policy anzeigen.
- [~] Performance-Metriken nur bei echtem Bedarf.

Abschluss: Overlap-/Policy-Fälle sind ohne Codeänderung diagnostizierbar.

### Phase 10 — Tests

Geometry:
- [x] Dreieck
- [x] Rechteck
- [x] konkav
- [x] innen
- [x] außen
- [x] Kante
- [x] Ecke
- [x] doppelte Punkte
- [x] Nullfläche
- [x] Selbstüberschneidung

Region/Policy:
- [x] World
- [x] Y
- [x] Priority
- [x] Owner
- [x] Member
- [x] Flags
- [x] Overlap
- [x] Fallback
- [x] Default
- [x] Bypass

Transition:
- [x] außerhalb → innerhalb
- [x] innerhalb → außerhalb
- [x] Region A → Region B
- [x] Overlap
- [x] Y-/Boundary-Verhalten
- [x] World-Wechsel über World UUID
- [x] Teleport-Transition-Kette

Storage:
- [x] Save/Load
- [x] Legacy-Format
- [x] Backup
- [x] Future-Schema-Rejection

Performance:
- [x] 1000-Region-Spatial-Index-Test

Abschluss: automatisierte Tests erfolgreich.

### Phase 11 — Build und Repository

- [x] alte de.pixelrpg-Buildreste entfernt.
- [x] eigener Namespace für Group und Relocation: de.pixelregion.
- [x] Verification-Tasknamen bereinigt.
- [x] Workflow auf main ausgerichtet.
- [x] Java 25.
- [x] Gradle 9.2.0 CI.
- [x] clean check build.
- [x] Legacy-API-Prüfungen.
- [x] Artifact-Prüfung.
- [x] Shadow-Relocation-Prüfung.
- [x] nur tatsächlich benötigte Gson-Laufzeitdependency bleibt.
- [x] ungenutzte HikariCP/MySQL-Abhängigkeiten entfernt.
- [x] settings.gradle Projektname Pixel-Region.
- [x] CI-Build erfolgreich verifiziert.

Verifizierter CI-Stand:
- Commit: d63d60759338644e5e71577c888cef7cd22961b4
- Workflow: Build
- Result: success

### Phase 12 — Dokumentation

- [x] README auf finalen Funktionsstand gebracht.
- [x] Installation / Build.
- [x] Commands.
- [x] Permissions.
- [x] Flags.
- [x] Priority und Overlap.
- [x] Storage.
- [x] Config.
- [x] Policy-Regeln.
- [x] Editor.
- [x] bekannte Einschränkungen / bewusst ausgelassene optionale Funktionen.
- [x] Development/Build.
- [x] README entspricht dem implementierten Stand.

## 6. Verbindliche Policy-Regeln

Eine Region passt nur bei identischer World UUID, gültigem Y-Bereich, Treffer in der Bounding Box und Treffer im Polygon.

Mehrere Regionen dürfen gleichzeitig gelten.

Höhere Priority wird zuerst ausgewertet.

Eine nicht gesetzte Flag-Entscheidung fällt auf die nächste passende Region zurück.

Wenn keine Region eine explizite Entscheidung liefert, gilt der konfigurierte Default.

Owner/Member erhalten nur die ausdrücklich definierte Region-Berechtigung.

Ein globaler Bypass ist eine separate Permission/Policy-Regel.

## 7. Arbeitsregeln

- [x] Keine unnötigen Refactorings.
- [x] Bestehende funktionierende Funktionen erhalten.
- [x] Keine erfundenen Paper-APIs.
- [x] aktuelle Paper-26.2-API bei Unsicherheit geprüft.
- [x] keine alten 1.21.x-APIs oder Workarounds.
- [x] keine CraftBukkit-/Legacy-NMS-Abhängigkeit.
- [x] keine statischen Live-Server-Objekte.
- [x] Phasen wurden erst nach Verifikation als erledigt markiert.

## 8. Projektstatus

### 🟢 FERTIG

Pixel-Region erfüllt den definierten Zielumfang.

Build: erfolgreich  
Tests: erfolgreich  
CI: erfolgreich  
Protection-Core: abgeschlossen  
Storage: abgeschlossen  
Editor: abgeschlossen  
Dokumentation: abgeschlossen  
Offene Muss-Punkte: 0

## 9. Fertigkriterium

- [x] alle Muss-Funktionen implementiert.
- [x] keine bekannte kritische Protection-Lücke im definierten Zielumfang.
- [x] Overlap/Priority automatisiert getestet.
- [x] Geometry automatisiert getestet.
- [x] Storage automatisiert getestet.
- [x] Movement/Transition automatisiert getestet.
- [x] Build erfolgreich.
- [x] CI erfolgreich.
- [x] Legacy-API-Verifikation erfolgreich.
- [x] Shadow-Verifikation erfolgreich.
- [x] README beschreibt den tatsächlichen Stand.
- [x] offene Muss-Punkte 0.

## 10. Fortschrittsprotokoll

- PixelRPG-Referenz: Region, RegionManager, RegionPolicyService, RegionTransitionService und Repository wurden als Architekturvorlage geprüft. Nur für Pixel-Region benötigte Bestandteile wurden übernommen; Pixel-Region bleibt vollständig eigenständig.
- Initial Audit: [x]
- Phase 1 Core: [x]
- Phase 2 Query/Policy: [x]
- Phase 3 Performance: [x]
- Phase 4 Protection: [x]
- Phase 5 Movement: [x]
- Phase 6 Editor: [x]
- Phase 7 Storage: [x]
- Phase 8 Permissions: [x]
- Phase 9 Diagnostics: [x]
- Phase 10 Tests: [x]
- Phase 11 Build/Repository: [x]
- Phase 12 Documentation: [x]
