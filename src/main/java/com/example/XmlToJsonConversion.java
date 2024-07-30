package com.example;

import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class XmlToJsonConversion {

    public static void processXmlFile(String xmlFilePath,
                                      String outputJsonPath) throws IOException {

        String xmlContent =
                new String(Files.readAllBytes(Paths.get(xmlFilePath)));


        String jsonString = convertXmlToJson(xmlContent);


        storeConvertedJsonInFile(jsonString, outputJsonPath);
    }

    public static void processXmlContent(String xmlContent,
                                         String outputJsonPath) throws IOException {
        String jsonString = convertXmlToJson(xmlContent);
        storeConvertedJsonInFile(jsonString, outputJsonPath);
    }

    private static String convertXmlToJson(String xml) {
        JSONObject jsonObject = XML.toJSONObject(xml);
        return jsonObject.toString(4);
    }

    private static void storeConvertedJsonInFile(String jsonString,
                                                 String outputPath) throws IOException {
        Path filename = Paths.get(outputPath);
        Files.write(filename, jsonString.getBytes());
    }
}
