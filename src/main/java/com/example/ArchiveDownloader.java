package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveDownloader {

    private static final Logger logger =
            LoggerFactory.getLogger(ArchiveDownloader.class);

    private static final String ZIP_URL = "https://www.awrportal.de/temp/HSCTWXX_I_00054_new-schema.zip";
    private static final String DOWNLOAD_DIR = "src/main/resources/downloads";
    private static final String EXTRACT_DIR = "src/main/resources/extracted";
    private static final String JSON_OUTPUT_DIR = "src/main/resources" +
            "/json_output";
    private static final String DB_URL = "jdbc:h2:./data/h2db";

    public static void main(String[] args) {
        try {
            Path zipPath = downloadZip(ZIP_URL, DOWNLOAD_DIR);
            extractZip(zipPath, EXTRACT_DIR);
            Files.createDirectories(Paths.get(JSON_OUTPUT_DIR));
            initializeDatabase();

            Files.walk(Paths.get(EXTRACT_DIR))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .forEach(ArchiveDownloader::processXmlFile);

            processXmlFilesFromDatabase();

        } catch(IOException | SQLException e) {
            logger.error("An error occurred: ", e);
        }
    }

    private static Path downloadZip(String url, String downloadDir) throws IOException {
        logger.info("Downloading ZIP from {}", url);
        URL website = new URL(url);
        Path zipPath = Paths.get(downloadDir, "archive.zip");
        try(InputStream in = website.openStream()) {
            Files.createDirectories(zipPath.getParent());
            Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return zipPath;
    }

    private static void extractZip(Path zipPath, String extractDir) throws IOException {
        logger.info("Extracting ZIP to {}", extractDir);
        Files.createDirectories(Paths.get(extractDir));
        try(ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(extractDir, entry.getName());
                if(entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try(InputStream in = zipFile.getInputStream(entry);
                        OutputStream out = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private static void initializeDatabase() throws SQLException {
        logger.info("Initializing database");
        try(Connection conn = DriverManager.getConnection(DB_URL, "sa", "");
            Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS xml_data (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "filename VARCHAR(255), " +
                    "content CLOB)";
            stmt.execute(sql);
        }
    }

    private static void processXmlFile(Path path) {
        try {
            String xmlContent = new String(Files.readAllBytes(path));
            saveXmlToDatabase(path.getFileName().toString(), xmlContent);
        } catch(IOException | SQLException e) {
            logger.error("Failed to process XML file: {}", path, e);
        }
    }

    private static void saveXmlToDatabase(String fileName, String content) throws SQLException {
        logger.info("Saving XML to database: {}", fileName);
        String sql = "INSERT INTO xml_data (filename, content) VALUES (?, ?)";
        try(Connection conn = DriverManager.getConnection(DB_URL, "sa", "");
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setClob(2, new StringReader(content));
            pstmt.executeUpdate();
        }
    }

    private static void processXmlFilesFromDatabase() throws SQLException {
        logger.info("Processing XML files from database");
        String sql = "SELECT filename, content FROM xml_data";
        try(Connection conn = DriverManager.getConnection(DB_URL, "sa", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String fileName = rs.getString("filename");
                String xmlContent = rs.getString("content");
                String jsonFileName = fileName.replace(".xml", ".json");
                String outputJsonPath = Paths.get(JSON_OUTPUT_DIR,
                        jsonFileName).toString();
                try {
                    XmlToJsonConversion.processXmlContent(xmlContent,
                            outputJsonPath);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
