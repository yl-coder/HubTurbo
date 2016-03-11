package updater;

import util.Version;

import java.net.URL;

/**
 * Represents a HT version with its download link
 */
public class UpdateDownloadLink implements Comparable<UpdateDownloadLink> {
    public Version version;
    public URL applicationFileLocation;

    @Override
    public int compareTo(UpdateDownloadLink other) {
        return this.version.compareTo(other.version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!UpdateDownloadLink.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final UpdateDownloadLink other = (UpdateDownloadLink) obj;

        return this.version.equals(other.version);
    }

    @Override
    public int hashCode() {
        return this.version.hashCode();
    }
}
