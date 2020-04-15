package com.hk.HKConnector.Service.impl;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.*;
import com.google.api.client.http.javanet.*;
import com.google.api.services.sheets.v4.*;
import com.google.api.services.sheets.v4.model.*;
import com.google.common.base.*;
import com.google.gson.*;
import com.google.gson.internal.*;
import com.hk.HKConnector.Constants.*;
import com.hk.HKConnector.Exception.*;
import com.hk.HKConnector.Repository.*;
import com.hk.HKConnector.Service.*;
import com.hk.HKConnector.Util.*;
import com.hk.HKConnector.model.*;
import com.hk.HKConnector.model.embedded.*;
import lombok.extern.slf4j.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import javax.servlet.http.*;
import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

@Slf4j
@Service
public class GoogleSheetServiceImpl implements GoogleSheetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleSheetServiceImpl.class);

    @Autowired
    private BulkProcessConfigurationRepository bulkProcessConfigurationRepository;

    @Autowired
    private BulkProcessConfigService bulkProcessConfigService;

    @Autowired
    private HkcRestClient hkcRestClient;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private GoogleSheetStatusRepository googleSheetStatusRepository;

    @Override
    public String processSheet(String buttonId, String googleSheetId, String email, Credential cr, Map<String, Object> stateMap) throws GeneralSecurityException, IOException, SheetProcessingException {
        //TODO : Distributed Locking
        GoogleSheetStatus googleSheetStatus = googleSheetStatusRepository.findBySheetId(googleSheetId).orElse(null);
        if(googleSheetStatus == null) {
            throw new SheetProcessingException(HKConnectorExceptionCodes.HKC_102);
        } else if(!googleSheetStatus.getStatus().equalsIgnoreCase(EnumGoogleSheetStatus.CREATED.getCode())) {
            throw new SheetProcessingException(HKConnectorExceptionCodes.HKC_103);
        } else if(!googleSheetStatus.getButtonId().equals(buttonId)) {
            throw new SheetProcessingException(HKConnectorExceptionCodes.HKC_103);
        } else {
            googleSheetStatus.setStatus(EnumGoogleSheetStatus.IN_PROCESS.getCode());
            googleSheetStatus.setProcessedBy(email);
            googleSheetStatus.setProcessedOn(new Date());
            googleSheetStatusRepository.save(googleSheetStatus);
        }
        BulkProcessConfiguration bulkProcessConfiguration;
        Channel channel;
        try {
            bulkProcessConfiguration = bulkProcessConfigService.fetchProcessingConfigurationById(buttonId);
            channel = channelRepository.findById(bulkProcessConfiguration.getChannelId()).orElseThrow(() -> new EntityNotFoundException(HKConnectorExceptionCodes.HKC_101));
        } catch (EntityNotFoundException e) {
            throw e;
        }
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        //TODO : Move to singleton scope
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, HKGoogleAuthUtil.JSON_FACTORY, cr)
                .setApplicationName(HKGoogleAuthUtil.APPLICATION_NAME)
                .build();
        List<Sheet> sheets = service.spreadsheets().get(googleSheetId).setIncludeGridData(true).execute().getSheets();
        boolean errorInProcessing = false;
        if(!CollectionUtils.isEmpty(sheets)) {
            Sheet inProcessSheet = sheets.get(0);
            //Get the column names from sheet and cross check with those in database
            List<GridData> sheetData = inProcessSheet.getData();
            if(sheetData != null) {
                List<CellData> columnCells = sheetData.get(0).getRowData().get(0).getValues();
                Map<String, String> excelNameToJsonName = new HashMap<>();
                Map<String, String> excelNameToType = new HashMap<>();
                Map<String,Object> requestStructureRefMap = new HashMap<>();
                Map<Integer, String> cellIdToColumnName = new HashMap<>();
                Map<String,Object> refMap;
                int level = 1;
                //Create a map for json data and column map
                for(BulkProcessColumnDetail bpc : bulkProcessConfiguration.getColumnDetails()) {
                    excelNameToJsonName.put(bpc.getExcelDisplayName(), bpc.getJsonKeyName());
                    excelNameToType.put(bpc.getExcelDisplayName(), bpc.getDataType());
                    String[] depthObj = bpc.getJsonKeyName().split("\\.");
                    level = 1;
                    refMap = requestStructureRefMap;
                    for(String jsonKey : depthObj) {
                        if (depthObj.length == level) {
                            refMap.put(jsonKey, null);
                        } else {
                            if(refMap.containsKey(jsonKey)) {
                                refMap = (Map<String, Object>) refMap.get(jsonKey);
                            } else {
                                refMap.put(jsonKey, new HashMap<String, Object>());
                                refMap = (Map<String, Object>) refMap.get(jsonKey);
                            }
                        }
                        level ++;
                    }
                }
                //Validations
                String sheetValidation = null;
                if(columnCells.size() != bulkProcessConfiguration.getColumnDetails().size()) {
                    sheetValidation = "Column count in sheet not as expected";
                }
                //Comparing column names
                if(sheetValidation == null) {
                    //List<String> columnNamesFromSheet = columnCells.stream().map(CellData::getFormattedValue).collect(Collectors.toList());
                    for(int i = 0; i < columnCells.size(); i++) {
                        cellIdToColumnName.put(i, columnCells.get(i).getFormattedValue());
                    }
                    if(Collections.disjoint(cellIdToColumnName.values(), excelNameToJsonName.keySet())) {
                        sheetValidation = "Column Name mismatch";
                    }
                }

                //Adding new column for status
                List<List<Object>> values = Arrays.asList(
                        Arrays.asList("Status", "Comments"));
                ValueRange valueRange = new ValueRange();
                valueRange.setValues(values);
                UpdateValuesResponse result =
                        service.spreadsheets().values().update(googleSheetId, HkUtil.getRange(bulkProcessConfiguration.getColumnDetails().size() + 1, bulkProcessConfiguration.getColumnDetails().size() + 2, 1, 1), valueRange)
                                .setValueInputOption("RAW") //The values the user has entered will not be parsed and will be stored as-is.
                                .execute();

                if(sheetValidation == null) {
                    Map<String, Object> reqMap;
                    List<RowData> sheetRows = inProcessSheet.getData().get(0).getRowData();
                    values = new ArrayList<>();
                    int i = 1;
                    int nullCellCount = 0;
                    for (i = 1 ; i < sheetRows.size() ; i++) {
                        reqMap = new HashMap<>(requestStructureRefMap);
                        columnCells = sheetRows.get(i).getValues();
                        nullCellCount = 0;
                        try {
                            for (int k = 0; k < columnCells.size(); k++) {
                                String[] depthObj = excelNameToJsonName.get(cellIdToColumnName.get(k)).split("\\.");
                                String dataType = excelNameToType.get(cellIdToColumnName.get(k));
                                level = 1;
                                refMap = reqMap;
                                for (String jsonKey : depthObj) {
                                    if (depthObj.length == level) {
                                        if(StringUtils.isEmpty(columnCells.get(k).getFormattedValue())) {
                                            nullCellCount++;
                                        }
                                        refMap.put(jsonKey, HkUtil.parseInputToGivenClassTypeValue(columnCells.get(k).getFormattedValue(), dataType));
                                    } else {
                                        refMap = (Map<String, Object>) refMap.get(jsonKey);
                                    }
                                    level ++;
                                }
                            }
                            if(nullCellCount == columnCells.size()) {
                                continue;
                            }
                            //Read each row and start processing it and update each sheet
                            //Client based - add configuration
                            Gson gson = new Gson();
                            String json = gson.toJson(reqMap);
                            StringBuffer url = new StringBuffer(channel.getChannelEndPoint()).append(bulkProcessConfiguration.getTargetProcessingUri());
                            //URL finalUrl = new URL(url.toString());
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            if (!CollectionUtils.isEmpty(stateMap)) {
                                if (stateMap.containsKey(HttpHeaders.COOKIE)) {
                                        LinkedTreeMap<String,Object> jSessionCookieMap = ((LinkedTreeMap<String,Object>)((LinkedTreeMap<String,Object>)stateMap.get(HttpHeaders.COOKIE)).get(CookieConstant.COOKIE_JSESSION_ID));
                                        if(jSessionCookieMap != null) {
                                            headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + jSessionCookieMap.get("value"));
                                        } else {
                                            log.error(MessageConstants.CRITICAL_ALERT + " JSESSIONID not found while processing sheet id :" + googleSheetId + " by user with email " + email);
                                        }
                                }

                            }
                            ResponseEntity responseEntity = hkcRestClient.post(json, headers , url.toString(), String.class);
                            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                                if (responseEntity.getHeaders().get(CoreConstants.BULK_UPDATE_API_RESPONSE_STATUS) != null
                                        && HttpStatus.valueOf(Integer.parseInt(responseEntity.getHeaders().get(CoreConstants.BULK_UPDATE_API_RESPONSE_STATUS).get(0))).is2xxSuccessful()) {
                                    values.add(
                                            Arrays.asList("Success", ""));
                                } else {
                                    if (responseEntity.getHeaders().get(CoreConstants.BULK_UPDATE_API_RESPONSE_STATUS) != null) {
                                        values.add(
                                                Arrays.asList("Failed", responseEntity.getHeaders().get(CoreConstants.BULK_UPDATE_API_RESPONSE_ERR_MSZ).get(0)));
                                    } else {
                                        values.add(
                                                Arrays.asList("Failed", "SOME_ERROR_OCCURRED_IN_PROCESSING"));
                                    }
                                }

                            } else {
                                values.add(
                                        Arrays.asList("Failed", "SYSTEM_ERROR | HTTP_STATUS " + responseEntity.getStatusCodeValue()));
                            }
                        } catch (ClassNotFoundException e) {
                            //Add log
                            values.add(
                                    Arrays.asList("Failed" , "SYSTEM_ERROR_CLASS_NOT_FOUND"));
                            errorInProcessing = true;
                        } catch (Exception e) {
                            //Add log
                            values.add(
                                    Arrays.asList("Failed" , e.getMessage()));
                            errorInProcessing = true;
                        } finally {
                            //Add Status Columns to sheet
                           /* result =
                                    service.spreadsheets().values().update(googleSheetId, HkUtil.getRange(bulkProcessConfiguration.getColumnDetails().size() + 1, bulkProcessConfiguration.getColumnDetails().size() + 2, i + 1, i + 1), valueRange)
                                            .setValueInputOption("RAW") //The values the user has entered will not be parsed and will be stored as-is.
                                            .execute();*/
                        }

                    }
                    valueRange.setValues(values);
                    result =
                            service.spreadsheets().values().update(googleSheetId, HkUtil.getRange(bulkProcessConfiguration.getColumnDetails().size() + 1, bulkProcessConfiguration.getColumnDetails().size() + 2, 2, i), valueRange)
                                    .setValueInputOption("RAW") //The values the user has entered will not be parsed and will be stored as-is.
                                    .execute();
                }
            }
        }

        if(errorInProcessing) {
            googleSheetStatus.setStatus(EnumGoogleSheetStatus.PROCESSED_WITH_ERROR.getCode());
        } else {
            googleSheetStatus.setStatus(EnumGoogleSheetStatus.PROCESSED_SUCCESS.getCode());
        }

        googleSheetStatusRepository.save(googleSheetStatus);
        return "Success";
    }

    @Override
    public String createSheet(String buttonId, String email, Credential cr) throws GeneralSecurityException, IOException {
        //Identify Sheet
        BulkProcessConfiguration bulkProcessConfiguration;
        try {
            bulkProcessConfiguration = bulkProcessConfigService.fetchProcessingConfigurationById(buttonId);
        } catch (EntityNotFoundException e) {
            throw e;
        }

        //Create sheet
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, HKGoogleAuthUtil.JSON_FACTORY, cr)
                .setApplicationName(HKGoogleAuthUtil.APPLICATION_NAME)
                .build();
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle(bulkProcessConfiguration.getSheetName()));
        spreadsheet = service.spreadsheets().create(spreadsheet)
                .execute();
        bulkProcessConfiguration.getColumnDetails().size();
        List<Object> excelColumns = bulkProcessConfiguration.getColumnDetails().stream().map(BulkProcessColumnDetail::getExcelDisplayName).collect(Collectors.toList());
        ValueRange valueRange = new ValueRange();
        List<List<Object>> values = new ArrayList<>();
        values.add(excelColumns);
        valueRange.setValues(values);

        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheet.getSpreadsheetId(), HkUtil.getRange(1, bulkProcessConfiguration.getColumnDetails().size(), 1, 1), valueRange)
                        .setValueInputOption("RAW") //The values the user has entered will not be parsed and will be stored as-is.
                        .execute();
        /*
           spreadsheetId - It is the Spreadsheet sheet which we are working on.
           spreadsheetUrl - Spreadsheet URL for reference
         */

        //Logging on SpreadsheetCreation
        GoogleSheetStatus gss = new GoogleSheetStatus();
        gss.setButtonId(buttonId);
        gss.setSheetId(spreadsheet.getSpreadsheetId());
        gss.setCreatedOn(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        gss.setCreateDt(formatter.format(new Date()));
        gss.setStatus(EnumGoogleSheetStatus.CREATED.getCode());
        gss.setCreateBy(email);

        googleSheetStatusRepository.save(gss);
        //Update mapping for sheet created
        return spreadsheet.getSpreadsheetUrl();
    }
}
