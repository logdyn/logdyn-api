var loggingWebsocket = {
		
		websocket : null,
		
		init : function()
		{
			var origin = window.location.origin.replace("http://", "ws://").replace("https://","wss://");
			//TODO generate / find address rather than hardcode
			loggingWebsocket.websocket = new WebSocket(origin + "/logdyn/LoggingEndpoint");
			
			loggingWebsocket.websocket.onopen = function() 
			{
				
			};
			
			loggingWebsocket.websocket.onmessage = function(message)
			{
				var jsonMessage = JSON.parse(message.data);

				if(Array.isArray(jsonMessage))
				{
					jsonMessage.forEach(function (message)
					{
						loggingWebsocket.logLocalOnly(message);
                    });
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
			loggingWebsocket.logLocalOnly(logRecord);
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
