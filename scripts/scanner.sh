#!/bin/bash

read -sp "Enter administrator password: " PASSWORD

echo $PASSWORD | sudo -S apt-get update
echo $PASSWORD | sudo -S apt-get upgrade
echo $PASSWORD | sudo -S apt install -y clamav clamav-daemon
echo $PASSWORD | sudo -S systemctl stop clamav-freshclam
echo $PASSWORD | sudo -S freshclam
echo $PASSWORD | sudo -S systemctl start clamav-freshclam
echo $PASSWORD | sudo -S clamscan -r -i --exclude-dir=\"^/sys\" / > clamav_scan_results.legal.lsat.txt