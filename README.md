# Trade Enricher Service
A REST implementation for trade enrichment service

This guide walks you through the process of using the trade enrichement service, which provides the following functionalities:


1. Expose an API to enrich trade data (trade.csv) with product names from the static data file (product.csv)
translate the product_id into product_name
perform data validation
2. Ensure that date is a valid date in YYYYMMDD format, otherwise discard the row and log an error
if the product name is not available, we should still log the missing mapping and set the product Name as "Missing Product Name"

 #### Github Source Repo


Source Code for project is available to clone from below link,
> https://github.com/rraguram/trade-enricher-service.git

#### How to run this project

To run this, 

> mvn clean package

This should build and package the necessary project artefacts. Once done, then you should be able to run the application,

> java -jar trade-enricher-service-1.0-SNAPSHOT.jar

#### How to run the client to input the trade

To run the client, run curl command,

>curl -F data=@trade.csv "http://localhost:8080/api/v1/enrich/" -H "Content-Type:text/csv"

