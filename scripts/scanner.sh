#!/bin/bash

read -sp "Enter administrator password: " PASSWORD

echo -e "\n"

echo -e "1. Updating Linux System: [apt-get update]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get update -y)
echo -e "\n"
echo " - Updated Linux System."
echo -e "\n"

echo -e "2. Upgrading Linux System: [apt-get upgrade]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get upgrade -y)  
echo -e "\n"
echo " - Upgraded Linux System."
echo -e "\n"

echo -e "3. Installing ClamAV on Linux System: [apt-get install -y clamav clamav-daemon]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get install -y clamav clamav-daemon)
if [[ "$VALUE" == *"clamav is already the newest version"* ]]; then
  SIEVE=$(echo $VALUE | awk -F'[()]' '{print $2; exit}')
  echo -e "\n"
  echo " - Latest ClamAV already installed: ($SIEVE)"
fi
echo -e "\n"

echo -e "4. Stopping & Configuring ClamAV on Linux System: [systemctl stop clamav-freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S systemctl stop clamav-freshclam)
if [[ "$VALUE" == "" ]]; then
  echo -e "\n"
  echo " - ClamAV stopped by systemctl call.";
fi
echo -e "\n"

echo -e "5. Updating ClamAV on Linux System: [freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S freshclam)
if [[ "$VALUE" == *"ClamAV update process started"* ]]; then
  echo -e "\n"
  echo " - ClamAV updated its database.";
fi
echo -e "\n"

echo -e "6. Starting ClamAV on Linux System: [systemctl start clamav-freshclam]"
if [[ "$VALUE" == *"Active: active (running)"* ]]; then
  echo -e "\n"
  echo " - ClamAV started by systemctl call.";
fi
echo "$PASSWORD" | sudo -S systemctl start clamav-freshclam
echo -e "\n"

echo -e "7. Checking ClamAV on Linux System: [systemctl status clamav-freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S systemctl status clamav-freshclam)
if [[ "$VALUE" == *"Active: active (running)"* ]]; then
  echo -e "\n"
  echo " - ClamAV binary verified by systemctl call.";
fi
echo -e "\n"

echo -e "8. Starting Full System Scan on Linux System: [clamscan -r -i --exclude-dir=\"^/sys\" / > clamav_scan_results.legal.lsat.txt]"
echo "$PASSWORD" | sudo -S clamscan -r -i --exclude-dir=\"^/sys\" / > clamav_scan_results.legal.lsat.txt
echo -e "\n"
