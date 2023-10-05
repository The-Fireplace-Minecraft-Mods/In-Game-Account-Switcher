package ru.vidtu.ias;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SharedIAS {
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor(r -> new Thread(r, "IAS Executor"));
    public static final Logger LOG = LoggerFactory.getLogger("IAS");
    public static final Gson GSON = new Gson();
    public static final String[] LOADING = new String[]{"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
            "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
            "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
            "▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
            "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
            "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"};
}
