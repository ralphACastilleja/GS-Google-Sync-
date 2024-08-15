

# Project README

This program Uses Google's APIS in order to retrive data from the User To Store and Processes Data From google sheets.  

## Existing Files

### __01_App.java
This file contains the main entry point of the application. It handles Google Sheets and Google Drive API authentication, loads previously saved links, and sets up the main GUI elements. It also provides functionality for processing Google Sheets links and handling folder links.

### _02_gui.java
This file defines the GUI components and layout, including text fields for input, buttons for actions like entering links, viewing sorted lists, and processing folders not yer implmeneted . It also includes methods for cleaning and retrieving links.

### _03_DataSheet.java
This file handles displaying and updating the data sheet GUI. It shows the titles and occurrences of items from Google Sheets, allowing users to add and remove links.

### _04_BrandSheet.java
This file manages the brand sheet GUI, displaying the brands and their occurrences from Google Sheets data. It updates the display based on new data and allows for interaction with the user.
 returning to the average price display.


## How to Use

1. Run `__01_App.java` to start the main application.
2. Use the GUI to enter Google Sheets links, view sorted lists, and process folder links.
3. To work with eBay data, run `_05_EbayApi.java` to start the eBay API-related functionality.


