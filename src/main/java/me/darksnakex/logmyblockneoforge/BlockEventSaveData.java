package me.darksnakex.blocktracker;

import com.google.gson.*;
import me.darksnakex.blocktracker.Utils.WorldBlockPos;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static com.mojang.text2speech.Narrator.LOGGER;
import static me.darksnakex.blocktracker.Events.BlockEventHandler.blockEventMap;
import static me.darksnakex.blocktracker.Events.BlockEventHandler.blockEventTimes;

public class BlockEventSaveData {

    private static final String DATA_FILE = "./config/logmyblockdata.json";  // Ruta al archivo de datos



    public static void saveBlockEvents() {
        File file = new File(DATA_FILE);
        JsonObject jsonObject = new JsonObject();

        // Serializar blockEventMap
        JsonObject eventMapJson = new JsonObject();
        for (Map.Entry<WorldBlockPos, List<String>> entry : blockEventMap.entrySet()) {
            JsonArray eventArray = new JsonArray();
            for (String event : entry.getValue()) {
                eventArray.add(event);
            }
            eventMapJson.add(entry.getKey().toString(), eventArray);
        }
        jsonObject.add("blockEventMap", eventMapJson);

        // Serializar blockEventTimes
        JsonObject eventTimesJson = new JsonObject();
        for (Map.Entry<WorldBlockPos, List<Instant>> entry : blockEventTimes.entrySet()) {
            JsonArray timeArray = new JsonArray();
            for (Instant time : entry.getValue()) {
                timeArray.add(time.toString());
            }
            eventTimesJson.add(entry.getKey().toString(), timeArray);
        }
        jsonObject.add("blockEventTimes", eventTimesJson);

        // Escribir los datos en el archivo
        if (writeFile(file, jsonObject)) {
            LOGGER.info("LogMyBlock: Finished Block event logging!");
        } else {
            LOGGER.error("LogMyBlock: Failed Block event logging!");
        }
    }


    public static void loadBlockEvents() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            LOGGER.warn("LogMyBlock: No block event log file found.");
            return;
        }

        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);

            // Deserializar blockEventMap
            JsonObject eventMapJson = jsonObject.getAsJsonObject("blockEventMap");
            for (Map.Entry<String, JsonElement> entry : eventMapJson.entrySet()) {
                WorldBlockPos pos = WorldBlockPos.fromString(entry.getKey());
                JsonArray eventArray = entry.getValue().getAsJsonArray();
                List<String> events = new ArrayList<>();
                for (JsonElement event : eventArray) {
                    events.add(event.getAsString());
                }
                blockEventMap.put(pos, events);
            }

            // Deserializar blockEventTimes
            JsonObject eventTimesJson = jsonObject.getAsJsonObject("blockEventTimes");
            for (Map.Entry<String, JsonElement> entry : eventTimesJson.entrySet()) {
                WorldBlockPos pos = WorldBlockPos.fromString(entry.getKey());
                JsonArray timeArray = entry.getValue().getAsJsonArray();
                List<Instant> times = new ArrayList<>();
                for (JsonElement time : timeArray) {
                    times.add(Instant.parse(time.getAsString()));
                }
                blockEventTimes.put(pos, times);
            }

            LOGGER.info("LogMyBlock: Block events loaded successfully.");
        } catch (IOException e) {
            LOGGER.error("LogMyBlock: Failed to load block event log file.", e);
        }
    }


    private static boolean writeFile(File file, JsonElement jsonElement) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {

            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (Writer writer = new FileWriter(file)) {
                writer.write(gson.toJson(jsonElement));
            }
        } catch (IOException e) {
            LOGGER.error("LogMyBlock: Writing JSON file failed!", e);
            return false;
        }
        return true;
    }


}
