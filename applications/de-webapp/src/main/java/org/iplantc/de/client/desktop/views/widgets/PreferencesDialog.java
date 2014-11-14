package org.iplantc.de.client.desktop.views.widgets;

import org.iplantc.de.apps.widgets.client.view.editors.validation.AnalysisOutputValidator;
import org.iplantc.de.client.KeyBoardShortcutConstants;
import org.iplantc.de.client.models.UserSettings;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.views.gxt3.dialogs.IPlantDialog;
import org.iplantc.de.diskResource.client.gin.factory.DiskResourceSelectorFieldFactory;
import org.iplantc.de.diskResource.client.views.widgets.FolderSelectorField;
import org.iplantc.de.resources.client.messages.IplantContextualHelpStrings;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;
import org.iplantc.de.shared.DeModule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.AbstractHtmlLayoutContainer.HtmlData;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.validator.MaxLengthValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sriram, jstroot
 */
public class PreferencesDialog extends IPlantDialog implements Editor<UserSettings> {

    public interface HtmlLayoutContainerTemplate extends XTemplates {
        @XTemplate(source = "PreferencesHelpTemplate.html")
        SafeHtml getTemplate();
    }

    @UiTemplate("PreferencesView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalLayoutContainer, PreferencesDialog> { }

    interface EditorDriver extends SimpleBeanEditorDriver<UserSettings, PreferencesDialog>{}

    @UiField @Ignore VerticalLayoutContainer container;
    @UiField @Ignore VerticalLayoutContainer kbContainer;
    @UiField @Ignore VerticalLayoutContainer prefContainer;

    @UiField TextField analysesShortCut;
    @UiField TextField appsShortCut;
    @UiField CheckBox rememberLastPath;
    @UiField CheckBox enableEmailNotification;
    @UiField CheckBox saveSession;
    @UiField TextField closeShortCut;
    @UiField TextField dataShortCut;
    @UiField(provided = true) FolderSelectorField defaultOutputFolder;
    @UiField TextField notifyShortCut;
    private final IplantDisplayStrings displayStrings;
    private final IplantContextualHelpStrings helpStrings;
    private final KeyBoardShortcutConstants KB_CONSTANTS;
    private final TextButton defaultsBtn;
    private final Map<TextField, String> kbMap;

    private final EditorDriver editorDriver = GWT.create(EditorDriver.class);
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private UserSettings flushedValue;
    private UserSettings usValue;

    @Inject UserSettings us;

    @Inject
    PreferencesDialog(final DiskResourceSelectorFieldFactory folderSelectorFieldFactory,
                      final IplantDisplayStrings displayStrings,
                      final IplantContextualHelpStrings helpStrings,
                      final KeyBoardShortcutConstants kbConstants) {
        super(true);
        this.defaultOutputFolder = folderSelectorFieldFactory.defaultFolderSelector();
        this.displayStrings = displayStrings;
        this.helpStrings = helpStrings;
        this.KB_CONSTANTS = kbConstants;
        setHeadingText(displayStrings.preferences());
        VerticalLayoutContainer vlc = uiBinder.createAndBindUi(this);
        kbMap = new HashMap<>();
        appsShortCut.addValidator(new MaxLengthValidator(1));
        dataShortCut.addValidator(new MaxLengthValidator(1));
        analysesShortCut.addValidator(new MaxLengthValidator(1));
        notifyShortCut.addValidator(new MaxLengthValidator(1));
        closeShortCut.addValidator(new MaxLengthValidator(1));
        defaultOutputFolder.addValidator(new AnalysisOutputValidator());
        defaultOutputFolder.addValueChangeHandler(new ValueChangeHandler<Folder>() {

            @Override
            public void onValueChange(ValueChangeEvent<Folder> event) {
                defaultOutputFolder.validate(false);
            }
        });
        populateKbMap();


        getButton(PredefinedButton.OK).setText(displayStrings.done());
        defaultsBtn = new TextButton(displayStrings.restoreDefaults());
        defaultsBtn.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                enableEmailNotification.setValue(true);
                rememberLastPath.setValue(true);
                saveSession.setValue(true);
                appsShortCut.setValue(KB_CONSTANTS.appsKeyShortCut());
                dataShortCut.setValue(KB_CONSTANTS.dataKeyShortCut());
                analysesShortCut.setValue(KB_CONSTANTS.analysisKeyShortCut());
                notifyShortCut.setValue(KB_CONSTANTS.notifyKeyShortCut());
                closeShortCut.setValue(KB_CONSTANTS.closeKeyShortCut());
                final Folder systemDefaultOutputFolder = usValue.getSystemDefaultOutputFolder();
                defaultOutputFolder.setValue(systemDefaultOutputFolder);
            }
        });
        getButtonBar().insert(defaultsBtn, 0);
        addHelp(constructHelpView());
        add(vlc);
        editorDriver.initialize(this);
        ensureDebugId(DeModule.PreferenceIds.PREFERENCES_DLG);
    }

    @Ignore
    @Override
    public TextButton getOkButton() {
        return getButton(PredefinedButton.OK);
    }

    @Ignore
    public UserSettings getValue() {
        return flushedValue;
    }

    public void initAndShow(final UserSettings userSettings) {
        this.usValue = userSettings;
        editorDriver.edit(userSettings);
        show();
    }

    public boolean isValid() {
        boolean valid = defaultOutputFolder.validate(false) && appsShortCut.isValid() && dataShortCut.isValid()
                            && analysesShortCut.isValid() && notifyShortCut.isValid() && closeShortCut.isValid();

        if (valid) {
            populateKbMap();
            resetKbFieldErrors();
            for (TextField ks : kbMap.keySet()) {
                for (TextField sc : kbMap.keySet()) {
                    if (ks != sc) {
                        if (kbMap.get(ks).equals(kbMap.get(sc))) {
                            ks.markInvalid(displayStrings.duplicateShortCutKey(kbMap.get(ks)));
                            sc.markInvalid(displayStrings.duplicateShortCutKey(kbMap.get(ks)));
                            valid = false;
                        }
                    }
                }
            }
        }
        return valid;
    }

    @Override
    protected void onButtonPressed(TextButton button) {
        if (button == getButton(PredefinedButton.OK)) {
            UserSettings value = editorDriver.flush();
            if (!editorDriver.hasErrors() && isValid()) {
                this.flushedValue = value;
                super.onButtonPressed(button);
            } else {
                IplantAnnouncer.getInstance()
                               .schedule(new ErrorAnnouncementConfig(displayStrings.publicSubmitTip()));
            }


        } else if (button == defaultsBtn) {
            enableEmailNotification.setValue(true);
            rememberLastPath.setValue(true);
            saveSession.setValue(true);
            appsShortCut.setValue(KB_CONSTANTS.appsKeyShortCut());
            dataShortCut.setValue(KB_CONSTANTS.dataKeyShortCut());
            analysesShortCut.setValue(KB_CONSTANTS.analysisKeyShortCut());
            notifyShortCut.setValue(KB_CONSTANTS.notifyKeyShortCut());
            closeShortCut.setValue(KB_CONSTANTS.closeKeyShortCut());
            defaultOutputFolder.setValue(us.getSystemDefaultOutputFolder());
            if (isValid()) {
                super.onButtonPressed(button);
            }
        } else if (button == getButton(PredefinedButton.CANCEL)) {
            super.onButtonPressed(button);
        }

    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        getButton(PredefinedButton.OK).ensureDebugId(baseID + DeModule.PreferenceIds.DONE);
        getButton(PredefinedButton.CANCEL).ensureDebugId(baseID + DeModule.PreferenceIds.CANCEL);
        defaultsBtn.ensureDebugId(baseID + DeModule.PreferenceIds.DEFAULTS_BTN);

        enableEmailNotification.ensureDebugId(baseID + DeModule.PreferenceIds.EMAIL_NOTIFICATION);
        rememberLastPath.ensureDebugId(baseID + DeModule.PreferenceIds.REMEMBER_LAST_PATH);
        saveSession.ensureDebugId(baseID + DeModule.PreferenceIds.SAVE_SESSION);
        defaultOutputFolder.ensureDebugId(baseID + DeModule.PreferenceIds.DEFAULT_OUTPUT_FOLDER);

        appsShortCut.ensureDebugId(baseID + DeModule.PreferenceIds.APPS_SC);
        dataShortCut.ensureDebugId(baseID + DeModule.PreferenceIds.DATA_SC);
        analysesShortCut.ensureDebugId(baseID + DeModule.PreferenceIds.ANALYSES_SC);
        notifyShortCut.ensureDebugId(baseID + DeModule.PreferenceIds.NOTIFICATION_SC);
        closeShortCut.ensureDebugId(baseID + DeModule.PreferenceIds.CLOSE_SC);
    }

    @UiHandler({"appsShortCut", "dataShortCut", "analysesShortCut", "notifyShortCut", "closeShortCut"})
    void onKeyPress(KeyPressEvent event) {
        TextField fld = (TextField) event.getSource();
        int code = event.getNativeEvent().getCharCode();
        if ((code > 96 && code <= 122)) {
            fld.clear();
            fld.setValue((event.getCharCode() + "").toUpperCase());
            fld.setText((event.getCharCode() + "").toUpperCase());
            fld.setCursorPos(1);
            fld.focus();
        } else if ((code > 47 && code <= 57) || (code > 64 && code <= 90)) {
            fld.clear();
            fld.setValue(event.getCharCode() + "");
            fld.setText(event.getCharCode() + "");
            fld.setCursorPos(1);
            fld.focus();
        }
        if (code != 0) {
            event.preventDefault();
        }

    }

    private Widget constructHelpView() {
        HtmlLayoutContainerTemplate templates = GWT.create(HtmlLayoutContainerTemplate.class);
        HtmlLayoutContainer c = new HtmlLayoutContainer(templates.getTemplate());
        c.add(new HTML(displayStrings.notifyemail()), new HtmlData(".emailHeader"));
        c.add(new HTML(helpStrings.notifyemailHelp()), new HtmlData(".emailHelp"));
        c.add(new HTML(displayStrings.rememberFileSectorPath()), new HtmlData(".filePathHeader"));
        c.add(new HTML(helpStrings.rememberFileSectorPathHelp()), new HtmlData(".filePathHelp"));
        c.add(new HTML(displayStrings.saveSession()), new HtmlData(".saveSessionHeader"));
        c.add(new HTML(helpStrings.saveSessionHelp()), new HtmlData(".saveSessionHelp"));
        c.add(new HTML(displayStrings.defaultOutputFolder()), new HtmlData(".defaultOp"));
        c.add(new HTML(helpStrings.defaultOutputFolderHelp()), new HtmlData(".defaultOpHelp"));
        return c.asWidget();
    }

    private void populateKbMap() {
        kbMap.put(appsShortCut, appsShortCut.getValue());
        kbMap.put(dataShortCut, dataShortCut.getValue());
        kbMap.put(analysesShortCut, analysesShortCut.getValue());
        kbMap.put(notifyShortCut, notifyShortCut.getValue());
        kbMap.put(closeShortCut, closeShortCut.getValue());
    }

    private void resetKbFieldErrors() {
        for (TextField ks : kbMap.keySet()) {
            ks.clearInvalid();
        }
    }

}
