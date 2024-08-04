The ```ZIP_URL``` variable in the ```ArchiveDownloader``` and 
```ArchiveDownloaderTest``` classes must be 
implemented with
the correct link before compiling and running the project.


---

### Beschreibung der Anwendung

Diese Java-Anwendung lädt ein ZIP-Archiv von einer angegebenen URL herunter, extrahiert den Inhalt in ein lokales Verzeichnis und verarbeitet XML-Dateien, indem sie in JSON-Dateien konvertiert werden.

#### Hauptarbeitsabläufe der Anwendung:

1. **ZIP-Archiv herunterladen**: Die Anwendung lädt ein ZIP-Archiv von der angegebenen URL herunter und speichert es in einem lokalen Verzeichnis.
2. **Archiv extrahieren**: Die Anwendung entpackt das heruntergeladene ZIP-Archiv in ein angegebenes Verzeichnis.
3. **Ausgabeverzeichnis erstellen**: Falls das Verzeichnis zur Speicherung der JSON-Dateien nicht existiert, wird es erstellt.
4. **XML-Dateien verarbeiten**: Alle XML-Dateien aus dem extrahierten Verzeichnis werden verarbeitet und in JSON-Dateien konvertiert, wobei die Originalnamen beibehalten werden.

### Detaillierte Analyse der Klassen und Methoden

#### Klasse `ArchiveDownloader`

##### main Methode

```java
public static void main(String[] args) {
    try {
        downloadZip(ZIP_URL, DOWNLOAD_DIR);
        unzipFile(DOWNLOAD_DIR + "/HSCTWXX_I_00054_new-schema.zip", EXTRACT_DIR);
        createDirectoryIfNotExists(JSON_OUTPUT_DIR);
        processXmlFiles(EXTRACT_DIR, JSON_OUTPUT_DIR);
    } catch (Exception e) {
        logger.error("An error occurred in main process", e);
    }
}
```

- **Beschreibung**: Hauptmethode, die den Prozess des Herunterladens, Entpackens und Verarbeitens der Dateien startet.
- **Schritte**:
    1. Ruft `downloadZip` auf, um das ZIP-Archiv herunterzuladen.
    2. Ruft `unzipFile` auf, um das heruntergeladene Archiv zu entpacken.
    3. Erstellt das Verzeichnis für JSON-Dateien, falls es nicht existiert, mithilfe von `createDirectoryIfNotExists`.
    4. Verarbeitet die XML-Dateien und konvertiert sie in JSON mithilfe von `processXmlFiles`.

##### downloadZip Methode

```java
private static void downloadZip(String urlString, String downloadDir) throws IOException {
    logger.info("Starting download from URL: {}", urlString);
    URL url = null;

    if(!urlString.equals("")) {
        url = new URL(urlString);
    } else {
        System.out.println("Need correct URL !");
        return;
    }

    BufferedInputStream bis = new BufferedInputStream(url.openStream());
    File downloadDirectory = new File(downloadDir);
    if (!downloadDirectory.exists()) {
        downloadDirectory.mkdirs();
    }
    FileOutputStream fis = new FileOutputStream(downloadDir + "/HSCTWXX_I_00054_new-schema.zip");
    byte[] buffer = new byte[1024];
    int count;
    while ((count = bis.read(buffer, 0, 1024)) != -1) {
        fis.write(buffer, 0, count);
    }
    fis.close();
    bis.close();
    logger.info("Download completed successfully");
}
```

- **Beschreibung**: Lädt ein ZIP-Archiv von einer angegebenen URL herunter und speichert es in ein angegebenes Verzeichnis.
- **Schritte**:
    1. Prüft, ob die URL nicht leer ist.
    2. Erstellt einen Stream, um Daten von der URL herunterzuladen.
    3. Erstellt das Verzeichnis zum Herunterladen, falls es nicht existiert.
    4. Speichert die heruntergeladenen Daten in eine ZIP-Datei.
    5. Schließt die Streams nach Abschluss des Downloads.

##### unzipFile Methode

```java
private static void unzipFile(String zipFilePath, String destDir) throws IOException {
    logger.info("Unzipping file: {}", zipFilePath);
    File destDirectory = new File(destDir);
    if (!destDirectory.exists()) {
        destDirectory.mkdirs();
    }
    ZipFile zipFile = new ZipFile(zipFilePath);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        File entryDestination = new File(destDir, entry.getName());
        if (entry.isDirectory()) {
            entryDestination.mkdirs();
        } else {
            entryDestination.getParentFile().mkdirs();
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
            FileOutputStream fos = new FileOutputStream(entryDestination);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            bis.close();
        }
    }
    zipFile.close();
    logger.info("Unzipping completed successfully");
}
```

- **Beschreibung**: Entpackt ein ZIP-Archiv in ein angegebenes Verzeichnis.
- **Schritte**:
    1. Erstellt das Verzeichnis für die extrahierten Dateien, falls es nicht existiert.
    2. Entpack

t jede Datei und jedes Verzeichnis aus dem ZIP-Archiv in das Zielverzeichnis.
3. Schließt das ZIP-Archiv nach Abschluss des Entpackens.

##### createDirectoryIfNotExists Methode

```java
protected static void createDirectoryIfNotExists(String dirPath) {
    File directory = new File(dirPath);
    if (!directory.exists()) {
        directory.mkdirs();
        logger.info("Created directory: {}", dirPath);
    }
}
```

- **Beschreibung**: Erstellt ein Verzeichnis, falls es nicht existiert.
- **Schritte**:
    1. Prüft, ob das Verzeichnis existiert.
    2. Erstellt das Verzeichnis, falls es nicht existiert.
    3. Protokolliert die Erstellung des Verzeichnisses.

##### processXmlFiles Methode

```java
private static void processXmlFiles(String extractDir, String outputDir) throws Exception {
    logger.info("Processing XML files in directory: {}", extractDir);
    Files.walk(Paths.get(extractDir))
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".xml"))
            .forEach(path -> {
                try {
                    XmlToJsonConverter.convert(path.toString(), outputDir);
                    logger.info("Processed XML file: {}", path);
                } catch (Exception e) {
                    logger.error("Failed to process XML file: {}", path, e);
                }
            });
}
```

- **Beschreibung**: Verarbeitet alle XML-Dateien in einem Verzeichnis und konvertiert sie in JSON.
- **Schritte**:
    1. Durchsucht das Verzeichnis nach allen XML-Dateien.
    2. Konvertiert jede XML-Datei in JSON mithilfe des `XmlToJsonConverter`.
    3. Protokolliert den Erfolg oder Fehler bei der Verarbeitung jeder Datei.

#### Klasse `XmlToJsonConverter`

##### convert Methode

```java
public static void convert(String xmlFilePath, String outputDir) throws Exception {
    logger.info("Converting XML file: {}", xmlFilePath);
    File xmlFile = new File(xmlFilePath);
    String baseName = xmlFile.getName().replaceFirst("[.][^.]+$", ""); // Получение имени файла без расширения

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(xmlFile);
    doc.getDocumentElement().normalize();

    NodeList levelNodes = doc.getElementsByTagName("level");
    Map<String, Level> levels = new HashMap<>();
    for (int i = 0; i < levelNodes.getLength(); i++) {
        Node node = levelNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element levelElement = (Element) node;
            Level level = new Level();
            level.LevelId = levelElement.getElementsByTagName("level_id").item(0).getTextContent();
            level.Length = Integer.parseInt(levelElement.getElementsByTagName("length").item(0).getTextContent());
            NodeList descriptionNodes = levelElement.getElementsByTagName("level_description");
            for (int j = 0; j < descriptionNodes.getLength(); j++) {
                Element descriptionElement = (Element) descriptionNodes.item(j);
                String language = descriptionElement.getElementsByTagName("language").item(0).getTextContent();
                String text = descriptionElement.getElementsByTagName("text").item(0).getTextContent();
                level.Descriptions.put(language, text);
            }
            levels.put(level.LevelId, level);
        }
    }

    NodeList numberDataNodes = doc.getElementsByTagName("number_data");
    List<CommodityCode> commodityCodeList = new ArrayList<>();
    for (int i = 0; i < numberDataNodes.getLength(); i++) {
        Node node = numberDataNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element numberDataElement = (Element) node;
            String levelId = numberDataElement.getElementsByTagName("level_id").item(0).getTextContent();
            if ("60".equals(levelId)) {
                CommodityCode commodityCode = new CommodityCode();
                commodityCode.CommodityCode = numberDataElement.getElementsByTagName("number").item(0).getTextContent();
                commodityCode.CommodityCodeUniqueID = numberDataElement.getElementsByTagName("id").item(0).getTextContent();
                commodityCode.ValidityStartDate = numberDataElement.getElementsByTagName("validity_begin").item(0).getTextContent();
                commodityCode.ValidityEndDate = numberDataElement.getElementsByTagName("validity_end").item(0).getTextContent();
                commodityCode.SupplementaryUnit = "KGM";  // Assuming

                NodeList textNodes = numberDataElement.getElementsByTagName("official_description");
                for (int j = 0; j < textNodes.getLength(); j++) {
                    Element textElement = (Element) textNodes.item(j);
                    String language = textElement.getElementsByTagName("language").item(0).getTextContent();
                    String text = textElement.getElementsByTagName("text").item(0).getTextContent();
                    commodityCode.CommodityCodeDescriptionList.add(new CommodityCodeDescription(language, text));
                }

                concatenateTexts(commodityCode, levels, numberDataElement.getElementsByTagName("parent_id").item(0).getTextContent());

                commodityCodeList.add(commodityCode);
            }
        }
    }

    JsonStructure jsonStructure = new JsonStructure();
    jsonStructure.TrdClassfctnCntntVersion = 54;
    jsonStructure.TrdClassfctnCntntRevisionVers = 1;
    jsonStructure.IsInitialVersion = true;
    jsonStructure.IsLastDataPackage = true;
    jsonStructure.DataPackageOrdinalNumber = 1;
    jsonStructure.CommodityCodeLength = 11;
    jsonStructure.CommodityCodeList = commodityCodeList;

    Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(JsonStructure.class, new UppercaseKeysSerializer()).create();
    String json = gson.toJson(jsonStructure);
    String jsonFilePath = outputDir + "/" + baseName + ".json";
    FileWriter writer = new FileWriter(jsonFilePath);
    writer.write(json);
    writer.close();
    logger.info("Converted XML to JSON: {}", jsonFilePath);
}
```

- **Beschreibung**: Konvertiert eine XML-Datei in JSON.
- **Schritte**:
    1. Lädt und parst die XML-Datei.
    2. Extrahiert Level-Daten und Beschreibungen, erstellt `Level`-Objekte.
    3. Extrahiert Commodity Code-Daten und erstellt `CommodityCode`-Objekte.
    4. Konvertiert die Datenstruktur in JSON und speichert sie in eine Datei.

##### concatenateTexts Methode

```java
private static void concatenateTexts(CommodityCode commodityCode, Map<String, Level> levels, String parentId) {
    StringBuilder enConcatenation = new StringBuilder();
    StringBuilder zhConcatenation = new StringBuilder();
    boolean first = true;
    while (parentId != null && !parentId.isEmpty()) {
        Level level = levels.get(parentId.substring(0, 2));
        if (level != null) {
            if (first) {
                enConcatenation.append(level.Descriptions.get("EN"));
                zhConcatenation.append(level.Descriptions.get("ZH"));
                first = false;
            } else {
                enConcatenation.insert(0, ", " + level.Descriptions.get("EN"));
                zhConcatenation.insert(0, ", " + level.Descriptions.get("ZH"));
            }
        }
        parentId = parentId.substring(2);
    }

    for (CommodityCodeDescription description : commodityCode.CommodityCodeDescriptionList) {
        if ("EN".equals(description.LanguageISOCode)) {
            description.ConcatenatedTariffNumberName = enConcatenation.append(", ").append(description.CommodityCodeName).toString();
        } else if ("ZH".equals(description.LanguageISOCode)) {
            description.ConcatenatedCommodityCodeName = zhConcatenation.append(", ").append(description.CommodityCodeName).toString();
        }
    }
}
```

- **Beschreibung**: Konkateniert Textbeschreibungen für Commodity Codes basierend auf der Level-Hierarchie.
- **Schritte**:
    1. Extrahiert und konkateniert Beschreibungen für alle Levels.
    2. Fügt die konkatenierten Beschreibungen zu den Commodity Code-Beschreibungen hinzu.

##### Zusätzliche Klassen

- **Level**: Repräsentiert ein Level in der Hierarchie mit seinen Beschreibungen.
- **JsonStructure**: Repräsentiert die Datenstruktur für die JSON-Datei.
- **CommodityCode**: Repräsentiert einen Commodity Code mit seinen Beschreibungen.
- **CommodityCodeDescription**: Repräsentiert die Beschreibung eines Commodity Codes in einer bestimmten Sprache.
- **UppercaseKeysSerializer**: JSON-Serializer, der Feldnamen in Großbuchstaben konvertiert.

