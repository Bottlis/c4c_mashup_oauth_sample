# c4c_mashup_oauth_sample
This sample code shows a concept of how to retrieve host and user information from C4C mashups, and use them in OAuth authentication for OData service calls from SCP apps.
The necessary enhancement of mashup framework is not available in C4C system yet, so this sample code will NOT work with existing C4C systems at this moment.

# Disclaimer
This sample code is not provided by SAP, and SAP is not responsible for the correctness, accuracy and up-to-date of this sample code. Please use or reference at hour own risk.


Please check https://github.com/SAP/C4CODATAAPIDEVGUIDE for more details regarding the setup of OAuth Client within C4C.


How to get host and user information from C4C mashup?
Take HTML mashup (Code) for example, 

1. use javascript to get the signed OAuthInfo from context sap.byd.ui.mashup.context.system.OAuthInfo
```javascript
        var sAuthInfo;
	try{
		sAuthInfo = sap.byd.ui.mashup.context.system.OAuthInfo;
	}catch(error){
		sAuthInfo = '';
	}
	var oOAuthInfo = JSON.parse(sAuthInfo);
	var payload = JSON.stringify(oOAuthInfo.OAuthInfo || {});			
	var signature = oOAuthInfo.signature || '';
	var url = "https://mymashupservicei035706trial.hanatrial.ondemand.com/MyMashupService/MyMashupServiceServlet";
			
	$.ajax({
		url : url,
		type : "POST",
		data : payload,
		beforeSend: function(request) {
		    request.setRequestHeader("x-c4c-signature", signature);
		},
	}).done(function(data) {
		//...
		//render the data
	}).fail(function() {
		//error handling
	}); 
```


2. parse OAuthInfo to get the payload and signature respectively
3. post the payload to your SCP apps, and put the signature into request header with name "x-c4c-signature"
4. in your SCP app, use the below sample code to verify the OAuthInfo,
```java
	String signature = request.getHeader("x-c4c-signature");
	String payload = getBody(request);
		
	AuthInfoProcessor processor = new AuthInfoProcessor(payload, signature);
	String host = processor.getHost();
	String user = processor.getUser();
	
	//user and host should be used to authenticate the user following OAuth flow
```


Please note!
The necessary cert and keystore files were not uploaded to this repository for security reasons, please follow the above mentioned  https://github.com/SAP/C4CODATAAPIDEVGUIDE to setup the needed artifacts for OAuth flow.
