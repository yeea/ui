package org.iplantc.de.tools.client.views.requests;

import org.iplantc.de.client.DEClientConstants;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.commons.client.validators.DiskResourceNameValidator;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.dom.client.Element;

import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;

/**
 * This class manages a single upload for the DE backend services. On submission, it posts a
 * multipart document. The first part of the document is a form containing three fields.
 * 
 * <lu> <li>'file' the name of the file being uploaded</li> <li>'user' the name of the DE user</li> <li>
 * 'dest' the path to the DE user's home collection in iRODS</li> </lu>
 * 
 * The second part of the document contains the contents of the file.
 * 
 * TODO move this class to ui-commons and consider converting the simple upload form to using it.
 */
public final class UploadForm extends Composite {

    interface Binder extends UiBinder<Widget, UploadForm> {
    }

    private static final Binder BINDER = GWT.create(Binder.class);

    @UiField
    FormPanel form;

    @UiField
    FileUpload fileField;

    @UiField
    Hidden userField;

    @UiField
    Hidden destinationField;

    @UiField
    VerticalPanel con;

    private final DiskResourceNameValidator diskResourceNameValidator = new DiskResourceNameValidator();
    private final NewToolRequestFormView.NewToolRequestFormViewAppearance appearance =
            GWT.create(NewToolRequestFormView.NewToolRequestFormViewAppearance.class);

    DEClientConstants constants = GWT.create(DEClientConstants.class);

    /**
     * the constructor
     */
    public UploadForm() {
        initWidget(BINDER.createAndBindUi(this));
        userField.setValue(UserInfo.getInstance().getUsername());
        destinationField.setValue(UserInfo.getInstance().getHomePath());
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
    }

    @UiHandler("form")
    void onSubmintComplete(final FormPanel.SubmitCompleteEvent event) {
        fireEvent(event);
    }

    /**
     * @see Composite#setEnabled(boolean)
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        fileField.setEnabled(enabled);
    }

    public HandlerRegistration addSubmitCompleteHandler(final FormPanel.SubmitCompleteHandler handler) {
        return addHandler(handler, SubmitCompleteEvent.getType());
    }

    public void clear() {
        form.clear();
    }

    public void reset() {
        form.reset();
    }

    public String getValue() {
        return appearance.getFileName(fileField.getFilename());
    }

    public void submit() {
        form.submit();
    }


    public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
        return fileField.addChangeHandler(handler);
    }

    public HandlerRegistration addKeyUpHandler(final KeyUpHandler handler) {
        return fileField.addKeyUpHandler(handler);
    }

    /**
     * Forces validation of the file name and returns whether or not it passed validation.
     */
    public boolean isValid() {
        String err = diskResourceNameValidator.validateAndReturnError(fileField.getFilename());
        if (!Strings.isNullOrEmpty(err)) {
            AlertMessageBox amb = new AlertMessageBox(appearance.invalidFileName(),
                                                      appearance.fileNameValidationMsg());
            amb.show();
            return false;
        }

        if (getSize(fileField.getElement()) > constants.maxFileSizeForSimpleUpload()) {
            AlertMessageBox amb = new AlertMessageBox(appearance.maxFileSizeExceed(),
                                                      appearance.fileSizeViolation(appearance.getFileName(
                                                              fileField.getFilename())));
            amb.show();
            return false;
        }

        return true;

    }

    public static native int getSize(Element element) /*-{
        input = element;
        if (!input) {
            return 0;
        }
        else if (!input.files) {
            return 0;
        }
        else if (!input.files[0]) {
            return 0;
        }
        else {
            file = input.files[0];
            return file.size;
        }
    }-*/;
}

