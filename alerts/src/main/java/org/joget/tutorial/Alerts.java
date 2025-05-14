package org.joget.tutorial;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class Alerts extends UserviewMenu {

    private final static String MESSAGE_PATH = "messages/Alerts";

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-exclamation-triangle\"></i>";
    }

    @Override
    public String getRenderPage() {
        Map<String, Object> model = new HashMap<>();
        model.put("request", getRequestParameters());
        model.put("element", this);

        String icon = getPropertyString("icon");
        String title = getPropertyString("title");
        String message = getPropertyString("message");
        String customContent = getPropertyString("customContent");
        String alertType = getPropertyString("alertType");

        model.put("icon", icon);
        model.put("title", title);
        model.put("message", message);
        model.put("customContent", customContent);
        model.put("alertType", alertType);

        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        String content = pluginManager.getPluginFreeMarkerTemplate(model, getClass().getName(), "/templates/Alerts.ftl", MESSAGE_PATH);

        return content;
    }


    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getName() {
        return AppPluginUtil.getMessage("org.joget.tutorial.Alerts.pluginName", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.tutorial.Alerts.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getLabel() {
        return AppPluginUtil.getMessage("org.joget.tutorial.Alerts.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/Alerts.json", null, true, MESSAGE_PATH);
    }
}
