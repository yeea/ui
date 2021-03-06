package org.iplantc.de.admin.apps.client.views.grid;

import org.iplantc.de.admin.apps.client.AdminAppsGridView;
import org.iplantc.de.admin.desktop.client.ontologies.events.HierarchySelectedEvent;
import org.iplantc.de.admin.desktop.client.ontologies.events.PreviewHierarchySelectedEvent;
import org.iplantc.de.admin.desktop.client.ontologies.events.SelectOntologyVersionEvent;
import org.iplantc.de.admin.desktop.shared.Belphegor;
import org.iplantc.de.apps.client.events.AppSearchResultLoadEvent;
import org.iplantc.de.apps.client.events.BeforeAppSearchEvent;
import org.iplantc.de.apps.client.events.selection.AppCategorySelectionChangedEvent;
import org.iplantc.de.apps.client.events.selection.AppInfoSelectedEvent;
import org.iplantc.de.apps.client.events.selection.AppSelectionChangedEvent;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.util.StaticIdHelper;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.ViewReadyEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import java.util.List;

/**
 * Created by jstroot on 3/9/15.
 * @author jstroot
 */
public class AdminAppsGridImpl extends ContentPanel implements AdminAppsGridView,
                                                               SelectionChangedEvent.SelectionChangedHandler<App> {

    interface AdminAppsGridImplUiBinder extends UiBinder<Widget, AdminAppsGridImpl> { }

    private static AdminAppsGridImplUiBinder ourUiBinder = GWT.create(AdminAppsGridImplUiBinder.class);
    @UiField(provided = true) ListStore<App> listStore;
    @UiField ColumnModel<App> cm;
    @UiField GridView<App> gridView;
    @UiField Grid<App> grid;

    private final AdminAppsColumnModel acm; // Convenience class

    @Inject AdminAppsGridView.Appearance appearance;

    @Inject
    AdminAppsGridImpl(@Assisted final ListStore<App> listStore) {
        this.listStore = listStore;

        setWidget(ourUiBinder.createAndBindUi(this));
        this.acm = (AdminAppsColumnModel) cm;
        grid.getSelectionModel().addSelectionChangedHandler(this);
    }

    @Override
    public HandlerRegistration addAppInfoSelectedEventHandler(AppInfoSelectedEvent.AppInfoSelectedEventHandler handler) {
        return acm.addAppInfoSelectedEventHandler(handler);
    }

    @Override
    public HandlerRegistration addAppSelectionChangedEventHandler(AppSelectionChangedEvent.AppSelectionChangedEventHandler handler) {
        return addHandler(handler, AppSelectionChangedEvent.TYPE);
    }

    @Override
    public Grid<App> getGrid() {
        return grid;
    }

    @Override
    public void clearAndAdd(List<App> apps) {
        listStore.clear();
        if (apps != null && !apps.isEmpty()) {
            listStore.addAll(apps);
        }
    }

    @Override
    public void onAppCategorySelectionChanged(AppCategorySelectionChangedEvent event) {
        // FIXME Move to appearance
        setHeading(Joiner.on(" >> ").join(event.getGroupHierarchy()));
    }

    @Override
    public void onHierarchySelected(HierarchySelectedEvent event) {
        setHeading(Joiner.on(" >> ").join(event.getPath()));
    }

    @Override
    public void onPreviewHierarchySelected(PreviewHierarchySelectedEvent event) {
        setHeading(Joiner.on(" >> ").join(event.getPath()));
    }

    @Override
    public void onAppSearchResultLoad(AppSearchResultLoadEvent event) {
        unmask();
//        searchRegexPattern = event.getSearchPattern();
//        acm.setSearchRegexPattern(searchRegexPattern);

        int total = event.getResults() == null ? 0 : event.getResults().size();
        setHeading(appearance.searchAppResultsHeader(event.getSearchText(), total));
    }

    @Override
    public void onBeforeAppSearch(BeforeAppSearchEvent event) {
        mask(appearance.beforeAppSearchLoadingMask());
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<App> event) {
        fireEvent(new AppSelectionChangedEvent(event.getSelection()));
    }

    @UiFactory
    ColumnModel<App> createColumnModel() {
        return new AdminAppsColumnModel();
    }

    @Override
    protected void onEnsureDebugId(final String baseID) {
        super.onEnsureDebugId(baseID);

        acm.ensureDebugId(baseID);
        grid.asWidget().ensureDebugId(baseID + Belphegor.AppIds.GRID);
        grid.addViewReadyHandler(new ViewReadyEvent.ViewReadyHandler() {
            @Override
            public void onViewReady(ViewReadyEvent event) {
                StaticIdHelper.getInstance()
                              .gridColumnHeaders(baseID + Belphegor.AppIds.GRID
                                                 + Belphegor.AppIds.COL_HEADER, grid);
            }
        });
    }

    @Override
    public App getAppFromElement(Element as) {
        Element row = gridView.findRow(as);
        int dropIndex = gridView.findRowIndex(row);
        return listStore.get(dropIndex);
    }

    @Override
    public List<App> getSelectedApps() {
        return grid.getSelectionModel().getSelectedItems();
    }

    @Override
    public void deselectAll() {
        grid.getSelectionModel().deselectAll();
    }

    @Override
    public void removeApp(App selectedApp) {
        App app = listStore.findModelWithKey(selectedApp.getId());
        if (app != null) {
            listStore.remove(app);
        }
    }

    @Override
    public void onSelectOntologyVersion(SelectOntologyVersionEvent event) {
        getHeader().setHTML("&nbsp;");
    }
}
