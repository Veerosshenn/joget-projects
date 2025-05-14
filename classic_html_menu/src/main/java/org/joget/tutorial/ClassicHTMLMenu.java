package org.joget.tutorial;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.plugin.base.PluginManager;

public class ClassicHTMLMenu extends UserviewMenu{
    private final static String MESSAGE_PATH = "messages/ClassicHTMLMenu";

    public String getName() {
        return "Classic HTML Menu";
    }
    public String getVersion() {
        return "7.0.0";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.tutorial.classicHTMLMenu.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.tutorial.classicHTMLMenu.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ClassicHTMLMenu.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String getCategory() {
        return "Tutorial";
    }

    @Override
    public String getIcon() {
        return "/plugin/org.joget.apps.userview.lib.HtmlPage/images/grid_icon.gif";
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
    public String getRenderPage() {
        Map model = new HashMap();
        model.put("request", getRequestParameters());
        model.put("element", this);

        String code = "";
        code = getPropertyString("code");

        model.put("code", code);

        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(model, getClass().getName(), "/templates/classicHTMLMenu.ftl", MESSAGE_PATH);
        return content;
    }
}
