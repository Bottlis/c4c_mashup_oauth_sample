# c4c_mashup_oauth_sample
This sample code shows a concept of how to retrieve host and user information from C4C mashups, and use them in OAuth authentication for OData service calls from SCP apps.
The necessary enhancement of mashup framework is not available in C4C system yet, so this sample code will NOT work with existing C4C systems at this moment.

# Disclaimer
This sample code is not provided by SAP, and SAP is not responsible for the correctness, accuracy and up-to-date of this sample code. Please use or reference at hour own risk.


Please check https://github.com/SAP/C4CODATAAPIDEVGUIDE for more details regarding the setup of OAuth Client within C4C.


How to get host and user information from C4C mashup?
Take HTML mashup (Code) for example,
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
