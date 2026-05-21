i#!/bin/bash

# Configuration
SCAN_TARGET="/"
LOG_FILE="clamav_scan_results.legal.lsat.txt"

echo "🚀 Starting ClamAV scan on $SCAN_TARGET..."

# Run clamscan
# -r: recursive
# -i: only print infected files
# -l: log to file
clamscan -r -i "$SCAN_TARGET" -l "$LOG_FILE"

# Capture the exit status
SCAN_RESULT=$?

echo "---------------------------------------"

if [ $SCAN_RESULT -eq 0 ]; then
    echo "✅ SUCCESS: No threats detected."
    echo "Summary available at: $LOG_FILE"
    echo "Proceeding with next tasks..."
    # Insert your next command here
    exit 0

elif [ $SCAN_RESULT -eq 1 ]; then
    echo "❌ CRITICAL: Malware detected!"
    echo "Review infected files in: $LOG_FILE"
    echo "Operation halted for safety."
    exit 1

else
    echo "⚠️ ERROR: ClamAV failed to complete the scan (Exit code: $SCAN_RESULT)."
    echo "Check if the directory exists or if you have sufficient permissions."
    exit 2
fi
