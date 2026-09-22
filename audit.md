# Pixel-Region — Audit & verbindlicher Buildplan

## Zweck
Dieses Dokument ist die verbindliche technische Arbeitsgrundlage für die weitere Entwicklung von Pixel-Region.
Pixel-Region ist ein eigenständiges, kleines polygonbasiertes Regionsystem für Paper 26.2. WorldGuard dient ausschließlich als funktionale Inspiration. Ziel ist kein vollständiger WorldGuard-Klon, sondern ein sauberer, zuverlässiger und wartbarer Kern mit den für ein kleines Plugin sinnvollen Funktionen.

Statusregeln:
- [ ] offen
- [-] in Arbeit
- [x] implementiert und erfolgreich verifiziert
- [~] bewusst nicht Bestandteil des Zielumfangs
- Ein Punkt darf erst mit [x] markiert werden, wenn Implementierung und relevanter Build-/Test-/Funktionsnachweis erfolgreich sind.

## 1. Verbindliche Plattform
- Paper 26.2
- Dev Bundle 26.2.build.121-stable
- Java 25
- Mojang-Mappings
- paper-plugin.yml
- aktuelle Paper-26.x-APIs
- Adventure Components
- Gradle + ShadowJar
- Repository: HazelTheSquirrel/Pixel-Region
- Zielbranch: main

## 2. Initiales Audit
### Bereits vorhanden
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
- [x] Entry/Exit-Grundlogik
- [x] Editor-Session
- [x] grundlegende Commands
- [x] Adventure Components
- [x] aktuelle Paper BasicCommand API
- [x] Build- und Shadow-Verifikation

### Kritische bzw. relevante offene Punkte
- [ ] Geometry wird bei Queries unnötig wiederholt erzeugt.
- [ ] RegionManager verbindet Verwaltung, Query und Policy.
- [ ] Es gibt keine ApplicableRegions-Abstraktion.
- [ ] Owner/Member-Policy ist nicht sauber von Regiondaten getrennt.
- [ ] findAll prüft grundsätzlich alle Regionen.
- [ ] Kein Spatial-/Chunk-Index.
- [ ] Movement betrachtet primär die jeweils höchste Region statt vollständiger Regionsmengen.
- [ ] Teleport-/Portal-Transitions sind nicht zentralisiert.
- [ ] MOB_SPAWN ist vorhanden, aber noch nicht vollständig geschützt.
- [ ] Config-Defaults sind noch nicht die tatsächliche Fallback-Policy.
- [ ] Config-Messages sind noch nicht vollständig zentral verwendet.
- [ ] Container-Schutz fehlt.
- [ ] Tests für Geometry und Policy fehlen bzw. sind unzureichend.
- [ ] Storage-Schema ist nicht versioniert.
- [ ] Backup-/Recovery-Konzept fehlt.
- [ ] Name-Validation ist zu schwach.
- [ ] Permissions sind zu grob.
- [ ] Debug-/Diagnosefunktion fehlt.
- [ ] Build enthält noch alte de.pixelrpg-Namensreste.
- [ ] CI enthält nicht mehr benötigte Branchfilter.

## 3. Zielumfang
### Muss
- [ ] Polygonregionen
- [ ] Weltbindung
- [ ] vertikaler Bereich
- [ ] Overlapping Regions
- [ ] Priority
- [ ] Flags
- [ ] Owner und Members
- [ ] zentrale Policy-Auswertung
- [ ] Schutz der wichtigsten Vanilla-Interaktionen
- [ ] Entry/Exit
- [ ] JSON Storage
- [ ] Region bearbeiten
- [ ] robuste Polygonvalidierung
- [ ] performante Queries
- [ ] saubere Commands
- [ ] Konfiguration
- [ ] Tests
- [ ] sichere Persistenz
- [ ] reproduzierbarer Build und CI

### Bewusst kein Ziel
- [~] vollständiger WorldGuard-Klon
- [~] Webinterface
- [~] Economy-/Claim-System
- [~] unnötige NMS-Nutzung
- [~] riesige Flag-Sammlung ohne konkreten Bedarf
- [~] künstliche Framework-Komplexität

## 4. Zielarchitektur
Paper Event → Protection Adapter → Region Query → Applicable Regions → Policy Evaluator → ALLOW/DENY

Empfohlene Verantwortlichkeiten:
- Region: Daten und unveränderliche Regionbeschreibung.
- RegionGeometry: vorbereitete Polygongeometrie und Bounds.
- RegionValidator: zentrale Datenvalidierung.
- RegionQuery: Finden passender Regionen.
- ApplicableRegions: vollständige priorisierte Regionsmenge.
- RegionPolicyEvaluator: Entscheidung über Action/Flag.
- RegionTransitionService: Entry/Exit/Teleport.
- RegionIndex: räumliche Kandidatensuche.
- RegionStorage / JsonRegionStorage: Persistenz.
- MessageService: Konfiguration und Adventure Components.

## 5. Verbindlicher Buildplan

### Phase 1 — Core stabilisieren
- [ ] Geometry Cache: RegionGeometry und Bounds wiederverwenden.
- [ ] Geometry bei Polygonänderungen korrekt erneuern.
- [ ] Tests für inside/outside/boundary/invalid.
- [ ] RegionValidator einführen.
- [ ] UUID, Name, World, Y, Polygon, Priority, Flags, Owner und Members validieren.
- [ ] Name auf sinnvolles Format und case-insensitive Eindeutigkeit bringen.
Abschluss: Build und Geometry-/Validator-Tests erfolgreich.

### Phase 2 — Query und Policy
- [ ] ApplicableRegions als zentrale Overlap-Abfrage.
- [ ] World, Y, Bounds und Polygon in der Query.
- [ ] Ergebnis nach Priority sortiert.
- [ ] RegionManager nicht mehr als zentrale Policy-Schicht verwenden.
- [ ] RegionPolicyEvaluator einführen.
- [ ] höchste explizite Flag-Entscheidung gewinnt.
- [ ] unset fällt auf niedrigere Regionen.
- [ ] kein Treffer fällt auf konfigurierten Default zurück.
- [ ] Owner/Member-Regel explizit und zentral definieren.
Abschluss: Single-, Overlap-, Priority-, Fallback-, Owner- und Member-Tests erfolgreich.

### Phase 3 — Performance
- [ ] Spatial-/Chunk-Index nach World + Chunk.
- [ ] Add, Remove, Edit und Reload aktualisieren den Index.
- [ ] Regionen über Chunkgrenzen korrekt behandeln.
- [ ] erst Kandidaten, dann Bounds, dann Polygon prüfen.
- [ ] Query-Cache nur einführen, wenn nach dem Index noch sinnvoll.
- [ ] Cache bei jeder relevanten Mutation invalidieren.
Abschluss: korrekte Query-Ergebnisse und Performance-Test mit vielen Regionen.

### Phase 4 — Protection
- [ ] Alle bestehenden Listener auf Query/Policy umstellen.
- [ ] keine duplizierte Priority-/Member-Logik in Listenern.
- [ ] MOB_SPAWN implementieren oder bewusst aus dem Zielumfang entfernen.
- [ ] CONTAINER_USE als kleine sinnvolle Erweiterung.
- [ ] Fire semantisch zwischen Ignition und Spread trennen, falls benötigt.
- [ ] Explosion-Semantik dokumentieren und testen.
Abschluss: alle tatsächlich angebotenen Flags funktionieren vollständig.

### Phase 5 — Movement
- [ ] RegionTransitionService.
- [ ] entered/exited/retained aus vollständigen Regionsmengen berechnen.
- [ ] ENTRY/EXIT nicht nur über Top-Region bestimmen.
- [ ] normale Teleports berücksichtigen.
- [ ] World-Wechsel berücksichtigen.
- [ ] gleiche Region vor/nach Teleport darf keinen falschen Transition-Event auslösen.
Abschluss: Movement-, Overlap-, Y- und Teleport-Tests erfolgreich.

### Phase 6 — Editor
- [ ] bestehendes create/point/finish/cancel erhalten.
- [ ] edit bestehender Regionen.
- [ ] Punkte hinzufügen, entfernen und ändern.
- [ ] Änderungen erst nach erfolgreicher Validierung übernehmen.
- [ ] Priority bearbeiten.
- [ ] Min-Y/Max-Y bearbeiten.
- [ ] Flags bearbeiten.
- [ ] Members bearbeiten.
- [ ] optionale Visualisierung nur für aktive Editoren.
- [ ] aktuelle Paper-26.2-Dialog-API nur nach tatsächlicher API-Prüfung verwenden.
Abschluss: vollständiger Create/Edit-Flow ohne Datenverlust.

### Phase 7 — Storage
- [ ] RegionStorage-Abstraktion.
- [ ] JsonRegionStorage.
- [ ] schemaVersion im Dateiformat.
- [ ] unbekannte Versionen sauber behandeln.
- [ ] Backup-Datei.
- [ ] sichere temporäre Speicherung und Replace-Strategie.
- [ ] Dirty-State.
- [ ] Save bei wichtigen Mutationen und Shutdown.
- [ ] Async I/O nur mit immutable/serialisierten Daten.
Abschluss: Load/Save/Reload/Restart und Fehlerfälle getestet.

### Phase 8 — Permissions
- [ ] pixelregion.command
- [ ] pixelregion.command.create
- [ ] pixelregion.command.edit
- [ ] pixelregion.command.delete
- [ ] pixelregion.command.flag
- [ ] pixelregion.command.member
- [ ] pixelregion.command.reload
- [ ] pixelregion.command.save
- [ ] pixelregion.bypass
Abschluss: Command- und Bypass-Rechte getestet.

### Phase 9 — Diagnostics
- [ ] /pixelregion debug.
- [ ] Location anzeigen.
- [ ] passende Regionen anzeigen.
- [ ] Priority anzeigen.
- [ ] explizites Flag anzeigen.
- [ ] Owner/Member-Kontext anzeigen.
- [ ] finale Policy anzeigen.
- [ ] Performance-Metriken nur bei echtem Bedarf.
Abschluss: komplizierte Overlap-/Policy-Fälle ohne Codeänderung diagnostizierbar.

### Phase 10 — Tests
Geometry:
- [ ] Dreieck
- [ ] Rechteck
- [ ] konkav
- [ ] innen
- [ ] außen
- [ ] Kante
- [ ] Ecke
- [ ] doppelte Punkte
- [ ] Nullfläche
- [ ] Selbstüberschneidung
Region/Policy:
- [ ] World
- [ ] Y
- [ ] Priority
- [ ] Owner
- [ ] Member
- [ ] Flags
- [ ] Overlap
- [ ] Fallback
- [ ] Default
Transition:
- [ ] außerhalb → innerhalb
- [ ] innerhalb → außerhalb
- [ ] Region A → Region B
- [ ] Overlap
- [ ] Y-Grenze
- [ ] World-Wechsel
- [ ] Teleport
Abschluss: automatisierte Tests erfolgreich.

### Phase 11 — Build und Repository
- [ ] alte de.pixelrpg-Buildreste entfernen.
- [ ] eigenen Namespace für Group und Relocations verwenden.
- [ ] Verification-Tasknamen bereinigen.
- [ ] Workflow auf den tatsächlich verwendeten Branch main ausrichten.
- [ ] Java 25 prüfen.
- [ ] Gradle 9.2.0 prüfen.
- [ ] clean build.
- [ ] Legacy-API-Prüfungen.
- [ ] Artifact-Prüfung.
- [ ] Shadow-Relocation-Prüfung.
- [ ] JDBC-Prüfung nur solange MySQL Teil des finalen Builds bleibt.
- [ ] Dependencies auf tatsächlichen Bedarf prüfen.
Abschluss: lokale Verifikation und CI erfolgreich.

### Phase 12 — Dokumentation
- [ ] README auf finalen Funktionsstand bringen.
- [ ] Installation.
- [ ] Commands.
- [ ] Permissions.
- [ ] Flags.
- [ ] Priority und Overlap.
- [ ] Storage.
- [ ] Config.
- [ ] Policy-Regeln.
- [ ] Editor.
- [ ] bekannte Einschränkungen.
- [ ] Development/Build.
Abschluss: README entspricht exakt der implementierten Runtime.

## 6. Verbindliche Policy-Regeln
Eine Region passt nur bei identischer World UUID, gültigem Y-Bereich, Treffer in der Bounding Box und Treffer im Polygon.
Mehrere Regionen dürfen gleichzeitig gelten.
Höhere Priority wird zuerst ausgewertet.
Eine nicht gesetzte Flag-Entscheidung fällt auf die nächste passende Region zurück.
Wenn keine Region eine explizite Entscheidung liefert, gilt der konfigurierte Default.
Owner/Member erhalten nur die ausdrücklich definierte Region-Berechtigung.
Ein globaler Bypass ist eine separate Permission/Policy-Regel.

## 7. Arbeitsregeln
- Keine unnötigen Refactorings.
- Bestehende funktionierende Funktionen erhalten.
- Keine erfundenen Paper-APIs.
- Vor Änderungen aktuelle Paper-26.2-API prüfen, wenn Unsicherheit besteht.
- Keine alten 1.21.x-APIs oder Workarounds.
- Keine CraftBukkit-/Legacy-NMS-Abhängigkeit.
- Keine statischen Live-Server-Objekte.
- Jede Phase erst nach Verifikation als erledigt markieren.
- Bei einer Regression wird der entsprechende Punkt wieder auf offen gesetzt.

## 8. Projektstatus
### 🔴 IN ENTWICKLUNG
Initiales Audit abgeschlossen. MVP vorhanden, Zielstand noch nicht vollständig erreicht.

FERTIG: NEIN

## 9. Fertigkriterium
Das Projekt wird erst auf FERTIG gesetzt, wenn:
- [ ] alle Muss-Funktionen implementiert sind.
- [ ] keine bekannte kritische Protection-Lücke besteht.
- [ ] Overlap/Priority korrekt getestet sind.
- [ ] Geometry getestet ist.
- [ ] Storage getestet ist.
- [ ] Movement/Teleport getestet ist.
- [ ] Build erfolgreich ist.
- [ ] CI erfolgreich ist.
- [ ] Legacy-API-Verifikation erfolgreich ist.
- [ ] Shadow-Verifikation erfolgreich ist.
- [ ] README den tatsächlichen Stand beschreibt.
- [ ] offene Muss-Punkte 0 sind.

Erst dann wird dieser Abschnitt geändert zu:

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

## 10. Fortschrittsprotokoll
- Initial Audit: [x]
- Phase 1 Core: [ ]
- Phase 2 Query/Policy: [ ]
- Phase 3 Performance: [ ]
- Phase 4 Protection: [ ]
- Phase 5 Movement: [ ]
- Phase 6 Editor: [ ]
- Phase 7 Storage: [ ]
- Phase 8 Permissions: [ ]
- Phase 9 Diagnostics: [ ]
- Phase 10 Tests: [ ]
- Phase 11 Build/Repository: [ ]
- Phase 12 Documentation: [ ]