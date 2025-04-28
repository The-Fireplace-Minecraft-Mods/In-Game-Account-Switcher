/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.storage.account;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import ru.vidtu.ias.auth.handlers.LoginHandler;
import ru.vidtu.ias.platform.IStonecutter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.UTFDataFormatException;
import java.util.Objects;
import java.util.UUID;

/**
 * Offline account instance.
 *
 * @author VidTu
 * @apiNote Internal use only
 */
@ApiStatus.Internal
@NullMarked
public final class OfflineAccount extends Account {
    /**
     * Account type for the hover tooltip.
     */
    private static final Component TIP_TYPE = IStonecutter.translate("ias.accounts.tip.type.offline");

    /**
     * Account UUID.
     */
    private final UUID uuid;

    /**
     * Account name.
     */
    private final String name;

    /**
     * Account skin UUID.
     */
    private final UUID skin;

    /**
     * An immutable list of account hover tooltip lines.
     */
    @Unmodifiable
    private final ImmutableList<Component> tip;

    /**
     * Creates a new account.
     *
     * @param name Account name
     * @param skin Account skin UUID, {@code null} if none
     */
    @Contract(pure = true)
    public OfflineAccount(String name, @Nullable UUID skin) {
        // Call super.
        super(/*online=*/false);

        // Validate.
        assert name != null : "IAS: Parameter 'name' is null. (skin: " + skin + ", account: " + this + ')';
        assert !name.isBlank() : "IAS: Name is blank. (name: " + name + ", skin: " + skin + ", account: " + this + ')';
        assert name.length() <= 16 : "IAS: Name is longer than 16 characters. (name: " + name + ", skin: " + skin + ", account: " + this + ", nameLength: " + name.length() + ')';
        assert skin == null || skin.version() == 4 : "IAS: Skin version is not 4. (name: " + name + ", skin: " + skin + ", account: " + this + ", skinVersion: " + skin.version() + ')';

        // Assign.
        this.uuid = UUIDUtil.createOfflinePlayerUUID(name);
        this.name = name.intern(); // Implicit NPE for 'name'
        this.skin = MoreObjects.firstNonNull(skin, this.uuid);

        // Create.
        this.tip = ImmutableList.of(
                IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.accounts.tip.nick"), this.name),
                IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.accounts.tip.uuid"), this.uuid.toString().intern()),
                IStonecutter.translate("options.generic_value", IStonecutter.translate("ias.accounts.tip.type"), TIP_TYPE)
        );
    }

    /**
     * Gets the UUID. Offline account UUIDs are final and never change.
     *
     * @return Account UUID
     * @see #name()
     * @see #skin()
     */
    @Contract(pure = true)
    @Override
    public UUID uuid() {
        return this.uuid;
    }

    /**
     * Gets the name. Offline account names are final and never change.
     *
     * @return Account name
     * @see #uuid()
     */
    @Contract(pure = true)
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Gets the skin. Offline skins are <b>currently</b> final and don't change.
     *
     * @return Account skin UUID
     * @see #uuid()
     */
    @Override
    public UUID skin() {
        return this.skin;
    }

    /**
     * Gets the tip. Offline account tips are final and don't change.
     *
     * @return An immutable list of account hover tooltip lines
     */
    @Contract(pure = true)
    @Unmodifiable
    public ImmutableList<Component> tip() {
        return this.tip;
    }

    @Override
    public void login(LoginHandler handler) {
        // Offline account can be logged in only via offline.
        handler.error(new UnsupportedOperationException("Offline account login: " + this));
    }

    /**
     * Encodes the account into the binary output.
     *
     * @param out Binary output
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void encode(DataOutput out) throws IOException {
        // Validate.
        assert out != null : "Parameter 'out' is null. (account: " + this + ')';

        // Encode the type.
        out.writeUTF("ias:offline_v2"); // Implicit NPE for 'out'

        // Encode the name.
        out.writeUTF(this.name);

        // Encode the skin.
        if (this.skin != this.uuid) {
            out.writeBoolean(true);
            out.writeLong(this.skin.getMostSignificantBits());
            out.writeLong(this.skin.getLeastSignificantBits());
        } else {
            out.writeBoolean(false);
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OfflineAccount that)) return false;
        return Objects.equals(this.name, that.name);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return "IAS/OfflineAccount{" +
                "uuid=" + this.uuid +
                ", name='" + this.name + '\'' +
                ", skin=" + this.skin +
                ", tip=" + this.tip +
                '}';
    }

    /**
     * Decodes the account from the binary input.
     *
     * @param in      Binary input
     * @param hasSkin Whether to read the skin from the input
     * @return A newly created decoded account
     * @throws EOFException           If the {@code in} reaches the end before reading all the data
     * @throws UTFDataFormatException If the decoded {@code name} string byte sequence can't be used to create a valid UTF string
     * @throws InvalidObjectException If the decoded {@code name} is blank or too long or the {@code skin} is present ant its version is not {@code 4}
     * @throws IOException            If an I/O error occurs
     */
    @CheckReturnValue
    static OfflineAccount decode(DataInput in, boolean hasSkin) throws IOException {
        // Validate.
        assert in != null : "IAS: Parameter 'in' is null. (hasSkin: " + hasSkin + ')';

        // Decode and validate the name.
        String name = in.readUTF(); // Implicit NPE for 'in'
        if (name.isBlank()) {
            throw new InvalidObjectException("IAS: Name is blank. (in: " + in + ", name: " + name + ", hasSkin: " + hasSkin + ')');
        }
        int length = name.length();
        if (length > 16) {
            throw new InvalidObjectException("IAS: Name is longer than 16 characters. (in: " + in + ", name: " + name + ", hasSkin: " + hasSkin + ", nameLength: " + length + ')');
        }

        // No skin present.
        if (!hasSkin || !in.readBoolean()) {
            return new OfflineAccount(name, null);
        }

        // Decode and validate the skin.
        long msb = in.readLong();
        long lsb = in.readLong();
        UUID skin = new UUID(msb, lsb);
        int version = skin.version();
        if (version != 4) {
            throw new InvalidObjectException("IAS: Skin version is not 4. (in: " + in + ", name: " + name + ", skin: " + skin + ", skinVersion: " + version + ')');
        }

        // Create and return.
        return new OfflineAccount(name, skin);
    }
}
