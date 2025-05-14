package org.joget.tutorial;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.commons.util.LogUtil;

public class ColorDatalistFormatter extends DataListColumnFormatDefault{

    private final static String MESSAGE_PATH = "messages/ColorDatalistFormatter";

    public String getName() {
        return "Color Datalist Formatter";
    }

    public String getVersion() {
        return "7.0.0";
    }

    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.tutorial.ColorDatalistFormatter.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String result = (String) value;

        if (result != null && !result.isEmpty()) {
            try {
                String fontColor = "white";

                if(getPropertyString("fontColor") != null && !getPropertyString("fontColor").isEmpty()){
                    fontColor = getPropertyString("fontColor");
                }

                boolean isShowLabel = true;
                if(getPropertyString("showLabel") != null){
                    isShowLabel = Boolean.parseBoolean(getPropertyString("showLabel"));
                }

                result = "";

                //suport for multi values
                for (String v : value.toString().split(";")) {
                    if (!v.isEmpty()) {
                        String rowValue = v;

                        String rowStyle = "<p style=\"margin: 0; padding: 3px; color:" + fontColor + ";background-color:" + rowValue + ";\">";

                        if (isShowLabel) {
                            result += rowStyle + rowValue + "</p> ";
                        }else{
                            result += rowStyle+ "&nbsp;</p> ";
                        }

                    }
                }
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
        }
        return result;
    }

    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.tutorial.ColorDatalistFormatter.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ColorDatalistFormatter.json", null, true, MESSAGE_PATH);
    }
}
