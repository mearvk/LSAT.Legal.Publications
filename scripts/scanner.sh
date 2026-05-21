#!/bin/bash

read -sp "Enter administrator password: " PASSWORD

echo "1. Updating Linux System \n\r"
echo $PASSWORD | sudo -S apt-get update

echo "2. Upgrading Linux System \n\r"
echo $PASSWORD | sudo -S apt-get upgrade

echo "3. Installing ClamAV on Linux System \n\r"
echo $PASSWORD | sudo -S apt install -y clamav clamav-daemon

echo "4. Stopping & Configuring ClamAV on Linux System \n\r"
echo $PASSWORD | sudo -S systemctl stop clamav-freshclam

echo "5. Updating ClamAV on Linux System \n\r"
echo $PASSWORD | sudo -S freshclam

echo "6. Starting ClamAV on Linux System \n\r"
echo $PASSWORD | sudo -S systemctl start clamav-freshclam

echo "7. Starting Full System Scan on Linux System \n]r"
echo $PASSWORD | sudo -S clamscan -r -i --exclude-dir=\"^/sys\" / > clamav_scan_results.legal.lsat.txt