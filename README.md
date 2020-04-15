# HKConnector

HKConnector is a full fledged application which can be deployed and leveraged as a black box app to any up and running server for any bulk processing. It is a Spring Boot 2, Java 11 based application which leverages Google Sheets API v4 and related authentication processes to help setup bulk processing with the shared excel sheet.

## Installation

Create a google developer account and enable sheet api. 
Whitelist the following redirection url :

```
<Scheme><base uri with port>/hkc/process/google/sheet/create
<Scheme><base uri with port>/hkc/process/google/sheet/start
```

Copy paste the credentials in credential.json file renaming credential.json_template to credential.json

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=<profile>
```
Profile can be dev,qa,staging, prod.
To execute it with debug mode :
```
mvn spring-boot:run -Dspring-boot.run.profiles=qa -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=0.0.0.0:7070"
```
Production will work on HTTPS and you need to generate keystore and add certificate to cacerts.
Local Environment

URL : localhost:8080/hkc

Credentials : admin/admin
## Usage
To get this up and running with your application, all you need is to get understanding of the api doing a single create/update operation which you wish to leverage for bulk operations, and log on to the admin console of hkconnector.
Before we configure our api we need to understand the following :
**Channel** : The application end point where your api is hosted.
**Bulk Configuration** : Here specify all the required information which is specific to the endpoint receiving the request and on saving bulk and process buttons are generated. You just need to copy paste that in your applications frontend.

Next on your application when user clicks the bulk button it will take to google permission grant page. Upon grant, a google sheet would be created with the column names defined as you mentioned while doing the bulk configuration on the admin page.
Fill in the required data and then copy paste the sheet url on the text box for process. On clicking the process button it will again ask for grant and upon grant hkconnector will hit the rest endpoint specified by you for each row in sheet and updating the response against those in sheet in column STATUS and Comment.

NOTE : 
- A sheet once processed can’t be processed again. New sheet need to be created.
- Also any change in the column name on sheet will mark sheet tampered.
- No manual sheet can be created. It has to be done via hkconnector only.


## Contributing
Intial setup by Arpit Harkawat for Healthkart.
Pull requests are welcome. For code or functional modification, please open an issue first to discuss what you would like to change. We will check for the incorporation.

## License
Owned by Healthkart.    