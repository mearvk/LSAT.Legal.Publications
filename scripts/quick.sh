#!/bin/bash

PASSWORD='$$Ironman1'

echo -e "3. Installing ClamAV on Linux System: [apt-get install -y clamav clamav-daemon]"
VALUE=$(echo "$PASSWORD" | sudo -S apt-get install -y clamav clamav-daemon)
if [[ "$VALUE" == *"clamav is already the newest version"* ]]; then
  SIEVE=$(echo $VALUE | awk -F'[()]' '{print $2; exit}')
  echo -e "\n"
  echo " - Latest ClamAV already installed: $SIEVE"
fi
echo -e "\n"
