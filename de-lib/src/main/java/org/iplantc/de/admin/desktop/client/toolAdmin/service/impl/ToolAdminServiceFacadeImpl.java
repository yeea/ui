package org.iplantc.de.admin.desktop.client.toolAdmin.service.impl;

import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.DELETE;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.GET;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.PATCH;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.POST;

import org.iplantc.de.admin.desktop.client.toolAdmin.service.ToolAdminServiceFacade;
import org.iplantc.de.client.models.tool.Tool;
import org.iplantc.de.client.models.tool.ToolAutoBeanFactory;
import org.iplantc.de.client.models.tool.ToolImportUpdateRequest;
import org.iplantc.de.client.models.tool.ToolImportUpdateRequestList;
import org.iplantc.de.client.services.converters.StringToVoidCallbackConverter;
import org.iplantc.de.shared.services.DiscEnvApiService;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;

import java.util.List;

/**
 * Created by aramsey on 10/28/15.
 */


public class ToolAdminServiceFacadeImpl implements ToolAdminServiceFacade {

    private final String TOOLS = "org.iplantc.services.tools";
    private final String TOOLS_ADMIN = "org.iplantc.services.admin.tools";

    @Inject
    public ToolAdminServiceFacadeImpl() {
    }

    @Inject
    private ToolAutoBeanFactory factory;
    @Inject
    private DiscEnvApiService deService;

    @Override
    public void getTools(String searchTerm, AsyncCallback<List<Tool>> callback) {
        String address = TOOLS + "?search=" + URL.encodeQueryString(searchTerm);

        ServiceCallWrapper wrapper = new ServiceCallWrapper(GET, address);
        deService.getServiceData(wrapper, new ToolListCallbackConverter(callback, factory));
    }

    @Override
    public void getToolDetails(String toolId, AsyncCallback<ToolImportUpdateRequest> callback) {
        String address = TOOLS + "/" + toolId;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(GET, address);
        deService.getServiceData(wrapper, new ToolImportUpdateRequestCallbackConverter(callback, factory));
    }

    @Override
    public void addTool(ToolImportUpdateRequestList requestList, AsyncCallback<Void> callback) {
        String address = TOOLS_ADMIN;

        final Splittable encode = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(requestList));
        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, encode.getPayload());
        deService.getServiceData(wrapper, new StringToVoidCallbackConverter(callback));
    }

    @Override
    public void updateTool(ToolImportUpdateRequest request, AsyncCallback<Void> callback) {

        String toolId = request.getId();
        String address = TOOLS_ADMIN + "/" + toolId;

        final Splittable encode = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(request));
        ServiceCallWrapper wrapper = new ServiceCallWrapper(PATCH, address, encode.getPayload());
        deService.getServiceData(wrapper, new StringToVoidCallbackConverter(callback));
    }

    @Override
    public void deleteTool(String toolId, AsyncCallback<Void> callback) {
        String address = TOOLS_ADMIN + "/" + toolId;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(DELETE, address);
        deService.getServiceData(wrapper, new StringToVoidCallbackConverter(callback));
    }


}