package com.example;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class _02_gui {
    private static JTextField textField;
    private static JButton enterButton;
    private static JButton viewSortedListButton;
    private static JButton processFolderButton; // New button for processing folder link
    private static JButton backButton; // New back button
    private static JLabel instructionsLabel; // Make this a static field
    private static JButton infoButton; // Info button


    static {
        // Set the FlatLaf Dark look and feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createGUI(JFrame parentFrame) {
        JFrame frame = new JFrame("Google Sheets Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(800, 600);
        frame.setLayout(new GridBagLayout());

        frame.setLocationRelativeTo(parentFrame);

        // Load the image
        String imagePath = "/3.png"; 
        try {
            InputStream resourceStream = _02_gui.class.getResourceAsStream(imagePath);
            BufferedImage myPicture = ImageIO.read(resourceStream);
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            picLabel.setBounds(1250, 900000, myPicture.getWidth(), myPicture.getHeight());
            frame.add(picLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        instructionsLabel = new JLabel("Copy and paste your Google Sheets or Drive folder link.");
        instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        instructionsLabel.setHorizontalAlignment(JLabel.CENTER);

        // Create the text field for typing
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setPreferredSize(new Dimension(200, 30)); // Smaller text field

        // Create the "Enter" button
        enterButton = new JButton("Enter");
        enterButton.setFont(new Font("Arial", Font.PLAIN, 16));
        enterButton.setPreferredSize(new Dimension(80, 30)); // Set a custom size for the button

        // Create the "Info" button
infoButton = new JButton("Info");
infoButton.setFont(new Font("Arial", Font.PLAIN, 16));
infoButton.setPreferredSize(new Dimension(80, 30)); // Set a custom size for the button
infoButton.addActionListener(new ActionListener() {
    @Override



    public void actionPerformed(ActionEvent e) {
        showInfoDialog();
    }
});

// Add the "Info" button to the frame





JButton themeButton = new JButton("Theme");
themeButton.setFont(new Font("Arial", Font.PLAIN, 16));
themeButton.setPreferredSize(new Dimension(100, 25)); // Custom size for the button
themeButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
 

        UIManager.LookAndFeelInfo[] themes = {
            new UIManager.LookAndFeelInfo("Flat Dark", FlatDarkLaf.class.getName()),
            new UIManager.LookAndFeelInfo("Flat Light", FlatLightLaf.class.getName()),
            new UIManager.LookAndFeelInfo("Flat IntelliJ", FlatIntelliJLaf.class.getName()),
            new UIManager.LookAndFeelInfo("Flat Darcula", FlatDarculaLaf.class.getName()),
       //Dosent work  new UIManager.LookAndFeelInfo("Flat Material Dark", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerContrastIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Material Light", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme"),
       //Dosent work new UIManager.LookAndFeelInfo("Flat Material Oceanic", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicContrastIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Solarized Light", "com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Solarized Dark", "com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Monokai Pro", "com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Arc", "com.formdev.flatlaf.intellijthemes.FlatArcIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Arc Dark", "com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme"),
            new UIManager.LookAndFeelInfo("Flat One Dark", "com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Dracula", "com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme"),
            //Dosent work        new UIManager.LookAndFeelInfo("Flat GitHub", "com.formdev.flatlaf.intellijthemes.FlatGitHubIJTheme"),
             //Dosent work       new UIManager.LookAndFeelInfo("Flat GitHub Dark", "com.formdev.flatlaf.intellijthemes.FlatGitHubDarkIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Carbon", "com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Cobalt 2", "com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme"),
            new UIManager.LookAndFeelInfo("Flat Gruvbox Dark", "com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Hiberbee Dark", "com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme"),
            new UIManager.LookAndFeelInfo("Flat High Contrast", "com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Nord", "com.formdev.flatlaf.intellijthemes.FlatNordIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Solarized Dark", "com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Solarized Light", "com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Monokai", "com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Material Darker", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Material Palenight", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightIJTheme"),
            new UIManager.LookAndFeelInfo("Flat Material Deep Ocean", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme")
        };
        


String [] themeNames = new String[themes.length]; 

for (int i = 0; i < themes.length; i++){ 
    themeNames[i] = themes[i].getName();
}

String selectedTheme = (String) JOptionPane.showInputDialog(
frame, 
"Choose a theme:",
"Theme Selector", 
JOptionPane.PLAIN_MESSAGE, 
null, 
themeNames, 
themeNames[0]);


if (selectedTheme != null) {
    for (UIManager.LookAndFeelInfo theme : themes) {
        if (theme.getName().equals(selectedTheme)) {
            try {
                UIManager.setLookAndFeel(theme.getClassName());
                SwingUtilities.updateComponentTreeUI(frame);
                frame.revalidate();
                frame.repaint();

                // Save the selected theme
                saveSelectedTheme(theme.getClassName());
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
}
});


        // Create the "View Sorted List" button
        viewSortedListButton = new JButton("View Sorted List");
        viewSortedListButton.setFont(new Font("Arial", Font.PLAIN, 16));
        viewSortedListButton.setPreferredSize(new Dimension(300, 30)); // Set a custom size for the button

        // Create the "Process Folder" button
        processFolderButton = new JButton("Process Folder");
        processFolderButton.setFont(new Font("Arial", Font.PLAIN, 16));
        processFolderButton.setPreferredSize(new Dimension(150, 30)); // Set a custom size for the button

    /*   // Create the "Back" button
        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setPreferredSize(new Dimension(80, 30)); // Set a custom size for the button
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the current frame (_02_gui)
                new StartingGUI(); // Start a new instance of _00_StartingGUI
            }
        });
        */ 
        // Set up the layout manager
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 10, 5); // Add some padding
        frame.add(instructionsLabel, gbc);

       // gbc.gridx = 2; // Column position for the info button
//gbc.gridy = 9;
//frame.add(infoButton, gbc);

gbc.gridy = 3;
gbc.insets = new Insets(5, 5, 10, 5); // Add some padding
frame.add(viewSortedListButton, gbc);


        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 10, 5); // Add some padding
        frame.add(textField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 10, 5); // Add some padding
        frame.add(infoButton, gbc);

        gbc.gridy = 4; // Add the theme button right below the info button
frame.add(themeButton, gbc);

        // Add an event listener to the text field
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = textField.getText();
                if (!userInput.isEmpty()) {
                    System.out.println("User input: " + userInput);
                    // Process the user input here
                    textField.setText(""); // Clear the text field after processing
                }
            }
        });

        // Add the "Enter" button to the frame
        gbc.gridx = 1; // Moves the button to the first column
        gbc.gridy = 2;
        frame.add(enterButton, gbc);

        // Add the "Process Folder" button to the frame
        gbc.gridx = 90; // Moves the button to the first column
        gbc.gridy = 90;

        // Add the "Back" button to the frame
        gbc.gridx = 1; // Moves the button to the first column
        gbc.gridy = 4;
       // frame.add(backButton, gbc);

        frame.setVisible(true);

        // Debug statements
        System.out.println("Enter Button Initialized: " + (enterButton != null));
        System.out.println("View Sorted List Button Initialized: " + (viewSortedListButton != null));
    }

    public static String cleanLink(String dirtyLink) {
        int startIndex = dirtyLink.indexOf("/d/") + 3; // Index after "/d/"
        int endIndex = dirtyLink.indexOf("/edit"); // Index before "/edit"
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return dirtyLink.substring(startIndex, endIndex);
        }
        return null; // Handle if the link format is unexpected
    }

    public static String getTypedLink() {
        return textField.getText();
    }

    public static void clearTypedLink() {
        textField.setText("");
    }

    public static JButton getEnterButton() {
        return enterButton;
    }

    public static JButton getViewSortedListButton() {
        return viewSortedListButton;
    }

    public static JButton getProcessFolderButton() {
        return processFolderButton;
    }

    public static void setVisibility(boolean visible) {
        textField.setVisible(visible);
        enterButton.setVisible(visible);
        viewSortedListButton.setVisible(visible);
        processFolderButton.setVisible(visible);
        instructionsLabel.setVisible(visible);
    }

    private static void showInfoDialog() {
        String infoMessage = "This";
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
    

    private static String getAppDataFilePath(String filename) {
        String appDataPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
        return appDataPath + "/" + filename;
    }
    

    static {
        try {
            String selectedThemeClassName = loadSelectedTheme();
            UIManager.setLookAndFeel(selectedThemeClassName);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf()); // Fallback to Flat Dark theme
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void saveSelectedTheme(String themeClassName) {
        ensureAppDataFolderExists();
        Properties props = new Properties();
        props.setProperty("theme", themeClassName);
        try (FileOutputStream out = new FileOutputStream(getAppDataFilePath("theme.properties"))) {
            props.store(out, "App Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

private static String loadSelectedTheme() {
    ensureAppDataFolderExists();
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream(getAppDataFilePath("theme.properties"))) {
        props.load(in);
        return props.getProperty("theme", FlatDarkLaf.class.getName()); // Default to Flat Dark theme
    } catch (IOException e) {
        e.printStackTrace();
        return FlatDarkLaf.class.getName(); // Default to Flat Dark theme if there is an issue
    }
}

private static void ensureAppDataFolderExists() {
    String appDataFolderPath = System.getProperty("user.home") + "/Downloads/GoogleSyncData";
    File appDataFolder = new File(appDataFolderPath);
    if (!appDataFolder.exists()) {
        appDataFolder.mkdirs();
    }
}


public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        createGUI(null);
    });
}
}
