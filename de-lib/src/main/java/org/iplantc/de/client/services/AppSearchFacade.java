package org.iplantc.de.client.services;

import org.iplantc.de.client.models.apps.proxy.AppListLoadResult;
import org.iplantc.de.shared.DECallback;

/**
 * Created by sriram on 10/25/16.
 */
public interface AppSearchFacade {
    /**
     * Searches for all active Apps with a name or description that contains the given search term.
     * @param term  search term
     * @param callback  callback object to call when search is complete
     */
    void searchApp(String term, DECallback<AppListLoadResult> callback);
}
