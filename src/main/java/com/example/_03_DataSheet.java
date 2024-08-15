package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class _03_DataSheet {
    private static JTextField linkTextField;
    private static List<String> links = new ArrayList<>();
    private static final String APPLICATION_NAME = "sOorting";
    private static final String CACHE_FILE_PATH = System.getProperty("user.home") + "/Downloads/GoogleSyncData/cache.dat";
    public static Map<String, CachedData> cachedDataMap;

    
    static {
        cachedDataMap = _01_App.loadCache();

        if (cachedDataMap == null || needsRefresh(cachedDataMap)) {
            System.out.println("Cache is empty or stale, fetching new data."); // Debugging line
            cachedDataMap = fetchDataFromGoogleSheets();
            _01_App.saveCache(cachedDataMap);
        } else {
            System.out.println("Using cached data."); // Debugging line
        }

    }

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static Sheets sheetsService;
    private static JFrame dataSheetFrame;
    public static Map<String, String> titleToCostMap = new HashMap<>();
    public static Map<String, String> titleToPriceMap = new HashMap<>();
    public static Map<String, String> titleToCategoryMap = new HashMap<>();

    static {
        cachedDataMap = loadCache();

        if (cachedDataMap == null || needsRefresh(cachedDataMap)) {
            // Fetch data from Google Sheets and update the cache
            cachedDataMap = fetchDataFromGoogleSheets();
            saveCache(cachedDataMap);
        }

        // Use cachedDataMap for display or other processing
    }
    private static String infoMessage = 
    "If you are getting an error message:\n\n" +
    "1. **This Link is Not Supported**\n" +
    "   - Your Google Sheet may be in .XLSX format. Go to your Google Sheets, \n" +
    "     go to 'File', and then click 'Save as Google Sheets' and try again.\n\n" +
    "2. **Resource Not Found**\n" +
    "   - Make sure the link you provided is correct and points to an existing Google Sheets document.\n" +
    "   - Check if the document is shared properly. Ensure it is accessible by anyone with the link.\n\n" +
    "3. **Invalid Link Format**\n" +
    "   - Ensure the link you provided is in the correct format. It should look something like:\n" +
    "     https://docs.google.com/spreadsheets/d/your-spreadsheet-id/edit\n\n" +
    "4. **Google Sheets API Errors**\n" +
    "   - If you encounter issues related to Google Sheets API, please contact developer: r.a.castilleja@tcu.edu\\n" + //
                "\\n" + //
                "" +
    
    "5. **Authorization Errors**\n" +
    "   - If you are asked to reauthorize, follow the prompts to grant access.\n" +
    "   - Ensure the OAuth 2.0 client ID is set up correctly in your Google Cloud Console and matches your application's settings.\n\n";




    
    public static void displayDataSheetGUI(List<String> existingLinks) {
        links = existingLinks; // Use existing shared links
        dataSheetFrame = new JFrame("Data Sheet");
        dataSheetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Increase the size by 30%
        dataSheetFrame.setSize(1140, 535); // Adjusted size
        
        dataSheetFrame.setLayout(new BorderLayout());
        displayResult(getUpdatedCountMap(), dataSheetFrame, "|View All|");
        dataSheetFrame.setVisible(true);

    
        // Add info button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton infoButton = new JButton("Error help");
        infoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInfoDialog();
            }
        });
        topPanel.add(infoButton);
        dataSheetFrame.add(topPanel, BorderLayout.NORTH);
    
    
        // Add link text field and button panel
        JPanel linkPanel = new JPanel(new FlowLayout());
        JLabel linkLabel = new JLabel("Enter Google Sheets Link:");
        linkTextField = new JTextField(30);
        JButton addLinkButton = new JButton("Add Link");
        addLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dirtyLink = linkTextField.getText();
                if (!dirtyLink.isEmpty()) {
                    String cleanLink = cleanLink(dirtyLink);
                    if (cleanLink != null) {
                        if (links.contains(cleanLink)) {
                            JOptionPane.showMessageDialog(dataSheetFrame, "This link has already been added.", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            links.add(cleanLink);
                            System.out.println("Added clean link: " + cleanLink);
                            linkTextField.setText("");
                            saveLinks(links); // Save links after addition
                            updateDisplay(); // Update display with new data
                            _04_BrandSheet.updateDisplay(); // Notify BrandSheet to update display
                        }
                    } else {
                        JOptionPane.showMessageDialog(dataSheetFrame, "Invalid link format.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    
        // Change the reset button to a back button
        JButton backButton = new JButton("Remove last link");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!links.isEmpty()) {
                    links.remove(links.size() - 1); // Remove the last link
                    saveLinks(links); // Save the updated list
                    updateDisplay(); // Update the display
                    _04_BrandSheet.updateDisplay(); // Notify BrandSheet to update display if necessary
                } else {
                    JOptionPane.showMessageDialog(dataSheetFrame, "No links to remove.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

JButton categoriesButton = new JButton("Categories");

categoriesButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        String[] categories = {"parts", "Watch Movement Holder", "band", "Collet", "Balance Assembly", 
            "Wristwatch Movement", "Wristwatch", "Pocket Watch Movement", 
            "pocket watch case", "pocket watch", "|View All|"};
        String selectedCategory = (String) JOptionPane.showInputDialog(dataSheetFrame, "Choose a category:", "Category Selection", JOptionPane.PLAIN_MESSAGE, null, categories, categories[0]);
        
        if (selectedCategory != null) {
            DebugUtil.debugPrint("Category selected: " + selectedCategory); // Debug line to know when a category is clicked

            Map<String, Integer> filteredCountMap;

            if ("|View All|".equals(selectedCategory)) {
                filteredCountMap = getUpdatedCountMap();
            } else {
                filteredCountMap = getUpdatedCountMapForCategory(selectedCategory.toLowerCase());
            }

            // Update the DataSheet display
            dataSheetFrame.getContentPane().removeAll();
            displayResult(filteredCountMap, dataSheetFrame, selectedCategory);

            // Update the BrandSheet display with the selected category
            _04_BrandSheet.updateDisplayWithCategory(selectedCategory);

            // Re-add the link panel
            JPanel linkPanel = new JPanel(new FlowLayout());
            linkPanel.add(linkLabel);
            linkPanel.add(linkTextField);
            linkPanel.add(addLinkButton);
            linkPanel.add(backButton);
            linkPanel.add(categoriesButton);

            dataSheetFrame.add(linkPanel, BorderLayout.SOUTH);

            dataSheetFrame.revalidate();
            dataSheetFrame.repaint();
        }
    }
});

        // Add the categoriesButton to the linkPanel
        
    
        linkPanel.add(linkLabel);
        linkPanel.add(linkTextField);
        linkPanel.add(addLinkButton);
        linkPanel.add(backButton); 
        linkPanel.add(categoriesButton);

        dataSheetFrame.add(linkPanel, BorderLayout.SOUTH);
    
        dataSheetFrame.setVisible(true);
    }
    

  
    public static JFrame getFrame() {
        return dataSheetFrame;
    }


    public static void updateDisplayWithCategory(String category) {
        if (dataSheetFrame != null) {
            dataSheetFrame.getContentPane().removeAll();
            displayResult(getUpdatedCountMap(), dataSheetFrame, "|View All|");
            dataSheetFrame.revalidate();
            dataSheetFrame.repaint();
        }
    }
    
    private static void findAndPrintCostAndPrice(String spreadsheetId, String sheetName, int titleRowIndex) {
        try {
            ValueRange response = _01_App.executeWithRetry(() -> sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute());
            List<List<Object>> values = response.getValues();
    
            int costColumnIndex = -1;
            int priceColumnIndex = -1;
            int titleColumnIndex = -1;
    
            // Define regular expressions for matching headers
            String costPattern = "(?i)\\s*cost\\s*(\\(\\$\\))?\\s*";
            String pricePattern = "(?i)\\s*price\\s*(\\(\\$\\))?\\s*";
            String titlePattern = "(?i)\\s*title\\s*";
    
            // Search through all rows and columns to find the headers
            if (values != null && !values.isEmpty()) {
                for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                    List<Object> row = values.get(rowIndex);
    
                    // Skip the header row based on title pattern matching
                    if (rowIndex == titleRowIndex - 1) continue;
    
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        String cellValue = row.get(colIndex).toString().trim();
    
                        if (cellValue.matches(costPattern)) {
                            costColumnIndex = colIndex;
                        } else if (cellValue.matches(pricePattern)) {
                            priceColumnIndex = colIndex;
                        } else if (cellValue.matches(titlePattern)) {
                            titleColumnIndex = colIndex;
                        }
                    }
    
                    // Process the rows below the header row
                    if (rowIndex >= titleRowIndex) {
                        if (costColumnIndex == -1 || priceColumnIndex == -1 || titleColumnIndex == -1) {
                            DebugUtil.debugPrint("Cost, Price, or Title column not found.");
                            return;
                        }
    
                        String title = row.get(titleColumnIndex).toString().trim().toLowerCase();
                        if (!title.isEmpty()) {
                            String cost = costColumnIndex < row.size() ? row.get(costColumnIndex).toString().trim() : "";
                            String price = priceColumnIndex < row.size() ? row.get(priceColumnIndex).toString().trim() : "";
    
                            titleToCostMap.put(title, cost);
                            titleToPriceMap.put(title, price);
                        }
                    }
                }
            }
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 429) {
                showErrorMessage("Rate limit exceeded. Please try again later.");
            } else {
                showErrorMessage("An error occurred: " + e.getMessage());
            }
        } catch (IOException | GeneralSecurityException e) {
            showErrorMessage("An I/O error occurred: " + e.getMessage());
        }
    }
    
  
 
public static void displayResult(Map<String, Integer> countMap, JFrame frame, String category) {
    ensureAppDataFolderExists2();
    JPanel resultPanel = new JPanel(new BorderLayout());

    // Create a JTable to display titles, occurrences, margin, price, and cost
    String[] columnNames = {"Title", "Occurrences", "Margin", "Price", "Cost"};
    Object[][] data = new Object[countMap.size()][5];
    int index = 0;

    List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(countMap.entrySet());
    sortedEntries.sort((e1, e2) -> {
        String title1 = e1.getKey();
        String title2 = e2.getKey();
        double margin1 = calculateMargin(titleToCostMap.getOrDefault(title1, "0.0"), titleToPriceMap.getOrDefault(title1, "0.0"));
        double margin2 = calculateMargin(titleToCostMap.getOrDefault(title2, "0.0"), titleToPriceMap.getOrDefault(title2, "0.0"));
        return Double.compare(margin2, margin1);
    });

    Set<String> uniqueTitles = new HashSet<>();

    try (PrintWriter writer = new PrintWriter(new FileWriter(getAppDataFilePath("DataSheet.txt"), true))) {
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            String title = entry.getKey();
            int occurrences = entry.getValue();

            String cost = titleToCostMap.getOrDefault(title, "N/A");
            String price = titleToPriceMap.getOrDefault(title, "N/A");
            String margin = "N/A";

            try {
                double marginValue = calculateMargin(cost, price);
                margin = String.format("%.2f", marginValue);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing margin for title: " + title);
            }

            if (!uniqueTitles.contains(title)) {
                uniqueTitles.add(title);

                data[index][0] = title;
                data[index][1] = occurrences;
                data[index][2] = margin;
                data[index][3] = price;
                data[index][4] = cost;

                writer.println(title + ", " + occurrences + ", " + margin + ", " + price + ", " + cost);

                index++;
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    JTable table = new JTable(data, columnNames);
    table.setFillsViewportHeight(true);
    table.setShowGrid(true);
    table.setGridColor(Color.LIGHT_GRAY);

    table.getColumnModel().getColumn(0).setPreferredWidth(650);
    table.getColumnModel().getColumn(1).setPreferredWidth(50);
    table.getColumnModel().getColumn(2).setPreferredWidth(50);
    table.getColumnModel().getColumn(3).setPreferredWidth(50);
    table.getColumnModel().getColumn(4).setPreferredWidth(50);

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(new Font("Arial", Font.PLAIN, 14));
            ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
            return c;
        }
    };

    for (int i = 0; i < table.getColumnCount(); i++) {
        table.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(BorderFactory.createTitledBorder("Titles, Occurrences, Margin, Price, and Cost"));

    resultPanel.add(scrollPane, BorderLayout.CENTER);
    frame.add(resultPanel, BorderLayout.CENTER);
    frame.revalidate();
    frame.repaint();
}


    private static Map<String, Integer> getUpdatedCountMap() {
        return getUpdatedCountMap("|View All|"); // Default to all categories
    }
    
    private static Map<String, Integer> getUpdatedCountMap(String categoryFilter) {
        Map<String, Integer> updatedCountMap = new HashMap<>();
        try {
            // Initialize Sheets service and get spreadsheet data
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, _01_App.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
    
            for (String spreadsheetId : links) {
                Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
                String sheetName = spreadsheet.getSheets().get(0).getProperties().getTitle();
    
                ValueRange response = _01_App.executeWithRetry(() -> sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute());
                List<List<Object>> values = response.getValues();
    
                int titleColumnIndex = -1;
    
                if (values != null && !values.isEmpty()) {
                    for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                        List<Object> row = values.get(rowIndex);
    
                        for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                            String cellValue = row.get(colIndex).toString().trim().toLowerCase();
                            if (cellValue.equals("title")) {
                                titleColumnIndex = colIndex;
                            }
                        }
    
                        // Skip header row
                        if (rowIndex == 0 || titleColumnIndex == -1) continue;
    
                        // Extract title from the row
                        String title = titleColumnIndex < row.size() ? row.get(titleColumnIndex).toString().trim().toLowerCase() : "";
                        String itemCategory = determineCategoryByTitle(title); // Determine the category from title
    
                        // Store the category in the map
                        titleToCategoryMap.put(title, itemCategory);
    
                        // Filter based on the provided category
                        if (categoryFilter.equals("|View All|") || itemCategory.equalsIgnoreCase(categoryFilter)) {
                            updatedCountMap.put(title, updatedCountMap.getOrDefault(title, 0) + 1);
                        }
    
                        // Make sure cost and price data are retrieved for "View All"
                        if (categoryFilter.equals("|View All|")) {
                            String cost = titleToCostMap.getOrDefault(title, "N/A");
                            String price = titleToPriceMap.getOrDefault(title, "N/A");
    
                            if (cost.equals("N/A") || price.equals("N/A")) {
                                findAndPrintCostAndPrice(spreadsheetId, sheetName, rowIndex);
                            }
                        }
                    }
                }
            }
        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace();
        }
        return updatedCountMap;
    }
    

    private static String getColumnLetter(int colIndex) {
        StringBuilder column = new StringBuilder();
        while (colIndex >= 0) {
            int remainder = colIndex % 26;
            column.insert(0, (char) (remainder + 'A'));
            colIndex = (colIndex / 26) - 1;
        }
        return column.toString();
    }

    public static String cleanLink(String dirtyLink) {
        int startIndex = dirtyLink.indexOf("/d/") + 3; // Index after "/d/"
        int endIndex = dirtyLink.indexOf("/edit"); // Index before "/edit"
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return dirtyLink.substring(startIndex, endIndex);
        }
        return null; // Handle if the link format is unexpected
    }

    public static void saveLinks(List<String> links) {
        ensureAppDataFolderExists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(getAppDataFilePath("links.txt")))) {
            for (String link : links) {
                writer.println(link);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        
    public static void updateDisplay() {
        if (dataSheetFrame != null) {
            dataSheetFrame.getContentPane().removeAll();
            
            // Update the main content (table)
            displayResult(getUpdatedCountMap(), dataSheetFrame, "|View All|");
    
            // Re-add the link panel with buttons
            JPanel linkPanel = new JPanel(new FlowLayout());
            JLabel linkLabel = new JLabel("Enter Google Sheets Link:");
            linkPanel.add(linkLabel);
            linkPanel.add(linkTextField);
    
            // Add the buttons back
            JButton addLinkButton = new JButton("Add Link");
            addLinkButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String dirtyLink = linkTextField.getText();
                    if (!dirtyLink.isEmpty()) {
                        String cleanLink = cleanLink(dirtyLink);
                        if (cleanLink != null) {
                            if (links.contains(cleanLink)) {
                                JOptionPane.showMessageDialog(dataSheetFrame, "This link has already been added.", "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                links.add(cleanLink);
                                linkTextField.setText("");
                                saveLinks(links); // Save links after addition
                                updateDisplay(); // Update display with new data
                                _04_BrandSheet.updateDisplay(); // Notify BrandSheet to update display
                            }
                        } else {
                            JOptionPane.showMessageDialog(dataSheetFrame, "Invalid link format.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            linkPanel.add(addLinkButton);
    
            JButton backButton = new JButton("Remove last link");
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!links.isEmpty()) {
                        links.remove(links.size() - 1); // Remove the last link
                        saveLinks(links); // Save the updated list
                        updateDisplay(); // Update the display
                        _04_BrandSheet.updateDisplay(); // Notify BrandSheet to update display if necessary
                    } else {
                        JOptionPane.showMessageDialog(dataSheetFrame, "No links to remove.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            linkPanel.add(backButton);
    
            JButton categoriesButton = new JButton("Categories");
            categoriesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] categories = {"parts", "Watch Movement Holder", "band", "Collet", "Balance Assembly", 
                        "Wristwatch Movement", "Wristwatch", "Pocket Watch Movement", 
                        "pocket watch case", "pocket watch", "|View All|"};
                    String selectedCategory = (String) JOptionPane.showInputDialog(dataSheetFrame, "Choose a category:", "Category Selection", JOptionPane.PLAIN_MESSAGE, null, categories, categories[0]);
    
                    if (selectedCategory != null) {
                        Map<String, Integer> filteredCountMap;
    
                        if ("|View All|".equals(selectedCategory)) {
                            filteredCountMap = getUpdatedCountMap();
                        } else {
                            filteredCountMap = getUpdatedCountMapForCategory(selectedCategory.toLowerCase());
                        }
    
                        // Update the DataSheet display
                        dataSheetFrame.getContentPane().removeAll();
                        displayResult(filteredCountMap, dataSheetFrame, selectedCategory);
    
                        // Re-add the link panel after category selection
                        updateDisplay();
                    }
                }
            });
            linkPanel.add(categoriesButton);
    
            dataSheetFrame.add(linkPanel, BorderLayout.SOUTH);
            
            dataSheetFrame.revalidate();
            dataSheetFrame.repaint();
        }
    }
    

    private static Map<String, Integer> getUpdatedCountMapForCategory(String categoryFilter) {
        Map<String, Integer> updatedCountMap = new HashMap<>();
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, _01_App.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
    
            for (String spreadsheetId : links) {
                try {
                    Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
                    String sheetName = spreadsheet.getSheets().get(0).getProperties().getTitle();
    
                    // Fetch the entire sheet
                    ValueRange response = _01_App.executeWithRetry(() -> sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute());
                    List<List<Object>> values = response.getValues();
                    int titleColumnIndex = -1;
                    int titleRowIndex = -1;
    
                    if (values != null && !values.isEmpty()) {
                        outerLoop:
                        for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                            List<Object> row = values.get(rowIndex);
                            for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                                String cellValue = row.get(colIndex).toString().trim().toLowerCase();
                                if (cellValue.equals("title")) {
                                    titleColumnIndex = colIndex;
                                    titleRowIndex = rowIndex + 1; // Store the row index where "Title" is found (1-based index)
                                    break outerLoop; // Exit both loops
                                }
                            }
                        }
                    }
    
                    if (titleColumnIndex == -1 || titleRowIndex == -1) {
                        continue;
                    }
    
                    findAndPrintCostAndPrice(spreadsheetId, sheetName, titleRowIndex);
    
                    // Fetch data from the identified column below the "Title" header
                    String range = sheetName + "!" + getColumnLetter(titleColumnIndex) + (titleRowIndex + 1) + ":" + getColumnLetter(titleColumnIndex);
                    response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
                    values = response.getValues();
    
                    if (values != null && !values.isEmpty()) {
                        for (List<Object> row : values) {
                            if (!row.isEmpty()) {
                                String title = row.get(0).toString().trim().toLowerCase();
                                String itemCategory = determineCategoryByTitle(title);
    
                                DebugUtil.debugPrint("Title: " + title + ", Expected Category: " + categoryFilter + ", Found Category: " + itemCategory);
    
                                if (categoryFilter.equals("|view all|") || itemCategory.equalsIgnoreCase(categoryFilter)) {
                                    updatedCountMap.put(title, updatedCountMap.getOrDefault(title, 0) + 1);
                                }
                            }
                        }
                    }
                } catch (GoogleJsonResponseException e) {
                    if (e.getStatusCode() == 400 && "failedPrecondition".equals(e.getDetails().getErrors().get(0).getReason())) {
                        JOptionPane.showMessageDialog(dataSheetFrame, "Error: " + e.getDetails().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        throw e; // Re-throw if it's not the specific error we're handling
                    }
                }
            }
        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace();
        }
        return updatedCountMap;
    }
    

    private static void showInfoDialog() {
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setText(infoMessage);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(true);

        int result = JOptionPane.showConfirmDialog(null, new JScrollPane(textArea), "Info", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            infoMessage = textArea.getText();
        }
    }

    private static double calculateMargin(String costStr, String priceStr) {
        try {
            double cost = Double.parseDouble(costStr);
            double price = Double.parseDouble(priceStr);
            return price - cost;
        } catch (NumberFormatException e) {
            return 0.0; // Default margin if cost or price are not valid numbers
        }
    }

    private static String getAppDataFilePath(String filename) {
        String appDataPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
        return appDataPath + "/" + filename;
    }
    
    private static void ensureAppDataFolderExists() {
        String appDataFolderPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
        File appDataFolder = new File(appDataFolderPath);
        if (!appDataFolder.exists()) {
            appDataFolder.mkdirs();
        }
    }
    
    private static void ensureAppDataFolderExists2() {
        String appDataFolderPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
        File appDataFolder = new File(appDataFolderPath);
        if (!appDataFolder.exists()) {
            appDataFolder.mkdirs();
        }
    }

    private static String determineCategoryByTitle(String title) {
        title = title.toLowerCase(); // Convert title to lowercase for easier matching
        List<String> categories = getCategories();
    
        for (String category : categories) {
            if (title.contains(category.toLowerCase())) {
                return category;
            }
        }
        return "Uncategorized"; // Return "Uncategorized" if no category matches
    }
    
    
    public static void debugPrint(String debugInfo) {
        // Get the current stack trace element
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // The element at index 2 contains the caller method's details
        StackTraceElement element = stackTrace[2];
        
        // Get the line number
        int lineNumber = element.getLineNumber();
        
        // Print the debug information with the line number
        System.out.println("Debug line: " + lineNumber + " - " + debugInfo);
    }

  

    private static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(dataSheetFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Update displayResult to take in category as a parameter
 
    public static List<String> getCategories() {
        return Arrays.asList(
            "parts",   "Watch Movement Holder","band", "Collet", "Balance Assembly","Wristwatch Movement", "Wristwatch",
            "Pocket Watch Movement", "pocket watch case",
              "pocket watch", "|View All|"
        );
    }






private static void saveCache(Map<String, CachedData> cacheData) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_FILE_PATH))) {
            oos.writeObject(cacheData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, CachedData> loadCache() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CACHE_FILE_PATH))) {
            return (Map<String, CachedData>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Cache doesn't exist or couldn't be loaded
            return null;
        }
    }

    private static boolean needsRefresh(Map<String, CachedData> cachedDataMap) {
        // Implement logic to determine if the cache is stale and needs to be refreshed
        return false;
    }

    static class CachedData implements Serializable {
        String title;
        String cost;
        String price;
        // Add other fields you need to cache

        // Constructor, getters, setters, etc.
    }



    private static Map<String, CachedData> fetchDataFromGoogleSheets() {
        Map<String, CachedData> dataMap = new HashMap<>();
        System.out.println("Fetching data from Google Sheets..."); // Debugging line
        // Your logic to fetch data from Google Sheets and populate dataMap
        return dataMap;
    }

    
}


