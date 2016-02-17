package util;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 *
 */
public class JsonSerializationConverter {
    private static final Logger logger = LogManager.getLogger(JsonSerializationConverter.class.getName());
    private static final String CHARSET = "UTF-8";

    private Gson gson;
    private final File jsonFile;

    public JsonSerializationConverter(File jsonFile) {
        this.jsonFile = jsonFile;
        setupGson();
        createDirectories();
    }

    public void saveToFile(Object object) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile) , CHARSET);
            gson.toJson(object, object.getClass(), writer);
            writer.close();
        } catch (IOException e) {
            logger.error("Failed to save object to JSON", e);
        }
    }

    public Optional<Object> loadFromFile(Class<?> objectClass) {
        if (jsonFile.exists()) {
            try (Reader reader =
                         new InputStreamReader(new FileInputStream(jsonFile), CHARSET)) {
                return Optional.ofNullable(gson.fromJson(reader, objectClass));
            } catch (IOException e) {
                logger.error(" Failed to load object from JSON file", e);
                return Optional.empty();
            }
        } else {
            logger.info("JSON file %1$s does not exist", jsonFile.getName());
            return Optional.empty();
        }
    }


    private void setupGson() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> {
                            Instant instant = src.atZone(ZoneId.systemDefault()).toInstant();
                            long epochMilli = instant.toEpochMilli();
                            return new JsonPrimitive(epochMilli);
                        })
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                            Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        }
                ).create();
    }

    private void createDirectories() {
        if (jsonFile.getParentFile() != null) {
            File directory = new File(jsonFile.getParent());

            if ((!directory.exists() || !directory.isDirectory()) && !directory.mkdirs()) {
                logger.warn("Could not create config file directory");
            }
        }
    }
}
