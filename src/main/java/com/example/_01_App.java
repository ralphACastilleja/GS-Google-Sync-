package com.example;

import com.example._03_DataSheet.CachedData;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import javax.swing.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.drive.DriveScopes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Point;
import java.io.*;
import java.util.concurrent.Callable;
import java.util.Map;

//ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute();



/*
 * 
 * 
 * 
 * 
  jpackage --input "/Users/ralphacastilleja/Desktop/Google Sync Code " \
    --main-jar "Google Sync Code .jar" \
    --name "Google Syne 1.01" \
    --icon "/Users/ralphacastilleja/Downloads/JDS.icns" \
    --main-class "com.example._01_App" \
    --type app-image \
    --app-version "1.0" \
    --dest "/Users/ralphacastilleja/Downloads/output"


 * 
 * 
 * 
 * 
 * 
 * 
 */


public class _01_App {
    private static final String APPLICATION_NAME = "Google Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credential533.json";
    private static final String CACHE_FILE_PATH = System.getProperty("user.home") + "/Downloads/GoogleSyncData/cache.dat";
    private static final int MAX_RETRIES = 5;
private static final int INITIAL_BACKOFF_MS = 1000; // 1 second

    private static List<String> links = new ArrayList<>();
    public static Drive driveService;
    private static JFrame mainFrame;

    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = _01_App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            ensureCacheFileExists();  // Ensure cache file exists before loading links
            links = loadLinks();

            mainFrame = new JFrame("sOorting Project");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1, 1);
            _02_gui.createGUI(mainFrame);
            mainFrame.setVisible(false);

            _02_gui.getEnterButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String dirtyLink = _02_gui.getTypedLink();
                    if (!dirtyLink.isEmpty()) {
                        System.out.println("User entered link: " + dirtyLink); // Debugging line
            
                        if (dirtyLink.contains("/folders/")) {
                            // Handle Google Drive folder link
                            System.out.println("Detected Google Drive folder link."); // Debugging line
                            String folderId = _01_App.extractFolderId(dirtyLink);
                            if (folderId != null) {
                                System.out.println("Extracted Folder ID: " + folderId); // Debugging line
                                List<String> sheetLinks = _01_App.getGoogleSheetLinksInFolder(folderId);
                                for (String link : sheetLinks) {
                                    if (!links.contains(link)) {
                                        links.add(link);
                                        System.out.println("Added Google Sheets link from folder: " + link); // Debugging line
                                    } else {
                                        System.out.println("Link already exists: " + link); // Debugging line
                                    }
                                }
                                _02_gui.clearTypedLink(); // Clear the text field after processing
                                _01_App.saveLinks(links); // Save updated list of links
                                _03_DataSheet.updateDisplay(); // Update the DataSheet display
                                _04_BrandSheet.updateDisplay(); // Update the BrandSheet display
                                JOptionPane.showMessageDialog(_01_App.getMainFrame(), "Processed folder and updated links.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(_01_App.getMainFrame(), "Invalid folder link.", "Error", JOptionPane.ERROR_MESSAGE);
                                System.out.println("Failed to extract Folder ID from the link."); // Debugging line
                            }
                        } else if (dirtyLink.contains("/spreadsheets/")) {
                            // Handle individual Google Sheets link
                            System.out.println("Detected Google Sheets link."); // Debugging line
                            String cleanLink = _02_gui.cleanLink(dirtyLink);
                            if (cleanLink != null) {
                                if (links.contains(cleanLink)) {
                                    JOptionPane.showMessageDialog(_01_App.getMainFrame(), "This link has already been added.", "Error", JOptionPane.ERROR_MESSAGE);
                                    System.out.println("Link already exists: " + cleanLink); // Debugging line
                                } else {
                                    links.add(cleanLink);
                                    System.out.println("Added clean Google Sheets link: " + cleanLink); // Debugging line
                                    _02_gui.clearTypedLink(); // Clear the text field after processing
                                    _01_App.saveLinks(links); // Save updated list of links
                                    _03_DataSheet.updateDisplay(); // Update the DataSheet display
                                    _04_BrandSheet.updateDisplay(); // Update the BrandSheet display
                                }
                            } else {
                                JOptionPane.showMessageDialog(_01_App.getMainFrame(), "Invalid link format.", "Error", JOptionPane.ERROR_MESSAGE);
                                System.out.println("Failed to clean Google Sheets link: " + dirtyLink); // Debugging line
                            }
                        } else {
                            JOptionPane.showMessageDialog(_01_App.getMainFrame(), "Unsupported link type.", "Error", JOptionPane.ERROR_MESSAGE);
                            System.out.println("Unsupported link entered: " + dirtyLink); // Debugging line
                        }
                    } else {
                        System.out.println("No link entered."); // Debugging line
                    }
                }
            });

            _02_gui.getViewSortedListButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("View Sorted List button clicked!");  // Debugging line
                    _03_DataSheet.displayDataSheetGUI(links);
            
                    // Disable the button after the first click
                    _02_gui.getViewSortedListButton().setEnabled(false);
            
                    // Position the BrandSheet to the right of DataSheet
                    JFrame dataSheetFrame = _03_DataSheet.getFrame();
                    if (dataSheetFrame != null) {
                        Point dataSheetLocation = dataSheetFrame.getLocation();
                        Dimension dataSheetSize = dataSheetFrame.getSize();
                        int brandSheetX = dataSheetLocation.x + dataSheetSize.width;
                        int brandSheetY = dataSheetLocation.y;
                        _04_BrandSheet.displayBrandSheetGUI(links);
                        JFrame brandSheetFrame = _04_BrandSheet.getFrame();
                        if (brandSheetFrame != null) {
                            brandSheetFrame.setLocation(brandSheetX, brandSheetY);
                        }
                    } else {
                        _04_BrandSheet.displayBrandSheetGUI(links);
                    }
                    mainFrame.setVisible(false); // Hide the main frame
                }
            });

            _02_gui.getProcessFolderButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // String folderLink = _02_gui.getFolderLink();
                    // if (!folderLink.isEmpty()) {
                    //     String folderId = extractFolderId(folderLink);
                    //     if (folderId != null) {
                    //         List<String> sheetLinks = getGoogleSheetLinksInFolder(folderId);
                    //         for (String link : sheetLinks) {
                    //             if (!links.contains(link)) {
                    //                 links.add(link);
                    //             }
                    //         }
                    //         saveLinks(links);
                    //         _03_DataSheet.updateDisplay();
                    //         _04_BrandSheet.updateDisplay();
                    //         JOptionPane.showMessageDialog(mainFrame, "Processed folder and updated links.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    //     } else {
                    //         JOptionPane.showMessageDialog(mainFrame, "Invalid folder link.", "Error", JOptionPane.ERROR_MESSAGE);
                    //     }
                    // }
                }
            });

        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace();
        }
    }

    public static void ensureCacheFileExists() {
        String cacheFilePath = CACHE_FILE_PATH;
        File cacheFile = new File(cacheFilePath);
        if (!cacheFile.exists()) {
            try {
                ensureAppDataFolderExists();  // Make sure the folder exists
                cacheFile.createNewFile();    // Create the cache file
                System.out.println("Cache file created at: " + cacheFilePath);  // Debugging line
            } catch (IOException e) {
                System.out.println("Failed to create cache file: " + e.getMessage());  // Debugging line
            }
        } else {
            System.out.println("Cache file already exists at: " + cacheFilePath);  // Debugging line
        }
    }


    public static void saveCache(Map<String, CachedData> cacheData) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_FILE_PATH))) {
            oos.writeObject(cacheData);
            System.out.println("Cache saved successfully."); // Debugging line
        } catch (IOException e) {
            System.out.println("Failed to save cache: " + e.getMessage()); // Debugging line
            e.printStackTrace();
        }
    }


    public static Map<String, CachedData> loadCache() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CACHE_FILE_PATH))) {
            System.out.println("Cache loaded successfully."); // Debugging line
            return (Map<String, CachedData>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to load cache or cache is not found: " + e.getMessage()); // Debugging line
            return null;
        }
    }


    public static String extractFolderId(String folderLink) {
        int startIndex = folderLink.indexOf("/folders/") + 9; // Index after "/folders/"
        int endIndex = folderLink.indexOf("?", startIndex); // Index before any query parameters
        if (startIndex != -1) {
            if (endIndex != -1) {
                String folderId = folderLink.substring(startIndex, endIndex);
                System.out.println("Extracted folder ID: " + folderId); // Debugging line
                return folderId;
            } else {
                String folderId = folderLink.substring(startIndex);
                System.out.println("Extracted folder ID: " + folderId); // Debugging line
                return folderId;
            }
        }
        return null; // Handle if the link format is unexpected
    }

    public static List<String> getGoogleSheetLinksInFolder(String folderId) {
        List<String> sheetLinks = new ArrayList<>();
        try {
            FileList result = driveService.files().list()
                    .setQ("'" + folderId + "' in parents and mimeType='application/vnd.google-apps.spreadsheet'")
                    .setFields("files(id, name)")
                    .execute();
            if (result.isEmpty()) {
                System.out.println("No Google Sheets found in the folder."); // Debugging line
            }
            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                String fileId = file.getId();
                System.out.println("Found Google Sheet with ID: " + fileId + " and name: " + file.getName()); // Debugging line
                sheetLinks.add("https://docs.google.com/spreadsheets/d/" + fileId + "/edit");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sheetLinks;
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

    public static List<String> loadLinks() {
        List<String> links = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(getAppDataFilePath("links.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                links.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }

    public static JFrame getMainFrame() {
        return mainFrame;
    }

    public static List<String> getGoogleSheetLinksFromDrive(String folderLink) {
        List<String> sheetLinks = new ArrayList<>();
        String folderId = extractFolderId(folderLink);
    
        if (folderId != null) {
            try {
                FileList result = driveService.files().list()
                    .setQ("'" + folderId + "' in parents and mimeType='application/vnd.google-apps.spreadsheet'")
                    .setFields("files(id, name)")
                    .execute();
                
                for (com.google.api.services.drive.model.File file : result.getFiles()) {
                    String fileId = file.getId();
                    String sheetLink = "https://docs.google.com/spreadsheets/d/" + fileId + "/edit";
                    sheetLinks.add(sheetLink);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sheetLinks;
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

    public static <T> T executeWithRetry(Callable<T> task) throws IOException, GeneralSecurityException {
        int maxAttempts = 3;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                return task.call();
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 429) { // Rate limit exceeded
                    attempt++;
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(1000); // Wait for 1 second before retrying
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Interrupted while waiting to retry", ie);
                        }
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new IOException("An unexpected error occurred during retry execution", e);
            }
        }
        throw new IOException("Failed to execute task after " + maxAttempts + " attempts");
    }
}    