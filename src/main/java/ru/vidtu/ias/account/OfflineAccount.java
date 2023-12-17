package ru.vidtu.ias.account;

import ru.vidtu.ias.IAS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

/**
 * Offline account instance.
 *
 * @param uuid Account UUID
 * @param name Account name
 * @author VidTu
 */
public record OfflineAccount(UUID uuid, String name) implements Account {
    @Override
    public void login(LoginHandler handler) {
        // Log the info.
        IAS.LOG.info("Logging (offline) as {}/{}", this.uuid, this.name);

        // Log in.
        LoginData data = new LoginData(this.name, this.uuid, "ias:offline", LoginData.LEGACY);
        handler.success(data);
    }

    /**
     * Writes the account to the output.
     *
     * @param out Target output
     * @throws IOException On I/O error
     */
    @Override
    public void write(DataOutput out) throws IOException {
        // Write the UUID.
        out.writeLong(this.uuid.getMostSignificantBits());
        out.writeLong(this.uuid.getLeastSignificantBits());

        // Write the name.
        out.writeUTF(this.name);
    }

    /**
     * Reads the account from the input.
     *
     * @param in Target input
     * @return Read account
     * @throws IOException On I/O error
     */
    public static OfflineAccount read(DataInput in) throws IOException {
        // Read the UUID.
        UUID uuid = new UUID(in.readLong(), in.readLong());

        // Read the name.
        String name = in.readUTF();

        // Create and return.
        return new OfflineAccount(uuid, name);
    }
}
