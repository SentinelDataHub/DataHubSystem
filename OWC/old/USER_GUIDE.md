# USER GUIDE

## Preface
This guide is oriented to anyone who wants to use the **OWC** application, the new Graphical User Interface for the DHuS, based on Google Polymer library.<br/>
This new GUI offers the same functionalities of the traditional one, with additional features devoted mainly to improve application configuration.
**OWC** development is still ongoing, so there will be both improvements on existing functionalities and new features implementation.<br/>
Furthermore, the **OWC** project is *Open Source*,  with the goal of creating a community of developers interested in implementing new web components suitable to the dissemination of space products.
Thanks to modularity and extensibility principles, components implemented by *Open Source* community can be dynamically imported in the OWC application on the basis of the users' needs.

## Prerequisites and Constraints
Since **OWC** application is based on Web Components standard, which is not supported yet on all browsers, OWC GUI works only with recent distribution of Google Chrome.

## Application Menu
**OWC** main page shows the application menu on a configurable background image.

![main labeled](https://cloud.githubusercontent.com/assets/10920750/25784024/a9fde4dc-3366-11e7-875e-4cfe3486954a.png)

All panels are displayed horizontally on the screen in the order they are requested by end users, clicking on the proper menu icon.<br/>
This navigation system choice makes it possible to present data efficiently and makes it easier for the user to navigate among components, giving also the chance to compare search results.<br/>
It is possible to close a panel clicking on the *close icon* on the top right-hand corner of each panel, while the *escape* key allows closing the last panel displayed on the right of the screen.<br/>
The default configuration of **OWC** application foresees the following menu options:
- Login panel, which allows users to:
  * perform login (if already registered);
  * subscribe to application (if not yet registered);
- User profile panel, which allows users to:
  * perform log-out;
  * access list of favourite products;
  * access list of saved searches;
- Search container, which provides:
  * full text search functionality;
  * product footprint visualization on map;
  * product list;
- Statistics container, which allows accessing the calendar of the *Product Availability*, reporting the publication density of products published on the DHuS with a sensing period covering the last 13 weeks.
- Client settings, which allows users to configure:
  * application language;
  * application theme;
  * the list of map layers visualized on search container map;
  * the list of Web Map Server, useful to project products quicklook on search container map;
  * product metadata to be shown in product list;
- About panel, showing general information about the application.

## Application access
Application access is free and allowed to all users registered to the Data Hub Service.<br/>
Already registered users can play with OWC GUI after performing log-in by means of *Login Panel*.<br/>
Not registered users can access OWC GUI after performing self-registration by means of *Signup Panel*.<br/>


### Login
*Login Panel* is displayed on the browser clicking on application menu *login icon* ![login_button](https://cloud.githubusercontent.com/assets/10920750/25742254/f0f7a6b8-318e-11e7-9176-a75021ee49d7.png)

![002](https://cloud.githubusercontent.com/assets/10920750/25740883/bf9f54b8-3188-11e7-90b1-9a54a191bcb8.png)

Registered users insert their username and password in the proper fields and then click on the *LOGIN* button.<br/>
If credentials are correctly inserted, a message *"Logged in Successfully"* appears in the bottom of the screen.<br/>
If credentials are not correctly inserted (i.e. wrong username or password) an error message appears on the *Login Panel* (*"The username and password you entered don't match."*)

### Signup
*Signup Panel* is displayed on the browser by clicking on the *SIGNUP* button of the *Login Panel*. This button is visible on all the DHuS instances on which self-registration to service is allowed.
![self-registration](https://cloud.githubusercontent.com/assets/10920750/25746623/a7f5cb32-31a4-11e7-918b-ea6c1fe105ac.png)


After filling all the fields of the registration form, the user has to click on the *REGISTER* button to send the registration request.<br/>
If the registration form is not filled correctly, wrong fields are marked in red to inform user about registration failure.<br/>
If the registration form is filled correctly, a message *"Registration successful: An email was sent to let you validate your registration."* appears in the bottom of the screen.<br/>
As reported in both *Signup Panel* and notification message, on completion of the registration process, new users will receive an e-mail with a link to validate the account. Clicking on the link reported in the e-mail, the account is enabled, so new users can start to play with the DHuS through **OWC** GUI.

Please note that the following special characters are not allowed for the generation of the username:

 &hyphen; & | ! ( ) { } [ ] ^ " ~ * ? : &#92; $ ' &lt;blank space&gt; <br/>

The registration process cannot be completed when:
- a field of the form is not filled in;
- the username contains a special character;
- the password is too short (minimum length: 8 characters);
- the password typed in the "Confirm Password" field is not identical to the one typed in the corresponding "Password" field;
- the e-mail typed in the "Confirm E-mail" field is not identical to the one typed in the corresponding "E-mail" field.

**Known registration issues:**

__Username already existing in the database__<br/>
If a username is already existing in the Data Hub database a new account with the same username cannot be created. In this case, a generic error is notified to the user.

__No e-mail for registration confirmation is sent to the user__<br/>
Registration e-mails are usually received within two minutes from registration. It is therefore suggested to try with a different username.

__User credentials are not sent via e-mail__<br/>
Current Data Hub system does not send e-mails to the user with the username and password defined during the registration process.
Please take care of writing down your credentials for future reference.

##  Search container
*Search Container* is displayed on the browser by clicking on application menu *search icon* ![search](https://cloud.githubusercontent.com/assets/10920750/25785394/87bb650c-337f-11e7-9404-02d0e2fdcf36.png)<br/>
It is possible to show 2 instances of search container at a time, allowing users to compare 2 searches results more easily.<br/>
If a user requests a new *Search Container* when there are already 2 containers displayed, a message appears in the bottom of the screen as notification that no more instances can be created for the specified component (*"Reached maximum number of component's instances"*).


The *Search Container* is composed by 3 panels, which are collapsible and expandable clicking on the icon reported in the top left-hand corner of each panel:
- *Search panel*
- *Product list*
- *Map*

![003](https://cloud.githubusercontent.com/assets/10920750/25806522/c1861924-3403-11e7-96b1-5c731aa82f57.png)

### Search panel
*Search panel* contains a **search bar** used to insert text query to perform.<br/>

![searchbar](https://cloud.githubusercontent.com/assets/10920750/25841108/2807b824-349f-11e7-8960-05b0460ffa77.png)

Search request is executed clicking on the search icon in the end of the search bar.

The text query displayed in the search bar can be saved in a list of user's favourite searches, clicking on *save icon* ![saveicon](https://cloud.githubusercontent.com/assets/10920750/25841330/f867ac72-349f-11e7-8d2e-abbb0e12c2e7.png).<br/>If the text query is saved successfully, a message appears in the bottom of the screen (*"User search saved successfully"*)

The text query displayed in the search bar can be also cleared, with all search filters, clicking on *clear icon* ![closeicon](https://cloud.githubusercontent.com/assets/10920750/25841126/37772ac4-349f-11e7-8e83-f475faa9a2b4.png).<br/>

Some text queries examples that can be typed in the full-text search bar, with available wildcards, operators and keywords, can be found at the following link: https://scihub.copernicus.eu/userguide/3FullTextSearch

#### Advanced Search
The *Search panel* contains a section with sort and ordering criteria and with different search fields.<br/>

Available sort criteria are:
- **sort by product index**, meaning that search results are displayed on the basis of the value of the selected index. Available indexes are:
  - Ingestion date, i.e. product publication date;
  - Sensing date, i.e. product acquisition date;
  - Cloud Coverage, i.e. product cloudiness percentage.
- **ascending or descending order**, which, combined with the sorting criteria, allows to show search results:
  - from the more recent to the older one (*DESCENDING*) or viceversa (*ASCENDING*) in case of temporal index (e.g. Ingestion date or Sensing Date);
  - from the greater to the smaller value (*DESCENDING*) or viceversa (*ASCENDING*) in case of numeric index (e.g. Cloud Coverage).

In this section it's also possible to specify a date range for the following parameters:
- **Ingestion Date**, in order to get all products published in the defined period. With regard to Ingestion time, the following convention is used:
  * Publication time equal or greater than 00:00:00 (hh:mm:ss) of the first selected date;
  * Publication time equal or less than 23:59:59 (hh:mm:ss) of the second selected date;
- **Sensing Date**, in order to get all the products whose sensing dates are included in the defined period. With regard to sensing time, the following convention is used:
  * Sensing start time equal or greater than 00:00:00 (hh:mm:ss) of the first selected date;
  * Sensing stop time equal or less than 23:59:59 (hh:mm:ss) of the second selected date.

For both *Ingestion Date* and *Sensing Date* search criteria, the following rule is applied:
- if only the first date is specified, the query will return all products with Ingestion/Sensing Date equal or greater than  00:00:00 (hh:mm:ss) of the specified date;
- if only the second date is specified, the query will return all products with Ingestion/Sensing Date equal or less than 23:59:59 (hh:mm:ss) of the specified date;
- if both first and second date are specified, the query will return all products with Ingestion/Sensing Date equal or greater than  00:00:00 (hh:mm:ss) of the first date and Ingestion/Sensing Date equal or less than 23:59:59 (hh:mm:ss) of the second date.

The date can be selected clicking on *calendar icon* ![calendaricon](https://cloud.githubusercontent.com/assets/10920750/25841125/3774499e-349f-11e7-9335-9764b7afc577.png).<br/>
The date can be cleared clicking on *clear date icon* ![clearcaledaricon](https://cloud.githubusercontent.com/assets/10920750/25841127/377792b6-349f-11e7-9eac-857d9b106129.png).

These search fields can be used singularly or in combination with:
- the full-text search bar content;
- a selected geographic area.

### Product list
The *Product list* provides all the products matching the submitted search query. Each result consists of:
- product thumbnail, when available;
- product main attributes, which are configurable by users on the basis of their needs. The configuration of product attributes is detailed in [List settings](#link0001) section;
- a set of available actions, such as:
  - open product details panel, clicking on ![show](https://cloud.githubusercontent.com/assets/10920750/25785356/8c127e66-337e-11e7-9bd0-33f791a94fdb.png) icon reported in list item;
  - download a product, clicking on ![download](https://cloud.githubusercontent.com/assets/10920750/25785352/721f96ec-337e-11e7-827d-ba0fa32135b9.png) icon reported in list item;
  - open auxiliary list panel, clicking on ![adf](https://cloud.githubusercontent.com/assets/10920750/25785334/2db839b4-337e-11e7-96bd-50a1e0900f4d.png)icon reported in list item;
  - add or remove product to/from user cart, clicking on the *star icon* ![staricon](https://cloud.githubusercontent.com/assets/10920750/25841129/37793d32-349f-11e7-8a12-56b9af4098e0.png) shown in list item. If *star icon* is black coloured, this means that the product is not present in the user cart, so, clicking on the icon, the product is added to the user cart. If *star icon* is blue coloured, this means that the product is  present in the user cart, so, clicking on the icon, the product is removed from user cart;
  - add or remove product quicklooks to/from map, if there is a configured Web Map Server exposing product quicklooks as layer, clicking on the following icon ![wmsgreyicon](https://cloud.githubusercontent.com/assets/10920750/25841130/377ea542-349f-11e7-9464-bd87599b64dd.png).


This component implements an *"infinite scroll"*. This means that after submitting a query, the list is populated with a number of products equals to a pre-defined page size (which is fixed to 25), but, as user scrolls product list, a new search is performed, according to the following rule:
- if user scrolls down, a next product page (if any) is loaded;
- if user scrolls up, a previous product page (if any) is loaded.

#### Product details
*Product details* panel is shown clicking on ![show](https://cloud.githubusercontent.com/assets/10920750/25785356/8c127e66-337e-11e7-9bd0-33f791a94fdb.png) icon in the product list.

![006](https://cloud.githubusercontent.com/assets/10920750/25808667/19c1611e-340b-11e7-8b19-26acebac94b7.png)

This panel contains:
- full product name;
- link for product download;
- product quicklook, if any;
- list of product metadata, listed in alphabetical order and grouped by category.

#### Auxiliary File list
*Auxiliary File list* panel is shown clicking on ![adf](https://cloud.githubusercontent.com/assets/10920750/25785334/2db839b4-337e-11e7-96bd-50a1e0900f4d.png) icon reported in list item.

![007](https://cloud.githubusercontent.com/assets/10920750/25808748/4e3fe942-340b-11e7-937d-2ca63a9b9074.png)

This panel contains the list of Auxiliary Data File used to process a product, obtained by means of product inspection via OData.<br/>
At present this feature is available only for Sentinel-1 and Sentinel-3 products.


### Map
*Map* section of *Search Container* shows footprints of the products matching the search query. <br/>
 NOTE: the map shows only the footprints of the products in the current search list page. To widen results, users have to scroll product list in order to trigger new product request.

This component contains a set of tool useful to:
- define a region of interest, drawing a bounding box on the map. This feature is enabled if *draw region icon* ![selector](https://cloud.githubusercontent.com/assets/10920750/25811087/5f25a3e4-3412-11e7-95c1-baa13a19d832.png) is visible on the map. If *drag icon* ![pan](https://cloud.githubusercontent.com/assets/10920750/25811081/5aaed7ae-3412-11e7-8ca4-9d929bc8662d.png) is visible instead, click on it to enable drawing feature.
- drag map, by means of mouse click.  This feature is enabled if *drag icon* ![pan](https://cloud.githubusercontent.com/assets/10920750/25811081/5aaed7ae-3412-11e7-8ca4-9d929bc8662d.png) is visible on the map. If *draw region icon* ![selector](https://cloud.githubusercontent.com/assets/10920750/25811087/5f25a3e4-3412-11e7-95c1-baa13a19d832.png) is visible instead, click on it to enable dragging feature.
- select or deselect available map layers, making them visible on not visible on the map. The list of available map layers is shown clicking on *map layers icon* ![layerswitcher](https://cloud.githubusercontent.com/assets/10920750/25811078/57d47ec6-3412-11e7-9f85-96c230c681e7.png).


##  User Profile
*User Profile* panel is displayed on the browser clicking on application menu *user icon*, which is visible only after performing login ![usericon](https://cloud.githubusercontent.com/assets/10920750/25783839/1fffa174-3363-11e7-8ab8-b8a624eb0fe5.png).<br/>

![015](https://cloud.githubusercontent.com/assets/10920750/25805611/9ebdd268-3400-11e7-8d96-adcd76c8ee73.png)

This panel allows user to:
- log-out from application, by clicking on *LOGOUT* button;
- access list of favourite products, by clicking on *PINNED LIST* button;
- access list of user's saved searches, by clicking on *SAVED SEARCHES* button.  



### Cart (Pinned List)
The *Cart Panel* shows the list of user's favourite products. <br/>
From this panel it is possible to:
- download a metalink file containing Internet locations of all the products in the cart, by clicking on ![bulk](https://cloud.githubusercontent.com/assets/10920750/25785342/46667386-337e-11e7-8ade-3aa6e41b2711.png) icon reported in the top of the panel. This metalink file, handled by a download manager, allows a bulk download of products saved in user cart
- remove all products from cart, clicking on ![clearcart](https://cloud.githubusercontent.com/assets/10920750/25785346/5a9dbea4-337e-11e7-8548-3a28cb20922e.png);
 icon reported in the top of the panel
- remove a single product from cart, clicking on ![star](https://cloud.githubusercontent.com/assets/10920750/25785359/a15a6360-337e-11e7-8e82-d2bccef36e8c.png) icon reported in the top right-hand corner of list item;
- open product details panel, clicking on ![show](https://cloud.githubusercontent.com/assets/10920750/25785356/8c127e66-337e-11e7-9bd0-33f791a94fdb.png) icon reported in list item;
- download a single product, clicking on ![download](https://cloud.githubusercontent.com/assets/10920750/25785352/721f96ec-337e-11e7-827d-ba0fa32135b9.png) icon reported in list item;
- open auxiliary list panel, clicking on ![adf](https://cloud.githubusercontent.com/assets/10920750/25785334/2db839b4-337e-11e7-96bd-50a1e0900f4d.png)
 icon reported in list item.

![cart labelled](https://cloud.githubusercontent.com/assets/10920750/25785020/22e71f98-3377-11e7-9b5b-ecbade2dbe23.png)
### Saved Searches
The *Saved Searches Panel* shows the list of user's saved searches. <br/>

![016](https://cloud.githubusercontent.com/assets/10920750/25805732/05eb6298-3401-11e7-97b6-fdc8607caa98.png)


From this panel is possible to:
- execute a saved search, by clicking on the *search icon* ![search](https://cloud.githubusercontent.com/assets/10920750/25785394/87bb650c-337f-11e7-9404-02d0e2fdcf36.png). This action implies that a *Search container* is displayed on the screen, showing saved search results, if any, in the product list and in the map.
- activate or deactivate notification on updates related to a saved search. This operation is performed clicking on the *e-mail icon* ![emailicon](https://cloud.githubusercontent.com/assets/10920750/25842500/83b2fc74-34a4-11e7-9eb3-757db5127f95.png) reported in the list item
- delete a saved search, if no more useful, by clicking on the *delete icon* ![deleteicon](https://cloud.githubusercontent.com/assets/10920750/25842501/85a11d18-34a4-11e7-8085-a29406ecd82d.png) shown in the list item.

It is also possible to perform a bulk deletion of saved searches, clicking on *CLEAR SAVED SEARCHES* button displayed in the bottom right-hand corner of the panel.

## Statistics container
*Statistics container* is displayed on the browser clicking on application menu *statistics icon* ![statisticsicon](https://cloud.githubusercontent.com/assets/10920750/25785404/b89b15e6-337f-11e7-9665-d6cfc4f011cb.png)

This panel contains link to access the following components:
- Product density calendar, showing at a glance the publication density of products published on the DHuS with a sensing period covering the last 13 weeks;
- Events list, where events are intended as relevant information about:
  -	Wrong data production (due to satellite unavailability, anomaly on PDGS etc..),
  -	Data production restoring,
  -	Reprocessing baseline,
  -	New missions availability,
  -	New features implementation,
  -	Publication of a new OSF version of the SW,
  -	Publication of a new annual report.


### Product density calendar
*Product density calendar* panel spans the previous 3-month (91 days) sensing period for the satellite and reports, for each sensing date on the calendar, the number of products which have been published on the corresponding Data Hub instance from that sensing date.

![calendar](https://cloud.githubusercontent.com/assets/10920750/25809735/425ab8d4-340e-11e7-9840-81de92fa8730.png)

The user can click on a coloured day of the calendar to get the list of products sensed that day. This product list is based on the same component used for *Search container* product list, so it is based on *infinite scroll* too and provides the same feature of the previous one, i.e.:
- show product details panel;
- download product;
- show auxiliary list panel;
- add or remove product to/from user cart;
- add or remove product quicklooks to/from map.


![calendar detail](https://cloud.githubusercontent.com/assets/10920750/25809702/1ffd6642-340e-11e7-9a9f-c684bc52f641.png)


The colour shading in the calendar indicates the publication density for each day, i.e. the darkest days in the calendar are the sensing dates on which the greatest number of products were published.
Where the publication density has been affected by an event, a grey triangle will be visible in the top left-hand corner of the day tile.

### Events list

*Events list* panel contains the list of relevant events impacting products dissemination.
<br/>This list is based on *infinite scroll*, so it's populated with a number of events equal to a pre-defined page size (which is fixed to 25), but, as user scrolls events list, a new search is performed, according to the following rule:
- if user scrolls down, a next event page (if any) is loaded;
- if user scrolls up, a previous event page (if any) is loaded.

 Each list item contains:
 - event title;
 - event icon, if any;
 - event description, which is partially shown if too long;
 - an icon, allowing to open *Event Details* panel.

#### Event Details
![stats_details](https://cloud.githubusercontent.com/assets/10920750/26068529/18eecd56-399e-11e7-8f35-5d80096ed2e5.png)

##  Client settings
*Client settings* panel is displayed on the browser clicking on application menu *settings icon* ![settingsicon](https://cloud.githubusercontent.com/assets/10920750/25785397/9f602454-337f-11e7-975f-1896e2dcf889.png).

This panel allows users to configure:
* application language;
* application theme;
* the list of map layers visualized on search container map;
* the list of Web Map Server, useful to project products quicklook on search container map;
* product metadata to be shown in product list.

### Application language
User can choose application language, selecting one option from the *Language* combo and then clicking on *save icon*.

Available languages are:
- Italian;
- English;
- French.

![language](https://cloud.githubusercontent.com/assets/10920750/25809560/a7b1a220-340d-11e7-8a3d-fb724e57edb1.png)

Once the preferred language has been configured, it is necessary to reload the **OWC** application to make the settings effective.

### Application theme

*Theme Editor* is displayed on the screen by clicking on the *Theme* button reported in the *Client settings* panel.

![theme](https://cloud.githubusercontent.com/assets/10920750/25809686/1237fc2a-340e-11e7-8c0c-b4a1ef771301.png)

Using this editor it is possible to configure:
- application title, which is shown in the application main menu;
- application logo, adding an url with logo information in the *Logo Url* input text. Please note that:
  - *Logo Url* must be such that it can be reached via browser;
  -  chosen logo should be square to be displayed properly in the application menu;
- application background color, using theme editor color selector;
- background image, adding an url with image information in the *Background Image Url* input text. Please note that:
  - *Background Image Url* must be such that it can be reached via browser;
  -  chosen image should be great enough to be displayed properly as background.

Theme settings are saved on browser local storage by clicking on the *SAVE* button.

Users can restore default theme settings by clicking on the *RESTORE DEFAULT* button.

It is necessary to reload the **OWC** application to make the settings effective.

### Map settings

*Map settings* is displayed on the screen by clicking on the *Map settings* button reported in the *Client settings* panel.

![map_settings](https://cloud.githubusercontent.com/assets/10920750/25816393/46f42cde-3424-11e7-924a-9eb5d1911287.png)

This panel allows configuring the list of map layers which can be added in the application map, by specifing, for each layer:
- if the layer is visible or not in the *Search container* map;
- HTTP url of the web map server exposing the layer of interest;
- a title, i.e. an identifier of the layer, which is shown in the list of available map layers when clicking on *map layers icon* ![layerswitcher](https://cloud.githubusercontent.com/assets/10920750/25811078/57d47ec6-3412-11e7-9f85-96c230c681e7.png);
- layers name separated by comma (e.g. name1, name2, name3).

This configuration is saved on the browser local storage clicking on the *SAVE* button.

It is possible to add a layer by clicking on the *add icon* reported in the top left-hand corner of the panel.

It is possible to remove a layer by clicking on the *deletion icon* reported in the top right-hand corner of each layer section.


### WMS settings

*WMS settings* is displayed on the screen clicking on the *WMS settings* button reported in the *Client settings* panel.

![wms_settings](https://cloud.githubusercontent.com/assets/10920750/25809838/952ab41a-340e-11e7-9b8b-b8599fa779af.png)

This panel allows configuring the list of WMS useful to show product quicklook in the application map.
For each WMS, it is necessary to specify the following parameters:
- a title, i.e. an identifier of the Web Map Server;
- HTTP url of the web map server exposing layers of interest;
- a username of the account used to access the Web Map Server, if needed;
- a password of the account used to access the Web Map Server, if needed;
- a regular expression which can be useful for avoiding performing too many requests towards WMS (e.g. **S3A_OL_1** rule allows performing requests only for Sentinel-3 A Level 2 OLCI products ).

This configuration is saved in the browser local storage by clicking on the *SAVE* button.

It is possible to add a Web Map Server by clicking on the *add icon* reported in the top left-hand corner of the panel.

It is possible to remove a Web Map Server by clicking on the *deletion icon* reported in the top right-hand corner of each WMS section.



### <a name="link0001"></a> List settings

*List settings* is displayed on the screen by clicking on the *List settings* button reported in the *Client settings* panel.

![metadata](https://cloud.githubusercontent.com/assets/10920750/25809693/18ec6006-340e-11e7-83d2-10e093bc0188.png)

This panel allows editing the schema used for the mapping between data model retrieved from the server when performing a query and information shown in the *Product list*.<br/>
At this stage of the **OWC** implementation, it is necessary to know the data model coming from the server to be able to make changes visible on the products list.

This configuration is saved in the browser local storage by clicking on the *SAVE* button.
<br/>Users can restore default list settings by clicking on *RESTORE DEFAULT* button.

Some examples are provided below to give indication about how to proceed with this configuration.

**Add title to list item**

This example shows how to add a title in the list item.
<br/>Add the following code in *“model”* section
```
{
    "@id": "title",
    "@type": "String",
    "valueExtractor": {
        "relativePath": "identifier"
    }
}
```
Click on the *SAVE* button and perform new search to see the result.

![listsettings_ex1](https://cloud.githubusercontent.com/assets/10920750/25815673/d5f53944-3421-11e7-9ca2-74ddd2214931.png)

**Add a new attribute to list item**

This example shows how to add a new attribute “Satellite” that is taken from the model starting from the “indexes” array searching inside it the key "summary" that returns an array of Children's from which we extract "Satellite".

From product model:

```
[-,- {
	"id": 1683253,
	"uuid": "2eb3f85a-a2a7-4f5a-b16a-c6fdd88816be",
	.
	.
	.
	.

	"indexes": [{
		"name": "product",
		"children": [{
			"name": "Acquisition Type",
			"value": "NOMINAL"
		},
		.
		.
		.
		.
		]
	}, {
		"name": "summary",
		"children": [{
			"name": "Date",
			"value": "2017-02-10T07:45:45.878Z"
		}, {
			"name": "Filename",
			"value": "S1B_IW_GRDH_1SDV_20170210T074545_20170210T074612_004235_007579_811E.SAFE"
		}, {
			"name": "Identifier",
			"value": "S1B_IW_GRDH_1SDV_20170210T074545_20170210T074612_004235_007579_811E"
		}, {
			"name": "Instrument",
			"value": "SAR-C"
		}, {
			"name": "Mode",
			"value": "IW"
		}, {
			"name": "Satellite",
			"value": "Sentinel-1"
		}, {
			"name": "Size",
			"value": "1.68 GB"
		}]
	},
	.
	.
	.
	.
	],
	.
	.
	.
	.
},

```
In order to make visible the "Satellite" attribute in the product list, add this code snippet in *“attributes”* section

```
{
                        "@id": "Satellite",
                        "@type": "Object",
                        "valueExtractor": {
                            "@type": "Array",
                            "relativePath": "indexes",
                            "findWhere": {
                                "name": "summary"
                            },
                            "valueExtractor": {
                                "@type": "Array",
                                "relativePath": "children",
                                "findWhere": {
                                    "name": "Satellite"
                                },
                                "valueExtractor": {
                                    "relativePath": "value"
                                }
                            }
                        }
                    }

```
Click on the *SAVE* button and perform new search to see the result.

![listsettings_ex2](https://cloud.githubusercontent.com/assets/10920750/25815927/b4055ec6-3422-11e7-83e6-f9e009c8c47f.png)

## About
the *About panel* is shown on the screen by clicking on application logo.

![about - labelled](https://cloud.githubusercontent.com/assets/10920750/25747631/6e176f16-31a8-11e7-9421-4e727420db1a.png)


This panel shows general information about the DHuS, software version and partners' logos.

