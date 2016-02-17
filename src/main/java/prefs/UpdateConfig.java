package prefs;

import util.Version;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class UpdateConfig {

    private Version lastUpdateVersionDownloaded;
    private boolean lastUpdateDownloadStatus;
    private List<Version> listOfVersionsPreviouslyDownloaded;

    public void setLastUpdateVersionDownloaded(Version version) {
        this.lastUpdateVersionDownloaded = version;
    }

    public Version getLastUpdateVersionDownloaded() {
        return this.lastUpdateVersionDownloaded;
    }

    public void setLastUpdateDownloadStatus(boolean status) {
        this.lastUpdateDownloadStatus = status;
    }

    public boolean getLastUpdateDownloadStatus() {
        return this.lastUpdateDownloadStatus;
    }

    public void addToVersionPreviouslyDownloaded(Version downloadedVersion) {
        if (listOfVersionsPreviouslyDownloaded == null) {
            listOfVersionsPreviouslyDownloaded = new ArrayList<>();
        }

        listOfVersionsPreviouslyDownloaded.add(downloadedVersion);
    }

    public boolean checkIfVersionWasPreviouslyDownloaded(Version version) {
        if (listOfVersionsPreviouslyDownloaded == null) {
            listOfVersionsPreviouslyDownloaded = new ArrayList<>();
            return false;
        }

        return listOfVersionsPreviouslyDownloaded.contains(version);
    }
}
