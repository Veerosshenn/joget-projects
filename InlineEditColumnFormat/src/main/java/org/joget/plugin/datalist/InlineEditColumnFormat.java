package org.joget.plugin.datalist;

import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.*;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.service.FormService;
import org.joget.apps.userview.model.PwaOfflineResources;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;

public class InlineEditColumnFormat extends DataListColumnFormatDefault implements PluginWebSupport, PwaOfflineResources {

    private final static String MESSAGE_PATH = "messages/inlineEditColumnFormat";
    private String injectedHtml = "";

    @Override
    public String getName() {
        return AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.name", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.description", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getLabel() {
        return AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.label", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/inlineEditColumnFormat.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        String result = "";

        if (value != null) {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            String datalistId = dataList.getId();
            String injectionKey = "inlineedit_" + datalistId;

            if (request != null && request.getAttribute(this.getClassName() + "-" + injectionKey) == null) {
                this.injectedHtml += "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/plugin/" + getClassName() + "/css/inlineEditColumnFormat.css\" />\n";
                this.injectedHtml += "<script src=\"" + request.getContextPath() + "/plugin/" + getClassName() + "/js/inlineEditColumnFormat.js\"></script>\n";
                this.injectedHtml += "<script>InlineEditColumnFormat = { url: '" + getServiceUrl(datalistId) + "' };</script>\n";
                request.setAttribute(this.getClassName() + "-" + injectionKey, true);
            }

            String rowId = null;
            if (row instanceof Map) {
                Object id = ((Map<?, ?>) row).get("id");
                if (id != null) rowId = id.toString();
            }

            Map<String, Object> props = getProperties();
            LogUtil.debug(getClassName(), "Plugin properties: " + props);
            String fieldType = (String) props.getOrDefault("fieldType", "text");
            String datePickerType = (String) props.getOrDefault("datePickerType", "dateTime");
            String format = (String) props.getOrDefault("format", "yyyy-MM-dd");
            String optionsJson = "[{\"value\":\"\",\"label\":\"Please configure options in Datalist Builder\"}]";
            if ("select".equals(fieldType)) {
                Object optionsObj = props.get("options");
                LogUtil.debug(getClassName(), "Options object: " + (optionsObj != null ? optionsObj.getClass() + " - " + optionsObj : "null"));
                try {
                    JSONArray optionsArray = new JSONArray();
                    if (optionsObj instanceof List) {
                        for (Object opt : (List<?>) optionsObj) {
                            if (opt instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> optionMap = (Map<String, String>) opt;
                                JSONObject option = new JSONObject();
                                String optValue = optionMap.getOrDefault("value", "").trim();
                                String optLabel = optionMap.getOrDefault("label", "").trim();
                                if (!optValue.isEmpty()) {
                                    option.put("value", optValue);
                                    option.put("label", optLabel.isEmpty() ? optValue : optLabel);
                                    optionsArray.put(option);
                                } else {
                                    LogUtil.warn(getClassName(), "Skipping option with empty value: " + optionMap);
                                }
                            } else {
                                LogUtil.warn(getClassName(), "Skipping invalid option: not a Map - " + opt);
                            }
                        }
                    } else if (optionsObj instanceof Object[]) {
                        for (Object opt : (Object[]) optionsObj) {
                            if (opt instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> optionMap = (Map<String, String>) opt;
                                JSONObject option = new JSONObject();
                                String optValue = optionMap.getOrDefault("value", "").trim();
                                String optLabel = optionMap.getOrDefault("label", "").trim();
                                if (!optValue.isEmpty()) {
                                    option.put("value", optValue);
                                    option.put("label", optLabel.isEmpty() ? optValue : optLabel);
                                    optionsArray.put(option);
                                } else {
                                    LogUtil.warn(getClassName(), "Skipping option with empty value: " + optionMap);
                                }
                            } else {
                                LogUtil.warn(getClassName(), "Skipping invalid option: not a Map - " + opt);
                            }
                        }
                    } else if (optionsObj != null) {
                        LogUtil.warn(getClassName(), "Options property is not a List or Array: " + optionsObj.getClass());
                    } else {
                        LogUtil.warn(getClassName(), "No options provided for fieldType: " + fieldType);
                    }
                    optionsJson = optionsArray.length() > 0 ? optionsArray.toString() : "[{\"value\":\"\",\"label\":\"Please configure options in Datalist Builder\"}]";
                    LogUtil.debug(getClassName(), "Serialized options JSON: " + optionsJson);
                } catch (Exception e) {
                    LogUtil.error(getClassName(), e, "Error serializing options");
                    optionsJson = "[{\"value\":\"\",\"label\":\"Error loading options\"}]";
                }
            }

            String formFieldId = column.getPropertyString("name");
            if (formFieldId == null || formFieldId.isEmpty()) {
                formFieldId = column.getName();
            }

            String escapedOptionsJson;
            try {
                escapedOptionsJson = StringEscapeUtils.escapeHtml4(optionsJson);
            } catch (NoClassDefFoundError e) {
                escapedOptionsJson = customEscapeHtml(optionsJson);
            }

            String displayValue = value.toString();

            result += "<span class='inline-edit-cell' " +
                    "data-row-id='" + (rowId != null ? rowId : "") + "' " +
                    "data-column-name='" + column.getName() + "' " +
                    "data-form-field-id='" + formFieldId + "' " +
                    "data-form-id='' " +
                    "data-field-type='" + fieldType + "' ";
            if ("date".equals(fieldType)) {
                result += "data-date-picker-type='" + datePickerType + "' " +
                        "data-format='" + format + "' ";
            }
            result += "data-options='" + escapedOptionsJson + "'>" +
                    displayValue + "</span>";
        }

        return injectedHtml + result;
    }

    private String customEscapeHtml(String input) {
        if (input == null) return "";
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '<': escaped.append("&lt;"); break;
                case '>': escaped.append("&gt;"); break;
                case '&': escaped.append("&amp;"); break;
                case '"': escaped.append("&quot;"); break;
                case '\'': escaped.append("&#39;"); break;
                default: escaped.append(c); break;
            }
        }
        return escaped.toString();
    }

    public String getServiceUrl(String id) {
        String url = WorkflowUtil.getHttpServletRequest().getContextPath() + "/web/json/plugin/" + this.getClassName() + "/service";
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String nonce = SecurityUtil.generateNonce(new String[]{"InlineEditColumnFormat", appDef.getAppId(), appDef.getVersion().toString(), id}, 4);

        try {
            url += "?_nonce=" + URLEncoder.encode(nonce, "UTF-8")
                    + "&_lid=" + URLEncoder.encode(id, "UTF-8")
                    + "&_appId=" + URLEncoder.encode(appDef.getAppId(), "UTF-8")
                    + "&_appVersion=" + URLEncoder.encode(appDef.getVersion().toString(), "UTF-8");
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error generating service URL");
        }

        return url;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String appId = request.getParameter("_appId");
        String appVersion = request.getParameter("_appVersion");
        String datalistId = request.getParameter("_lid");

        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) AppUtil.getApplicationContext().getBean("datalistDefinitionDao");
        DataListService dataListService = (DataListService) AppUtil.getApplicationContext().getBean("dataListService");
        FormDefinitionDao formDefDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");

        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        if (appDef == null || !SecurityUtil.verifyNonce(request.getParameter("_nonce"),
                new String[]{"InlineEditColumnFormat", appId, appVersion, datalistId})) {
            response.setStatus(403);
            writer.write(new JSONObject().put("status", "error").put("message", "Unauthorized access").toString());
            return;
        }

        DatalistDefinition def = datalistDefinitionDao.loadById(datalistId, appDef);
        if (def == null) {
            response.setStatus(404);
            writer.write(new JSONObject().put("status", "error").put("message", "Datalist not found").toString());
            return;
        }

        DataList dataList = dataListService.fromJson(def.getJson());
        String action = request.getParameter("action");

        if ("save".equalsIgnoreCase(action)) {
            try {
                String rowId = request.getParameter("rowId");
                String columnName = request.getParameter("columnName");
                String value = request.getParameter("value");

                String formId = null;
                DataListBinder binder = dataList.getBinder();
                if (binder != null && binder.getProperties() != null) {
                    formId = (String) binder.getProperties().get("formDefId");
                }

                JSONObject result = new JSONObject();

                if (formId == null || formId.isEmpty()) {
                    result.put("status", "error");
                    result.put("message", "Form ID could not be resolved.");
                    writer.write(result.toString());
                    return;
                }

                FormDefinition formDef = formDefDao.loadById(formId, appDef);
                if (formDef == null) {
                    result.put("status", "error");
                    result.put("message", "Form definition not found.");
                    writer.write(result.toString());
                    return;
                }

                String formFieldId = columnName;
                for (DataListColumn column : dataList.getColumns()) {
                    if (column.getName().equals(columnName)) {
                        String configField = column.getPropertyString("name");
                        if (configField != null && !configField.isEmpty()) {
                            formFieldId = configField;
                        }
                        break;
                    }
                }

                String tableName = "app_fd_" + formDef.getTableName();
                LogUtil.info(getClassName(), "Attempting to update table: " + tableName + ", field: " + formFieldId + ", rowId: " + rowId + ", value: " + value);

                String sql = "UPDATE " + tableName + " SET " + formFieldId + " = ? WHERE id = ?";
                LogUtil.info(getClassName(), "SQL: " + sql);

                javax.sql.DataSource dataSource = null;
                String[] dataSourceNames = {"setupDataSource", "dataSource", "jogetDataSource", "defaultDataSource", "appDataSource"};

                for (String dsName : dataSourceNames) {
                    try {
                        dataSource = (javax.sql.DataSource) AppUtil.getApplicationContext().getBean(dsName);
                        if (dataSource != null) {
                            LogUtil.info(getClassName(), "Found datasource: " + dsName);
                            break;
                        }
                    } catch (Exception e) {
                        LogUtil.debug(getClassName(), "Datasource " + dsName + " not found: " + e.getMessage());
                    }
                }

                if (dataSource == null) {
                    throw new RuntimeException("No datasource found. Tried: " + String.join(", ", dataSourceNames));
                }

                java.sql.Connection conn = null;
                java.sql.PreparedStatement stmt = null;
                try {
                    conn = dataSource.getConnection();
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, value);
                    stmt.setString(2, rowId);

                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        LogUtil.info(getClassName(), "Successfully updated " + rowsAffected + " row(s)");
                    } else {
                        LogUtil.warn(getClassName(), "No rows were updated for rowId: " + rowId);
                        java.sql.PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE id = ?");
                        checkStmt.setString(1, rowId);
                        java.sql.ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            throw new RuntimeException("Row with ID " + rowId + " does not exist in table " + tableName);
                        }
                        checkStmt.close();
                    }
                } catch (java.sql.SQLException sqlException) {
                    LogUtil.error(getClassName(), sqlException, "SQL Error: " + sqlException.getMessage());
                    throw new RuntimeException("Database error: " + sqlException.getMessage(), sqlException);
                } finally {
                    if (stmt != null) {
                        try { stmt.close(); } catch (Exception e) { LogUtil.debug(getClassName(), "Error closing statement: " + e.getMessage()); }
                    }
                    if (conn != null) {
                        try { conn.close(); } catch (Exception e) { LogUtil.debug(getClassName(), "Error closing connection: " + e.getMessage()); }
                    }
                }

                LogUtil.info(getClassName(), "Saved field [" + formFieldId + "] = " + value + " on row [" + rowId + "] for form [" + formId + "]");

                result.put("status", "success");
                result.put("message", AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.save.success", getClassName(), MESSAGE_PATH));
                writer.write(result.toString());
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "Error saving inline edit");
                response.setStatus(500);
                writer.write(new JSONObject()
                        .put("status", "error")
                        .put("message", AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.error.save", getClassName(), MESSAGE_PATH))
                        .toString());
            }
        } else if ("getTranslations".equalsIgnoreCase(action)) {
            try {
                JSONObject translations = new JSONObject();
                translations.put("plugin.inlineEditColumnFormatter.validation.default",
                        AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.validation.default", getClassName(), MESSAGE_PATH));
                translations.put("plugin.inlineEditColumnFormatter.error.save",
                        AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.error.save", getClassName(), MESSAGE_PATH));
                translations.put("plugin.inlineEditColumnFormatter.error.network",
                        AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.error.network", getClassName(), MESSAGE_PATH));
                writer.write(translations.toString());
            } catch (Exception e) {
                response.setStatus(500);
                writer.write(new JSONObject()
                        .put("status", "error")
                        .put("message", AppPluginUtil.getMessage("plugin.inlineEditColumnFormatter.error.save", getClassName(), MESSAGE_PATH))
                        .toString());
            }
        }
    }

    @Override
    public Set<String> getOfflineStaticResources() {
        Set<String> urls = new HashSet<>();
        String contextPath = AppUtil.getRequestContextPath();
        urls.add(contextPath + "/plugin/" + getClassName() + "/css/inlineEditColumnFormat.css");
        urls.add(contextPath + "/plugin/" + getClassName() + "/js/inlineEditColumnFormat.js");
        return urls;
    }
}