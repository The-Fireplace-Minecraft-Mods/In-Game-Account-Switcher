package ru.vidtu.ias.auth.account;

import com.google.common.collect.BiMap;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vidtu.ias.auth.AuthData;
import ru.vidtu.ias.auth.ms.AuthStage;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Parent interface for all accounts.
 *
 * @author VidTu
 */
public sealed interface Account permits MicrosoftAccount, OfflineAccount {
    /**
     * Gets the UUID of this account.
     *
     * @return Account UUID
     */
    @CheckReturnValue
    @NotNull
    UUID uuid();

    /**
     * Gets the username of this account.
     *
     * @return Account player name
     */
    @CheckReturnValue
    @NotNull
    String name();

    /**
     * Starts the authentication process for this account.
     *
     * @param progress Progress handler (can be called from another thread)
     * @return Future that will complete with auth data or exceptionally
     */
    @Contract(value = "_ -> new")
    @CheckReturnValue
    @NotNull
    CompletableFuture<AuthData> login(@NotNull Consumer<AuthStage> progress);
}
