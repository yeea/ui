package org.iplantc.de.pipelines.client.views;

import org.iplantc.de.client.models.pipelines.Pipeline;
import org.iplantc.de.client.models.pipelines.PipelineTask;
import org.iplantc.de.pipelines.shared.Pipelines;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;

import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.client.editor.ListStoreEditor;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import java.util.List;

/**
 * An implementation of the PipelineAppOrderView.
 *
 * @author psarando
 */
public class PipelineAppOrderViewImpl extends Composite
        implements PipelineAppOrderView, Editor<Pipeline> {

    @UiTemplate("PipelineAppOrderView.ui.xml")
    interface PipelineAppOrderUiBinder extends UiBinder<Widget, PipelineAppOrderViewImpl> {
    }

    private static PipelineAppOrderUiBinder uiBinder = GWT.create(PipelineAppOrderUiBinder.class);
    private static PipelineAppProperties pipelineAppProps = GWT.create(PipelineAppProperties.class);
    private Presenter presenter;

    ListStoreEditor<PipelineTask> apps;

    @UiField
    @Ignore
    TextButton addAppsBtn;

    @UiField
    @Ignore
    TextButton removeAppBtn;

    @UiField
    @Ignore
    TextButton moveDownBtn;

    @UiField
    @Ignore
    TextButton moveUpBtn;

    public PipelineAppOrderViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        apps = new AppListStoreEditor(this);
        appOrderGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        appOrderGrid.getSelectionModel()
                    .addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<PipelineTask>() {
                        @Override
                        public void onSelectionChanged(SelectionChangedEvent<PipelineTask> event) {
                            setButtonState();
                        }
                    });

    }

    @UiField
    Grid<PipelineTask> appOrderGrid;

    @UiField
    ListStore<PipelineTask> pipelineAppStore;

    @UiFactory
    ListStore<PipelineTask> createListStore() {
        ListStore<PipelineTask> store =
                new ListStore<PipelineTask>(new ModelKeyProvider<PipelineTask>() {

                    @Override
                    public String getKey(PipelineTask item) {
                        //prevent liststore assertion errors
                        return item.getTaskId() + "-" + presenter.getStepName(item);
                    }
                });

        store.addSortInfo(new StoreSortInfo<PipelineTask>(pipelineAppProps.step(), SortDir.ASC));

        return store;
    }

    @UiFactory
    ColumnModel<PipelineTask> createColumnModel() {
        return new AppColumnModel(pipelineAppProps);
    }

    @UiHandler("addAppsBtn")
    public void onAddAppsClick(SelectEvent e) {
        presenter.onAddAppsClicked();
    }

    @UiHandler("removeAppBtn")
    public void onRemoveAppClick(SelectEvent e) {
        presenter.onRemoveAppClicked();
        setButtonState();
    }

    @UiHandler("moveUpBtn")
    public void onMoveUpClick(SelectEvent e) {
        presenter.onMoveUpClicked();
        setButtonState();
    }

    @UiHandler("moveDownBtn")
    public void onMoveDownClick(SelectEvent e) {
        presenter.onMoveDownClicked();
        setButtonState();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ListStore<PipelineTask> getPipelineAppStore() {
        return pipelineAppStore;
    }

    @Override
    public PipelineTask getOrderGridSelectedApp() {
        return appOrderGrid.getSelectionModel().getSelectedItem();
    }

    private void setButtonState() {
        int size = appOrderGrid.getSelectionModel().getSelectedItems().size();
        if (size > 0) {
            removeAppBtn.enable();
            if (appOrderGrid.getStore().size() > 1) {
                PipelineTask task = appOrderGrid.getSelectionModel().getSelectedItem();
                if (getPipelineAppStore().indexOf(task)
                    == getPipelineAppStore().size() - 1) { //last item
                    moveDownBtn.disable();
                } else {
                    moveDownBtn.enable();
                }
                if (getPipelineAppStore().indexOf(task) == 0) {   //first item
                    moveUpBtn.disable();
                } else {
                    moveUpBtn.enable();
                }
            }
        } else {
            removeAppBtn.disable();
            moveUpBtn.disable();
            moveDownBtn.disable();
        }
    }

    public class AppListStoreEditor extends ListStoreEditor<PipelineTask> {
        private final PipelineAppOrderView view;
        private EditorDelegate<List<PipelineTask>> delegate;

        public AppListStoreEditor(PipelineAppOrderView view) {
            super(view.getPipelineAppStore());

            this.view = view;
        }

        @Override
        public void flush() {
            ListStore<PipelineTask> pipelineAppStore = view.getPipelineAppStore();
            if (delegate != null && pipelineAppStore.size() < 2) {
                delegate.recordError(I18N.DISPLAY.selectOrderPnlTip(), pipelineAppStore.getAll(), view);
            }

            super.flush();
        }

        @Override
        public void onPropertyChange(String... paths) {
            // no-op
        }

        @Override
        public void setDelegate(EditorDelegate<List<PipelineTask>> delegate) {
            this.delegate = delegate;
        }
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        appOrderGrid.ensureDebugId(baseID + Pipelines.Ids.APP_ORDER_GRID);
    }
}
