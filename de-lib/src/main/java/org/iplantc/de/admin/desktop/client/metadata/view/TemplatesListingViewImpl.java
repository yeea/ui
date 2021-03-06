package org.iplantc.de.admin.desktop.client.metadata.view;

import org.iplantc.de.admin.desktop.client.metadata.events.AddMetadataSelectedEvent;
import org.iplantc.de.admin.desktop.client.metadata.events.DeleteMetadataSelectedEvent;
import org.iplantc.de.admin.desktop.client.metadata.events.EditMetadataSelectedEvent;
import org.iplantc.de.admin.desktop.shared.Belphegor;
import org.iplantc.de.client.models.diskResources.MetadataTemplateInfo;
import org.iplantc.de.client.util.StaticIdHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.ViewReadyEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplatesListingViewImpl extends Composite implements IsWidget, TemplateListingView {

    private static TemplatesListingViewImplUiBinder uiBinder = GWT.create(TemplatesListingViewImplUiBinder.class);

    interface TemplatesListingViewImplUiBinder extends UiBinder<Widget, TemplatesListingViewImpl> {
    }

    @UiField
    TextButton addBtn, editBtn, delBtn;
    @UiField
    Grid<MetadataTemplateInfo> grid;
    @UiField
    ListStore<MetadataTemplateInfo> store;
    @UiField
    ColumnModel<MetadataTemplateInfo> cm;
    @UiField
    VerticalLayoutContainer con;
    @UiField(provided = true)
    TemplateListingView.TemplateListingAppearance appearance;

    private final MetadataTemplateProperties props;

    @Inject
    public TemplatesListingViewImpl(final MetadataTemplateProperties props,
                                    TemplateListingView.TemplateListingAppearance appearance) {
        this.props = props;
        this.appearance = appearance;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void onEnsureDebugId(final String baseID) {
        super.onEnsureDebugId(baseID);

        addBtn.ensureDebugId(baseID + Belphegor.MetadataIds.ADD);
        editBtn.ensureDebugId(baseID + Belphegor.MetadataIds.EDIT);
        delBtn.ensureDebugId(baseID + Belphegor.MetadataIds.DELETE);
        grid.ensureDebugId(baseID + Belphegor.MetadataIds.GRID);
        grid.addViewReadyHandler(new ViewReadyEvent.ViewReadyHandler() {
            @Override
            public void onViewReady(ViewReadyEvent event) {
                StaticIdHelper.getInstance()
                              .gridColumnHeaders(baseID + Belphegor.MetadataIds.GRID
                                                 + Belphegor.MetadataIds.COL_HEADER, grid);
            }
        });
    }

    @Override
    public void mask(String loadingMask) {
        con.mask(loadingMask);

    }

    @Override
    public void unmask() {
        con.unmask();

    }

    @Override
    public HandlerRegistration addEditMetadataSelectedEventHandler(EditMetadataSelectedEvent.EditMetadataSelectedEventHandler handler) {
        return addHandler(handler, EditMetadataSelectedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addAddMetadataSelectedEventHandler(AddMetadataSelectedEvent.AddMetadataSelectedEventHandler handler) {
        return addHandler(handler, AddMetadataSelectedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addDeleteMetadataSelectedEventHandler(DeleteMetadataSelectedEvent.DeleteMetadataSelectedEventHandler handler) {
        return addHandler(handler, DeleteMetadataSelectedEvent.TYPE);
    }

    @UiHandler("addBtn")
    void addButtonClicked(SelectEvent event) {
        fireEvent(new AddMetadataSelectedEvent());
    }

    @UiHandler("editBtn")
    void editButtonClicked(SelectEvent event) {
        MetadataTemplateInfo selectedItem = grid.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            fireEvent(new EditMetadataSelectedEvent(selectedItem));
        }
    }

    @UiHandler("delBtn")
    void delButtonClicked(SelectEvent event) {
        MetadataTemplateInfo selectedItem = grid.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            fireEvent(new DeleteMetadataSelectedEvent(selectedItem));
        }
    }

    @UiFactory
    ListStore<MetadataTemplateInfo> createListStore() {
        final ListStore<MetadataTemplateInfo> listStore = new ListStore<>(new ModelKeyProvider<MetadataTemplateInfo>() {

            @Override
            public String getKey(MetadataTemplateInfo item) {
                return item.getId();
            }
        });
        listStore.setEnableFilters(true);
        return listStore;
    }

    @UiFactory
    ColumnModel<MetadataTemplateInfo> createColumnModel() {
        ColumnConfig<MetadataTemplateInfo, String> nameCol = new ColumnConfig<>(props.name(),
                                                                                150,
                                                                                appearance.nameColumn());
        ColumnConfig<MetadataTemplateInfo, Date> createdOnCol = new ColumnConfig<>(props.createdDate(),
                                                                                   192,
                                                                                   appearance.createdOn());
        ColumnConfig<MetadataTemplateInfo, String> createdByCol = new ColumnConfig<>(props.createdBy(),
                                                                                     160,
                                                                                     appearance.createdBy());
        ColumnConfig<MetadataTemplateInfo, Boolean> deletedCol = new ColumnConfig<>(props.deleted(),
                                                                                    50,
                                                                                    appearance.deleted());
        List<ColumnConfig<MetadataTemplateInfo, ?>> columns = new ArrayList<ColumnConfig<MetadataTemplateInfo, ?>>();
        columns.add(nameCol);
        columns.add(createdOnCol);
        columns.add(createdByCol);
        columns.add(deletedCol);
        return new ColumnModel<>(columns);

    }

    @Override
    public void loadTemplates(List<MetadataTemplateInfo> result) {
        store.clear();
        store.addAll(result);
    }

    @Override
    public void remove(MetadataTemplateInfo template) {
        store.remove(template);
    }

}
