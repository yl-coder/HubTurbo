package updater;

import ui.UI;
import util.Version;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents update data serializable
 */
public class UpdateData {

    public List<UpdateDownloadLink> listOfLinks;

    /**
     * Get UpdateDownloadLink for update
     * @return Optional.empty() if there is no update, UpdateDownloadLink of update that can be downloaded otherwise
     */
    // TODO
    public Optional<UpdateDownloadLink> getUpdateDownloadLink() {
        if (listOfLinks == null) {
            return Optional.empty();
        }

        // List the update link in descending order of version
        Collections.sort(listOfLinks, Collections.reverseOrder());

        Version current = Version.getCurrentVersion();

        // Get link of version that has same major version or just 1 major version up than current
        Optional<UpdateDownloadLink> updateLink = listOfLinks.stream()
                .filter(link ->
                        link.version.getMajor() == current.getMajor() ||
                        link.version.getMajor() == current.getMajor() + 1)
                .findFirst();

        if (updateLink.isPresent()) {
            return updateLink;
        }

        return Optional.empty();
    }
}
