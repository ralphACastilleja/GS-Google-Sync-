package com.example;

public class DebugUtil {
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
}
