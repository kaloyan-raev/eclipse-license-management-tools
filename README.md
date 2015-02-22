# README #

### License file specification ###

The license file is a Java Properties file with the following keys

* Id - UUID
* Issuer - Who issued this license file. It could be the product vendor itself, or it could be a 3rd party license issuer. 
* Type - Evaluation|Commercial|Subscription|Perpetual|Custom
* ExpirationDate - YYYY-MM-DD
* ConcurrentUsers - number of concurrent users at the same time
* ProductId - UUID
* ProductName
* ProductVendor
* ProductVersions
* CustomerId
* CustomerName
* Signature - Base64-encoded String representation

It is allowed to have product-specific properties.

### Converting key file to byte array ###

Use the hexdump command to dump the file in decimal bytes:


```
#!bash

hexdump mydsa.pub -v -e '1/1 "%d" ", "'
```
