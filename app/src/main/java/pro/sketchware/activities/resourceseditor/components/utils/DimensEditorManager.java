package pro.sketchware.activities.resourceseditor.components.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mod.hey.studios.util.Helper;
import pro.sketchware.activities.resourceseditor.ResourcesEditorActivity;
import pro.sketchware.utility.FileUtil;

public class DimensEditorManager {

    public boolean isDefaultVariant = true;
    public boolean isDataLoadingFailed;
    public String sc_id;

    public HashMap<Integer, String> notesMap = new HashMap<>();

    public static void setupDimenAutoComplete(Context context, String scId, MaterialAutoCompleteTextView autoCompleteTextView) {
        if (context == null || autoCompleteTextView == null) return;
        ArrayList<HashMap<String, Object>> dimensListMap = new ArrayList<>();
        if (scId != null && !scId.isEmpty()) {
            String dimensFilePath = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(scId.concat("/files/resource/values/dimens.xml"));
            DimensEditorManager dimensEditorManager = new DimensEditorManager();
            dimensEditorManager.convertXmlDimensToListMap(FileUtil.readFileIfExist(dimensFilePath), dimensListMap);
        }

        List<String> suggestions = new ArrayList<>();
        for (HashMap<String, Object> map : dimensListMap) {
            String keyValue = String.valueOf(map.get("key"));
            suggestions.add("@dimen/" + keyValue + " ( " + map.get("value") + " )");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, suggestions);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String val = Helper.getText(autoCompleteTextView);
            if (val.contains(" (")) {
                autoCompleteTextView.setText(val.substring(0, val.indexOf(" (")));
                autoCompleteTextView.setSelection(autoCompleteTextView.getText().length());
            }
        });
    }

    public void convertXmlDimensToListMap(final String xmlString, final ArrayList<HashMap<String, Object>> listMap) {
        isDataLoadingFailed = false;
        try {
            listMap.clear();
            notesMap.clear();
            if (xmlString == null || xmlString.trim().isEmpty()) {
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(new InputSource(input));
            doc.getDocumentElement().normalize();
            NodeList childNodes = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.COMMENT_NODE) {
                    notesMap.put(listMap.size(), node.getNodeValue().trim());
                } else if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("dimen")) {
                    addToListMap(listMap, (Element) node);
                }
            }
        } catch (Exception ignored) {
            isDataLoadingFailed = xmlString != null && !xmlString.trim().isEmpty();
        }
    }

    private void addToListMap(ArrayList<HashMap<String, Object>> list, Element node) {
        HashMap<String, Object> map = new HashMap<>();
        String key = node.getAttribute("name");
        String value = node.getTextContent().replace("\\", "").trim();
        map.put("key", key);
        map.put("value", value);

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node attr = node.getAttributes().item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();

            if (!attrName.equals("name")) {
                map.put(attrName, attrValue);
            }
        }

        list.add(map);
    }

    public boolean isXmlDimensExist(ArrayList<HashMap<String, Object>> listMap, String value) {
        for (Map<String, Object> map : listMap) {
            if (map.containsKey("key") && value.equals(map.get("key"))) {
                return true;
            }
        }
        return false;
    }

    public String convertListMapToXmlDimens(final ArrayList<HashMap<String, Object>> listMap, HashMap<Integer, String> notesMap) {
        StringBuilder xmlString = new StringBuilder();
        xmlString.append("<resources>\n");
        for (int i = 0; i < listMap.size(); i++) {
            if (notesMap.containsKey(i)) {
                xmlString.append("    <!-- ").append(notesMap.get(i)).append(" -->\n");
            }
            HashMap<String, Object> map = listMap.get(i);
            String key = (String) map.get("key");
            String value = (String) map.getOrDefault("value", "");
            String escapedValue = ResourcesEditorActivity.escapeXml(value);
            xmlString.append("    <dimen name=\"").append(key).append("\"");
            for (String mapKey : map.keySet()) {
                if (mapKey.equals("value") || mapKey.equals("key")) continue;
                String extraAttr = (String) map.get(mapKey);
                xmlString.append(" ").append(mapKey).append("=\"").append(extraAttr).append("\"");
            }
            xmlString.append(">").append(escapedValue).append("</dimen>\n");
        }
        xmlString.append("</resources>");
        return xmlString.toString();
    }
}
