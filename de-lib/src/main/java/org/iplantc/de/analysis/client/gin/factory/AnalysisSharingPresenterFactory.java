package org.iplantc.de.analysis.client.gin.factory;

import org.iplantc.de.client.models.analysis.Analysis;
import org.iplantc.de.client.sharing.SharingPresenter;

import java.util.List;

/**
 * @author aramsey
 */
public interface AnalysisSharingPresenterFactory {
    SharingPresenter create(List<Analysis> analyses);
}
