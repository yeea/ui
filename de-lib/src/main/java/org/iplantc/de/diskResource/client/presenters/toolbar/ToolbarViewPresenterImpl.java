package org.iplantc.de.diskResource.client.presenters.toolbar;

import static com.sencha.gxt.widget.core.client.Dialog.PredefinedButton.OK;

import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.events.diskResources.OpenFolderEvent;
import org.iplantc.de.client.gin.ServicesInjector;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.diskResources.DiskResourceAutoBeanFactory;
import org.iplantc.de.client.models.diskResources.File;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.models.diskResources.HTPathListRequest;
import org.iplantc.de.client.models.diskResources.MetadataTemplateInfo;
import org.iplantc.de.client.models.errors.diskResources.DiskResourceErrorAutoBeanFactory;
import org.iplantc.de.client.models.genomes.GenomeAutoBeanFactory;
import org.iplantc.de.client.models.genomes.GenomeList;
import org.iplantc.de.client.models.identifiers.PermanentIdRequestType;
import org.iplantc.de.client.models.sharing.PermissionValue;
import org.iplantc.de.client.models.viewer.InfoType;
import org.iplantc.de.client.models.viewer.MimeType;
import org.iplantc.de.client.services.DiskResourceServiceFacade;
import org.iplantc.de.client.services.FileEditorServiceFacade;
import org.iplantc.de.client.services.PermIdRequestUserServiceFacade;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.info.SuccessAnnouncementConfig;
import org.iplantc.de.commons.client.views.dialogs.IPlantDialog;
import org.iplantc.de.commons.client.views.window.configs.ConfigFactory;
import org.iplantc.de.commons.client.views.window.configs.FileViewerWindowConfig;
import org.iplantc.de.commons.client.views.window.configs.PathListWindowConfig;
import org.iplantc.de.commons.client.views.window.configs.TabularFileViewerWindowConfig;
import org.iplantc.de.diskResource.client.DataLinkView;
import org.iplantc.de.diskResource.client.DiskResourceView;
import org.iplantc.de.diskResource.client.ToolbarView;
import org.iplantc.de.diskResource.client.events.CreateNewFileEvent;
import org.iplantc.de.diskResource.client.events.RequestSimpleDownloadEvent;
import org.iplantc.de.diskResource.client.events.ShowFilePreviewEvent;
import org.iplantc.de.diskResource.client.events.selection.CreateNcbiSraFolderStructureSelected;
import org.iplantc.de.diskResource.client.events.selection.CreateNewFolderSelected;
import org.iplantc.de.diskResource.client.events.selection.SimpleDownloadSelected;
import org.iplantc.de.diskResource.client.events.selection.SimpleDownloadSelected.SimpleDownloadSelectedHandler;
import org.iplantc.de.diskResource.client.gin.factory.BulkMetadataDialogFactory;
import org.iplantc.de.diskResource.client.gin.factory.DataLinkPresenterFactory;
import org.iplantc.de.diskResource.client.gin.factory.DiskResourceSelectorFieldFactory;
import org.iplantc.de.diskResource.client.gin.factory.HTPathListAutomationDialogFactory;
import org.iplantc.de.diskResource.client.gin.factory.ToolbarViewFactory;
import org.iplantc.de.diskResource.client.views.dialogs.BulkMetadataDialog;
import org.iplantc.de.diskResource.client.views.dialogs.CreateFolderDialog;
import org.iplantc.de.diskResource.client.views.dialogs.CreateNcbiSraFolderStructureDialog;
import org.iplantc.de.diskResource.client.views.dialogs.GenomeSearchDialog;
import org.iplantc.de.diskResource.client.views.dialogs.HTPathListAutomationDialog;
import org.iplantc.de.diskResource.client.views.toolbar.dialogs.TabFileConfigDialog;
import org.iplantc.de.shared.AsyncProviderWrapper;
import org.iplantc.de.shared.DataCallback;

import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author jstroot
 */
public class ToolbarViewPresenterImpl implements ToolbarView.Presenter, SimpleDownloadSelectedHandler {

    @Inject ToolbarView.Presenter.Appearance appearance;
    @Inject DataLinkPresenterFactory dataLinkPresenterFactory;
    @Inject DiskResourceSelectorFieldFactory drSelectorFactory;
    @Inject EventBus eventBus;
    @Inject DiskResourceServiceFacade drFacade;
    @Inject UserInfo userInfo;

    @Inject DiskResourceAutoBeanFactory drAbFactory;

    @Inject HTPathListAutomationDialog.HTPathListAutomationAppearance htAppearance;
    @Inject DiskResourceUtil diskResourceUtil;

    PermIdRequestUserServiceFacade prFacade =
            ServicesInjector.INSTANCE.getPermIdRequestUserServiceFacade();

    @Inject DiskResourceErrorAutoBeanFactory drFactory;
    FileEditorServiceFacade feFacade;

    @Inject IplantAnnouncer announcer;
    @Inject AsyncProviderWrapper<TabFileConfigDialog> tabFileConfigDlgProvider;
    @Inject AsyncProviderWrapper<CreateFolderDialog> createFolderDlgProvider;
    @Inject AsyncProviderWrapper<CreateNcbiSraFolderStructureDialog> createNcbiSraDlgProvider;

    private final GenomeSearchDialog genomeSearchView;
    private final BulkMetadataDialogFactory bulkMetadataViewFactory;
    final private GenomeAutoBeanFactory gFactory;
    private final DiskResourceView.Presenter parentPresenter;
    private final ToolbarView view;
    private final HTPathListAutomationDialogFactory htPathListAutomationViewFactory;
    private HandlerManager handlerManager;
    HTPathListAutomationDialog dialog;


    Logger LOG = Logger.getLogger(ToolbarViewPresenterImpl.class.getSimpleName());

    @Inject
    ToolbarViewPresenterImpl(final ToolbarViewFactory viewFactory,
                             GenomeSearchDialog genomeSearchView,
                             BulkMetadataDialogFactory bulkMetadataViewFactory,
                             HTPathListAutomationDialogFactory htPathListAutomationViewFactory,
                             GenomeAutoBeanFactory gFactory,
                             @Assisted DiskResourceView.Presenter parentPresenter) {
        this.parentPresenter = parentPresenter;
        this.view = viewFactory.create(this);
        this.bulkMetadataViewFactory = bulkMetadataViewFactory;
        this.htPathListAutomationViewFactory = htPathListAutomationViewFactory;
        this.feFacade = ServicesInjector.INSTANCE.getFileEditorServiceFacade();
        view.addSimpleDownloadSelectedHandler(this);
        this.genomeSearchView = genomeSearchView;
        this.genomeSearchView.setPresenter(this);
        this.gFactory = gFactory;
    }

    @Override
    public ToolbarView getView() {
        return view;
    }

    @Override
    public void onCreateNewDelimitedFileSelected() {
        tabFileConfigDlgProvider.get(new AsyncCallback<TabFileConfigDialog>() {
            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(TabFileConfigDialog dialog) {
                dialog.addOkButtonSelectHandler(selectEvent -> {
                    TabularFileViewerWindowConfig config = ConfigFactory.newTabularFileViewerWindowConfig();
                    config.setEditing(true);
                    config.setVizTabFirst(true);
                    config.setSeparator(dialog.getSeparator());
                    config.setColumns(dialog.getNumberOfColumns());
                    config.setContentType(MimeType.PLAIN);
                    eventBus.fireEvent(new CreateNewFileEvent(config));
                });

                dialog.show();
            }
        });
    }

    @Override
    public void onCreateNewFileSelected(final Folder selectedFolder, final MimeType mimeType) {
        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
        config.setEditing(true);
        config.setParentFolder(selectedFolder);
        config.setContentType(mimeType);
        eventBus.fireEvent(new CreateNewFileEvent(config));
    }

    @Override
    public void onCreateNewFolderSelected(Folder selectedFolder) {
        if(selectedFolder == null) {
            Folder parent = drAbFactory.folder().as();
            parent.setPath(userInfo.getHomePath());
            selectedFolder = parent;
        }
        Folder finalSelectedFolder = selectedFolder;
        createFolderDlgProvider.get(new AsyncCallback<CreateFolderDialog>() {
            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(CreateFolderDialog dialog) {
                dialog.show(finalSelectedFolder);
                dialog.addOkButtonSelectHandler(selectEvent -> ensureHandlers().fireEvent(new CreateNewFolderSelected(finalSelectedFolder, dialog.getFolderName())));
            }
        });
    }

    @Override
    public void onCreateNcbiSraFolderStructure(final Folder selectedFolder) {
        createNcbiSraDlgProvider.get(new AsyncCallback<CreateNcbiSraFolderStructureDialog>() {
            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(CreateNcbiSraFolderStructureDialog dialog) {
                dialog.addOkButtonSelectHandler(selectEvent -> {
                    if (dialog.isValid()) {
                        ensureHandlers().fireEvent(new CreateNcbiSraFolderStructureSelected(selectedFolder,
                                                                                            dialog.getProjectTxt(),
                                                                                            dialog.getBioSampNum(),
                                                                                            dialog.getLibNum()));
                        dialog.hide();
                    }
                });
                dialog.addCancelButtonSelectHandler(selectEvent -> dialog.hide());
                dialog.show(selectedFolder);
            }
        });
    }

    @Override
    public void onCreateNewPathListSelected() {
        PathListWindowConfig config = ConfigFactory.newPathListWindowConfig();
        config.setEditing(true);
        eventBus.fireEvent(new CreateNewFileEvent(config));
    }

    @Override
    public void onCreatePublicLinkSelected(final List<DiskResource> selectedDiskResources) {
        // FIXME Do not fire dialog from presenter. Do so from the view.
        IPlantDialog dlg = new IPlantDialog(true);
        dlg.setPredefinedButtons(OK);
        dlg.setHeading(appearance.manageDataLinks());
        dlg.setHideOnButtonClick(true);
        dlg.setWidth(appearance.manageDataLinksDialogWidth());
        dlg.setHeight(appearance.manageDataLinksDialogHeight());
        dlg.setOkButtonText(appearance.done());
        DataLinkView.Presenter dlPresenter =
                dataLinkPresenterFactory.createDataLinkPresenter(selectedDiskResources);
        dlPresenter.go(dlg);
        dlg.addHelp(new HTML(appearance.manageDataLinksHelp()));
        dlg.show();
    }

    @Override
    public void onEditFileSelected(final List<DiskResource> selectedDiskResources) {
        Preconditions.checkState(selectedDiskResources.size() == 1,
                                 "Only one file should be selected, but there are %i",
                                 selectedDiskResources.size());
        final DiskResource next = selectedDiskResources.iterator().next();
        Preconditions.checkState(next instanceof File, "Selected item should be a file, but is not.");
        Preconditions.checkState(PermissionValue.own.equals(next.getPermission())
                                 || PermissionValue.write.equals(next.getPermission()),
                                 "User should have either own or write permissions for the selected item");

        eventBus.fireEvent(new ShowFilePreviewEvent((File)next, null));
    }

    @Override
    public void onOpenNewWindowAtLocationSelected(final Folder selectedFolder) {
        final String selectedFolderPath = selectedFolder == null ? null : selectedFolder.getPath();
        OpenFolderEvent openFolderEvent = new OpenFolderEvent(selectedFolderPath, true);
        eventBus.fireEvent(openFolderEvent);
    }

    @Override
    public void onOpenNewWindowSelected() {
        OpenFolderEvent openFolderEvent = new OpenFolderEvent(null, true);
        eventBus.fireEvent(openFolderEvent);
    }

    @Override
    public void onOpenTrashFolderSelected() {
        parentPresenter.selectTrashFolder();
    }

    @Override
    public void onSimpleDownloadSelected(SimpleDownloadSelected event) {
        eventBus.fireEvent(new RequestSimpleDownloadEvent(event.getSelectedDiskResources(),
                                                          event.getSelectedFolder()));
    }

    @Override
    public void onImportFromCoge() {
        view.openViewForGenomeSearch(genomeSearchView);

    }

    @Override
    public void searchGenomeInCoge(String searchTerm) {
        feFacade.searchGenomesInCoge(searchTerm, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                AutoBean<GenomeList> genomesBean =
                        AutoBeanCodex.decode(gFactory, GenomeList.class, result);
                GenomeList list = genomesBean.as();
                genomeSearchView.loadResults(list.getGenomes());
            }

            @Override
            public void onFailure(Throwable caught) {
                genomeSearchView.unmask();
                IplantAnnouncer.getInstance()
                               .schedule(new ErrorAnnouncementConfig(appearance.cogeSearchError()));

            }
        });

    }

    @Override
    public void importGenomeFromCoge(Integer id) {
        feFacade.importGenomeFromCoge(id, true, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                IplantAnnouncer.getInstance()
                               .schedule(new ErrorAnnouncementConfig(appearance.cogeImportGenomeError()));

            }

            @Override
            public void onSuccess(String result) {
                MessageBox amb =
                        new MessageBox(SafeHtmlUtils.fromTrustedString(appearance.importFromCoge()), SafeHtmlUtils.fromTrustedString(appearance.cogeImportGenomeSucess()));
                amb.setIcon(MessageBox.ICONS.info());
                amb.show();
            }

        });

    }

    @Override
    public void onBulkMetadataSelected(final BulkMetadataDialog.BULK_MODE mode) {
        drFacade.getMetadataTemplateListing(new AsyncCallback<List<MetadataTemplateInfo>>() {

            @Override
            public void onSuccess(List<MetadataTemplateInfo> templates) {
                BulkMetadataDialog bmd = bulkMetadataViewFactory.create(drSelectorFactory,
                                                                        parentPresenter.getSelectedDiskResources()
                                                                                       .get(0)
                                                                                       .getPath(),
                                                                        templates,
                                                                        mode);
                bmd.setPresenter(ToolbarViewPresenterImpl.this);
                view.openViewBulkMetadata(bmd);
            }

            @Override
            public void onFailure(Throwable caught) {
                IplantAnnouncer.getInstance()
                               .schedule(new ErrorAnnouncementConfig(appearance.templatesError()));

            }
        });

    }

    @Override
    public void submitBulkMetadataFromExistingFile(String filePath,
                                                   String destFolder) {
        drFacade.setBulkMetadataFromFile(filePath, destFolder, new DataCallback<String>() {
            @Override
            public void onFailure(Integer statusCode, Throwable caught) {
                announcer.schedule(new ErrorAnnouncementConfig(appearance.bulkMetadataError()));
            }

            @Override
            public void onSuccess(String result) {
                announcer.schedule(new SuccessAnnouncementConfig(appearance.bulkMetadataSuccess()));
            }
        });
    }

    @Override
    public void onDoiRequest(String uuid) {
        prFacade.requestPermId(uuid, PermanentIdRequestType.DOI, new DataCallback<String>() {

            @Override
            public void onFailure(Integer statusCode, Throwable caught) {
                IplantAnnouncer.getInstance()
                               .schedule(new ErrorAnnouncementConfig(appearance.doiRequestFail()));
                ErrorHandler.post(appearance.doiRequestFail(),caught);
            }

            @Override
            public void onSuccess(String result) {
                IplantAnnouncer.getInstance()
                               .schedule(new SuccessAnnouncementConfig(appearance.doiRequestSuccess()));

            }
        });
    }

    @Override
    public void onAutomateHTPathList() {
        drFacade.getInfoTypes(new DataCallback<List<InfoType>>() {

            @Override
            public void onFailure(Integer statusCode, Throwable arg0) {
                ErrorHandler.post(arg0);
            }

            @Override
            public void onSuccess(List<InfoType> infoTypes) {
                dialog = htPathListAutomationViewFactory.create(drSelectorFactory, infoTypes);
                dialog.setSize(htAppearance.dialogWidth(), htAppearance.dialogHeight());
                dialog.addOkButtonSelectHandler(event -> {
                    if (dialog.isValid()) {
                        HTPathListRequest request = dialog.getRequest();
                        requestHTPathListCreation(dialog, request);
                    } else {
                        showHTProcessingError();
                    }
                });
                dialog.addCancelButtonSelectHandler(event -> {
                    dialog.hide();
                });
                dialog.show();
            }
        });

    }

    protected void showHTProcessingError() {
        AlertMessageBox amb =
                new AlertMessageBox(htAppearance.heading(), htAppearance.validationMessage());
        amb.show();
    }


    protected void requestHTPathListCreation(HTPathListAutomationDialog dialog,
                                           HTPathListRequest request) {
        dialog.mask(htAppearance.processing());
        drFacade.requestHTPathlistFile(request, new DataCallback<File>() {
            @Override
            public void onFailure(Integer statusCode, Throwable exception) {
                ErrorHandler.post(htAppearance.requestFailed(), exception);
            }

            @Override
            public void onSuccess(File result) {
                dialog.hide();
                announcer.schedule(new SuccessAnnouncementConfig(htAppearance.requestSuccess()));
                eventBus.fireEvent(new ShowFilePreviewEvent(result, null));
            }
        });
    }

    HandlerManager ensureHandlers() {
        return handlerManager == null ? handlerManager = createHandlerManager() : handlerManager;
    }

    HandlerManager createHandlerManager() {
        return new HandlerManager(this);
    }

    @Override
    public HandlerRegistration addCreateNewFolderSelectedHandler(CreateNewFolderSelected.CreateNewFolderSelectedHandler handler) {
        return ensureHandlers().addHandler(CreateNewFolderSelected.TYPE, handler);
    }

    @Override
    public HandlerRegistration addCreateNcbiSraFolderStructureSelectedHandler(
            CreateNcbiSraFolderStructureSelected.CreateNcbiSraFolderStructureSelectedHandler handler) {
        return ensureHandlers().addHandler(CreateNcbiSraFolderStructureSelected.TYPE, handler);
    }
}
