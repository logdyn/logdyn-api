var loggingWebsocket = {
		
		websocket : null,
		
		init : function()
		{
			var origin = window.location.origin.replace("http://", "ws://").replace("https://","wss://");
			//TODO generate / find address rather than hardcode
			loggingWebsocket.websocket = new WebSocket(origin + "/logdyn/LoggingEndpoint");
			loggingWebsocket.websocket.onopen = function() 
			{
				//Sets up the logger instance with the correct session ID
				loggingWebsocket.websocket.send('{"httpSessionId":"' + sessionId + '"}');
			};
			
			loggingWebsocket.websocket.onmessage = function(message)
			{
				var jsonMessage = JSON.parse(message.data);			
				
				if (Array.isArray(jsonMessage))
				{
					for (i in jsonMessage)
					{
						loggingWebsocket.logLocalOnly(jsonMessage[i]);
					}
				}
				else
				{
					loggingWebsocket.logLocalOnly(jsonMessage);
				}
			};
		},
		
		logLocalOnly : function(logRecord)
		{
			logRecord.level = logRecord.level.toUpperCase();

			if (!logRecord.timestamp)
			{
				logRecord.timestamp = Date.now();
			}
			
			var func;
			if(typeof outputLog === "object")
			{
				outputLog.append(logRecord);
			}
			
			switch (logRecord.level) 
			{
				case 'INFO':
					func = console.info;
					break;
				case 'WARN':
				case 'WARNING':
					func = console.warn;
					break;
				case 'ERROR':
				case 'SEVERE':
					func = console.error;
					break;
				default:
					func = console.log;
			}
			
			func(logRecord.level + " : " + logRecord.message);
		},
		
		log : function(logRecord)
		{
			logLocalOnly(logRecord);
			loggingWebsocket.websocket.send(JSON.stringify(logRecord));
		},
		
		closeConnect : function()
		{
			loggingWebsocket.websocket.close();
		}
}

window.addEventListener('error', function(msg)
{
	loggingWebsocket.log({level:'ERROR', message:(msg.message || msg)});
});
document.addEventListener('DOMContentLoaded', loggingWebsocket.init, false);
