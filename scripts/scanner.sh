#!/bin/bash
echo -e "\n"
echo "- United States LSAT Curricumlus Legal Edition 1.21 -"
echo ""
DATE=$(date +%m-%d-%Y) PERIMIUM=$(date +%r)
echo "Time on System: $DATE - $PERIMIUM"
echo -e "\n"

read -sp "Enter admin password: " PASSWORD

echo -e "\n"

echo -e "1. Updating Linux System: [apt-get update]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get update -y)
echo -e "\n"
echo "   - Updated Linux System."
echo -e "\n"
sleep 5 

echo -e "2. Upgrading Linux System: [apt-get upgrade]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get upgrade -y)  
echo -e "\n"
echo "   - Upgraded Linux System."
echo -e "\n"
sleep 5

echo -e "3. Installing ClamAV on Linux System: [apt-get install -y clamav clamav-daemon]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get install -y clamav clamav-daemon)
if [[ "$VALUE" == *"clamav is already the newest version"* ]]; then
  SIEVE=$(echo $VALUE | awk -F'[()]' '{print $2; exit}')
  echo -e "\n"
  echo "   - Latest ClamAV already installed: ($SIEVE)"
fi
echo -e "\n"
sleep 5

echo -e "4. Stopping & Configuring ClamAV on Linux System: [systemctl stop clamav-freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S systemctl stop clamav-freshclam)
if [[ "$VALUE" == "" ]]; then
  echo -e "\n"
  echo "   - ClamAV stopped by systemctl call.";
fi
echo -e "\n"
sleep 5

echo -e "5. Updating ClamAV on Linux System: [freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S freshclam)
if [[ "$VALUE" == *"ClamAV update process started"* ]]; then
  echo -e "\n"
  echo "   - ClamAV updated its database.";
fi
echo -e "\n"
sleep 5

echo -e "6. Starting ClamAV on Linux System: [systemctl start clamav-freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S systemctl start clamav-freshclam)
if [[ "$VALUE" == *"clamav-freshclam.service failed"* ]]; then
  echo -e "\n"
  echo "   - ClamAV failed to start by systemctl call."
else
  echo -e "\n"	
  echo "   - ClamAV started by systemctl control."
fi
echo -e "\n"
sleep 5

echo -e "7. Checking ClamAV on Linux System: [systemctl status clamav-freshclam]"
VALUE=$(echo "$PASSWORD" | sudo -S systemctl status clamav-freshclam)
if [[ "$VALUE" == *"Active: active (running)"* ]]; then
  echo -e "\n"
  echo "   - ClamAV initialization verified by systemctl call.";
fi
echo -e "\n"
sleep 5

echo -e "8. Starting Full System Scan on Linux System: [clamscan]"
VALUE=$(echo "$PASSWORD" | sudo -S clamscan -r -i ~ | tee clamav_scan_results.legal.lsat.txt)

command | while read -r VALUE; do
if [[ "$VALUE" == *"Infected files:"* ]]; then
  COUNT=$(echo "$VALUE" | tr -dc '0-9')
  echo "Infected files: $COUNT"
  echo -e "\n"
else
  echo "File scan complete."
  echo -e "\n"
fi
done

sleep 5
