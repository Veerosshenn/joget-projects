package org.joget.marketplace;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

public class ColorPicker extends Element implements FormBuilderPaletteElement {
    public ColorPicker() {
    }

    public String getName() {
        return "Color Picker Field";
    }

    public String getVersion() {
        return "8.0.0";
    }

    public String getDescription() {
        return "Color Picker Field Element";
    }

    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "colorPicker.ftl";
        String value = FormUtil.getElementPropertyValue(this, formData);
        if (value != null && !value.isEmpty()) {
            dataModel.put("value", value);
        }

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;
        String id = this.getPropertyString("id");
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if (value != null) {
                FormRow result = new FormRow();
                result.setProperty(id, value);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getFormBuilderTemplate() {
        return "<label class='label'>colorPicker</label><input type='text' />";
    }

    public String getLabel() {
        return "Color Picker";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(this.getClass().getName(), "/properties/form/colorPicker.json", (Object[])null, true, "message/form/colorPicker");
    }

    public String getFormBuilderCategory() {
        return "Marketplace";
    }

    public int getFormBuilderPosition() {
        return 100;
    }

    public String getFormBuilderIcon() {
        return "/plugin/org.joget.marketplace.ColorPicker/images/colorPicker_icon.png";
    }
}
