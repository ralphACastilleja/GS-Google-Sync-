greetings...


# Project README

This program helps you sort your Google Sheets or your eBay data. Below is a description of each file and its purpose.

## Existing Files

### __01_App.java
This file contains the main entry point of the application. It handles Google Sheets and Google Drive API authentication, loads previously saved links, and sets up the main GUI elements. It also provides functionality for processing Google Sheets links and handling folder links.

### _02_gui.java
This file defines the GUI components and layout, including text fields for input, buttons for actions like entering links, viewing sorted lists, and processing folders not yer implmeneted . It also includes methods for cleaning and retrieving links.

### _03_DataSheet.java
This file handles displaying and updating the data sheet GUI. It shows the titles and occurrences of items from Google Sheets, allowing users to add and remove links.

### _04_BrandSheet.java
This file manages the brand sheet GUI, displaying the brands and their occurrences from Google Sheets data. It updates the display based on new data and allows for interaction with the user.

### _05_EbayApi.java
This file serves as the entry point for the eBay API-related functionality. It initializes the eBay API GUI.

### _06_EbayApiGUI.java
This file sets up the eBay API GUI, including buttons for choosing CSV files, displaying information, and processing turnover time. It processes CSV files to calculate brand averages and occurrences.

### _07_AvgP.java
This file contains utility methods for processing CSV files to calculate brand occurrences and average prices.

### _08_TunT.java
This file manages the GUI for displaying turnover time data. It includes buttons for file selection, information display, and returning to the average price display.

### _09_BrandList.java
This file provides a list of brands, brands with turnover time, and the categories we can search for used in the application.


### _10_TTCalc.java
This file calculates turnover times for each brand based on current listings and completed orders. It processes CSV data, matches listings to orders, calculates turnover times, and updates average turnover times.

### _11_TestingTT.java
This file is used for testing purposes. It loads and stores listings from a CSV file, fetches specific start dates for items, and includes methods for getting brands and categories from titles.

### _12_Testing2.java
This file checks sold items and calculates average turnover times. It reads completed orders, finds matches with stored listings, calculates turnover times for brands, and writes the results to a file.

### _13_BrandTT.java
This file calculates the average turnover time for each brand based on completed orders. It reads CSV data, matches listings to orders, and calculates average turnover times for brands.

## How to Use

1. Run `__01_App.java` to start the main application.
2. Use the GUI to enter Google Sheets links, view sorted lists, and process folder links.
3. To work with eBay data, run `_05_EbayApi.java` to start the eBay API-related functionality.
4. Use `08_TunT.java` to display turnover time data and switch between different views.
5. For testing and data processing, use `_10_TTCalc.java`, `_11_TestingTT.java`, `_12_Testing2.java`, and `_13_BrandTT.java` as needed to calculate and verify turnover times and other metrics.


