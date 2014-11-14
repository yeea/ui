package org.iplantc.de.diskResource.client.gin.factory;

import org.iplantc.de.client.models.viewer.InfoType;
import org.iplantc.de.diskResource.client.views.widgets.FileSelectorField;
import org.iplantc.de.diskResource.client.views.widgets.FolderSelectorField;

import java.util.List;

/**
 * @author jstroot
 */
public interface DiskResourceSelectorFieldFactory {
    FolderSelectorField createFilteredFolderSelector(List<InfoType> infoTypes);

    FolderSelectorField defaultFolderSelector();


    FileSelectorField createFilteredFileSelector(List<InfoType> infoTypes);
    FileSelectorField defaultFileSelector();


}
