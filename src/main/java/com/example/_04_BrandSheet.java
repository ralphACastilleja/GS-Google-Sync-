package com.example;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;



public class _04_BrandSheet {
    private static List<String> links = new ArrayList<>();
    private static final String APPLICATION_NAME = "sOorting";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static Sheets sheetsService;
    private static JFrame brandSheetFrame;
    private static Map<String, String> titleToCategoryMap = new HashMap<>();

    public static void displayBrandSheetGUI(List<String> existingLinks) {
        links = existingLinks; // Use existing shared links
        brandSheetFrame = new JFrame("Brand Sheet");
        brandSheetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        brandSheetFrame.setSize(550, 500);
        brandSheetFrame.setLayout(new BorderLayout());

        displayResult(getUpdatedCountMap(), brandSheetFrame);

        brandSheetFrame.setVisible(true);
    }

    public static JFrame getFrame() {
        return brandSheetFrame;
    }

    private static Map<String, Integer> getUpdatedCountMap() {
        return getUpdatedCountMap("|View All|"); // Default to all categories
    }

    private static Map<String, Integer> getUpdatedCountMap(String categoryFilter) {
        Map<String, Integer> updatedCountMap = new HashMap<>();
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, _01_App.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
    
            for (String spreadsheetId : links) {
                Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
                String sheetName = spreadsheet.getSheets().get(0).getProperties().getTitle();
    
                // Fetch the entire sheet
                ValueRange response = _01_App.executeWithRetry(() -> sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute());
                List<List<Object>> values = response.getValues();
                int brandColumnIndex = -1;
                int titleColumnIndex = -1;
    
                if (values != null && !values.isEmpty()) {
                    for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                        List<Object> row = values.get(rowIndex);
    
                        for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                            String cellValue = row.get(colIndex).toString().trim().toLowerCase();
                            if (cellValue.equals("brand")) {
                                brandColumnIndex = colIndex;
                            } else if (cellValue.equals("title")) {
                                titleColumnIndex = colIndex;
                            }
                        }
    
                        // Skip header row
                        if (rowIndex == 0 || brandColumnIndex == -1 || titleColumnIndex == -1) continue;
    
                        // Extract brand and title from the row
                        String brand = brandColumnIndex < row.size() ? row.get(brandColumnIndex).toString().trim() : "";
                        String title = titleColumnIndex < row.size() ? row.get(titleColumnIndex).toString().trim() : "";
                        String itemCategory = determineCategoryByTitle(title); // Determine the category from title
    
                        // Store the category in the map
                        titleToCategoryMap.put(title, itemCategory);
    
                        // Filter based on the provided category
                        if (categoryFilter.equals("|View All|") || itemCategory.equalsIgnoreCase(categoryFilter)) {
                            updatedCountMap.put(brand, updatedCountMap.getOrDefault(brand, 0) + 1);
                        }
                    }
                }
            }
        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace();
        }
        return updatedCountMap;
    }
     
    private static void findAndPrintCostAndPrice(String spreadsheetId, String sheetName) throws GeneralSecurityException {
        try {
            // Using executeWithRetry to handle transient errors such as rate limiting
            ValueRange response = _01_App.executeWithRetry(() -> sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute());
            List<List<Object>> values = response.getValues();
    
            int costColumnIndex = -1;
            int priceColumnIndex = -1;
            int titleRowIndex = -1;
    
            if (values != null && !values.isEmpty()) {
                outerLoop:
                for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                    List<Object> row = values.get(rowIndex);
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        String cellValue = row.get(colIndex).toString().trim().toLowerCase();
                        if (cellValue.equals("cost")) {
                            costColumnIndex = colIndex;
                            titleRowIndex = rowIndex + 1; // Adjust for 1-based index
                        } else if (cellValue.equals("price") || cellValue.equals("price ($)")) {
                            priceColumnIndex = colIndex;
                            titleRowIndex = rowIndex + 1; // Adjust for 1-based index
                        }
                    }
                    if (costColumnIndex != -1 && priceColumnIndex != -1) {
                        break outerLoop; // Exit the loop once both columns are found
                    }
                }
            }
    
            if (costColumnIndex == -1 || priceColumnIndex == -1) {
                System.out.println("DEBUG line 167: Cost or Price column not found.");
                return;
            }
    
            // Fetch data from the identified columns below the headers
            for (int i = titleRowIndex; i < values.size(); i++) {
                List<Object> row = values.get(i);
                String cost = costColumnIndex < row.size() ? row.get(costColumnIndex).toString().trim() : "";
                String price = priceColumnIndex < row.size() ? row.get(priceColumnIndex).toString().trim() : "";
    
                // Calculate and print margin
                double margin = calculateMargin(cost, price);
                System.out.println("Cost: " + cost + ", Price: " + price + ", Margin: " + margin);
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
        public static Map<String, String> getTitleToCategoryMap() {
            return titleToCategoryMap;
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
        
        private static void displayResult(Map<String, Integer> countMap, JFrame frame) {
            ensureAppDataFolderExists2();
            System.out.println("Displaying results: " + countMap);
            JPanel resultPanel = new JPanel(new BorderLayout());
        
            // Create a JTable to display brands, occurrences, average margin, average price, and average cost
            String[] columnNames = {"Brand", "Occurrences", "Average Margin", "Average Price", "Average Cost"};
            Object[][] data = new Object[countMap.size()][5]; // 5 columns now
            int index = 0;
        
            // Sort the countMap by occurrences in descending order
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(countMap.entrySet());
            sortedEntries.sort((e1, e2) -> {
                String brand1 = e1.getKey();
                String brand2 = e2.getKey();
        
                double avgMargin1 = calculateAverageMargin(brand1);
                double avgMargin2 = calculateAverageMargin(brand2);
        
                return Double.compare(avgMargin2, avgMargin1);
            });
            Set<String> uniqueEntries = new HashSet<>();
        
            try (PrintWriter writer = new PrintWriter(new FileWriter(getAppDataFilePath("BrandSheet.txt"), true))) {
                for (Map.Entry<String, Integer> entry : sortedEntries) {
                    String brand = entry.getKey();
                    int occurrences = entry.getValue();
        
                    // Initialize totals for calculating averages
                    double totalMargin = 0.0;
                    double totalPrice = 0.0;
                    double totalCost = 0.0;
                    int marginCount = 0; // Only count valid margins
                    int priceCount = 0;  // Count all valid prices
        
                    // Iterate over all titles to calculate totals for the brand
                    for (Map.Entry<String, String> titleEntry : _03_DataSheet.titleToPriceMap.entrySet()) {
                        String title = titleEntry.getKey();
                        if (title.toLowerCase().contains(brand.toLowerCase())) {
                            try {
                                double price = Double.parseDouble(titleEntry.getValue());
                                totalPrice += price;
                                priceCount++;
        
                                String costStr = _03_DataSheet.titleToCostMap.getOrDefault(title, "0.0");
                                if (!costStr.equals("0.0")) { // Only calculate margin if cost is available
                                    double cost = Double.parseDouble(costStr);
                                    double margin = price - cost;
        
                                    totalCost += cost;
                                    totalMargin += margin;
                                    marginCount++;
                                }
                            } catch (NumberFormatException e) {
                                // Handle parsing errors, skip invalid entries
                                System.out.println("Error parsing price or cost for title: " + title);
                                System.out.println("Raw price value: " + titleEntry.getValue());
                                System.out.println("Raw cost value: " + _03_DataSheet.titleToCostMap.getOrDefault(title, "N/A"));
                            }
                        }
                    }
        
                    // Calculate averages
                    String avgMargin = marginCount > 0 ? String.format("%.2f", totalMargin / marginCount) : "N/A";
                    String avgPrice = priceCount > 0 ? String.format("%.2f", totalPrice / priceCount) : "N/A";
                    String avgCost = marginCount > 0 ? String.format("%.2f", totalCost / marginCount) : "N/A";
        
                    if (!uniqueEntries.contains(brand)) {
                        uniqueEntries.add(brand);
        
                        data[index][0] = brand;
                        data[index][1] = occurrences;
                        data[index][2] = avgMargin;
                        data[index][3] = avgPrice;
                        data[index][4] = avgCost;
        
                        // Write to file
                        writer.println(brand + ", " + occurrences + ", " + avgMargin + ", " + avgPrice + ", " + avgCost);
        
                        index++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        
            // Remaining code to display the table in the GUI...
        
        
        
            // Remaining code to display the table in the GUI
            JTable table = new JTable(data, columnNames);
            table.setFillsViewportHeight(true);
            table.setShowGrid(true);
            table.setGridColor(Color.LIGHT_GRAY);
        
            // Set the preferred width for the columns
            table.getColumnModel().getColumn(0).setPreferredWidth(200); // Adjust for Brand
            table.getColumnModel().getColumn(1).setPreferredWidth(100); // Adjust for Occurrences
            table.getColumnModel().getColumn(2).setPreferredWidth(150); // Adjust for Average Margin
            table.getColumnModel().getColumn(3).setPreferredWidth(150); // Adjust for Average Price
            table.getColumnModel().getColumn(4).setPreferredWidth(150); // Adjust for Average Cost
        
            // Set a custom renderer to add lines between rows and change text size
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setFont(new Font("Arial", Font.PLAIN, 14)); // Set the font and size
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
                    return c;
                }
            };
        
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        
            JScrollPane scrollPane = new JScrollPane(table);
        
            // Add a titled border to the table
            scrollPane.setBorder(BorderFactory.createTitledBorder("Brands, Occurrences, Average Margin, Price, and Cost"));
        
            resultPanel.add(scrollPane, BorderLayout.CENTER);
        
            // Add the result panel to the frame
            frame.add(resultPanel, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        }
           
    
    public static void updateDisplay() {
        if (brandSheetFrame != null) {
            brandSheetFrame.getContentPane().removeAll();
            displayResult(getUpdatedCountMap(), brandSheetFrame);
            brandSheetFrame.revalidate();
            brandSheetFrame.repaint();
        }
    }

    private static double calculateAverageMargin(String brand) {
        double totalMargin = 0.0;
        int validEntries = 0;
    
        // Iterate over all titles to calculate the total margin for the brand
        for (Map.Entry<String, String> titleEntry : _03_DataSheet.titleToPriceMap.entrySet()) {
            String title = titleEntry.getKey();
            if (title.toLowerCase().contains(brand.toLowerCase())) {
                try {
                    double price = Double.parseDouble(titleEntry.getValue());
                    double cost = Double.parseDouble(_03_DataSheet.titleToCostMap.getOrDefault(title, "0.0"));
                    double margin = price - cost;
    
                    totalMargin += margin;
                    validEntries++;
                } catch (NumberFormatException e) {
                    // Handle parsing errors, skip invalid entries
                    System.out.println("Error parsing price or cost for title: " + title);
                }
            }
        }
    
        // Calculate and return the average margin
        return validEntries > 0 ? totalMargin / validEntries : 0.0;
    }
    

    private static void ensureAppDataFolderExists2() {
        String appDataFolderPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
        File appDataFolder = new File(appDataFolderPath);
        if (!appDataFolder.exists()) {
            appDataFolder.mkdirs();
        }
    }
    
    private static String getAppDataFilePath(String filename) {
        String appDataPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
        return appDataPath + "/" + filename;
    }
    

private static String determineCategoryByTitle(String title) {
    title = title.toLowerCase(); // Convert title to lowercase for easier matching

    // Define your categories here
    List<String> categories = Arrays.asList(
        "parts", "Watch Movement Holder", "band", "Collet", "Balance Assembly",
        "Wristwatch Movement", "Wristwatch", "Pocket Watch Movement",
        "pocket watch case", "pocket watch"
    );

    // Loop through the categories and check if the title contains any category name
    for (String category : categories) {
        if (title.contains(category.toLowerCase())) {
            return category;
        }
    }

    // If no category matches, return "Uncategorized"
    return "Uncategorized";
}

public static void updateDisplayWithCategory(String category) {
    if (brandSheetFrame != null) {
        brandSheetFrame.getContentPane().removeAll();
        displayResult(getUpdatedCountMap(category), brandSheetFrame);
        brandSheetFrame.revalidate();
        brandSheetFrame.repaint();
    }
}



    // Helper method to convert column index to letter(s)
    private static String getColumnLetter(int colIndex) {
        StringBuilder column = new StringBuilder();
        while (colIndex >= 0) {
            int remainder = colIndex % 26;
            column.insert(0, (char) (remainder + 'A'));
            colIndex = (colIndex / 26) - 1;
        }
        return column.toString();
    }
}
