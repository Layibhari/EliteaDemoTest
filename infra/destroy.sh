#!/bin/bash
echo "WARNING: This will destroy ALL infrastructure (EC2, security group, key pair)."
echo "This action is IRREVERSIBLE."
read -p "Type 'destroy-petclinic' to confirm: " confirmation

if [ "$confirmation" == "destroy-petclinic" ]; then
  terraform destroy
else
  echo "Confirmation text did not match. Aborting."
  exit 1
fi
