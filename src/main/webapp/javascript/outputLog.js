var outputLog = 
{
	
	isHover : false,
		
	init : function()
	{
		var logElements = Array.from(document
				.getElementsByClassName("logElement"));
		logElements.forEach(function(log) 
		{
			log.addEventListener("mouseover", function(){outputLog.isHover = true});
			log.addEventListener("mouseout", function(){outputLog.isHover = false});
            Array.from(log.parentElement.getElementsByTagName("input")).forEach(function(input)
            {
                if(input.type === "checkbox")
                {
                    input.addEventListener("change", outputLog.toggleLevelVisible);
                }
                else if(input.type === "search")
                {
                    input.addEventListener("input", outputLog.filterEvent);
                }
            });
            Array.from(log.parentElement.getElementsByTagName("button")).forEach(function(button)
            {
                button.addEventListener("click", outputLog.clear, true);//currenty only button is for clearing
            });
		});
	},
	
	append : function(logRecord) 
	{
		var logElements = Array.from(document
				.getElementsByClassName("logElement"));
		logElements.forEach(function(log) 
		{
			var levelClass;
            var date = new Date(logRecord.timestamp);
            var timeHTML = '<span class="timestamp"><time datetime=' + date.toISOString() + ' title="' + date.toISOString() + '">' + date.toLocaleTimeString() + '</time> : </span>';
			var iconHTML = '<span class="glyphicon';
			
			switch (logRecord.level) 
			{
				case 'FINE':
					levelClass = "text-success";
					break;
				case 'INFO':
					levelClass = "text-info";
					iconHTML += " glyphicon-info-sign";
					break;
				case 'WARN':
				case 'WARNING':
					iconHTML += " glyphicon-alert";
					levelClass = "text-warning";
					break;
				case 'ERROR':
				case 'SEVERE':
					iconHTML += " glyphicon-remove-sign";
					levelClass = "text-danger";
					break;
				default:
					levelClass = "text-" + logRecord.level.toLowerCase();
			}
			
			iconHTML += '"></span>';
			var messageHTML = '<samp class="' + levelClass + '">' + iconHTML + timeHTML + logRecord.level + " : " +  outputLog.escapeHtml(logRecord.message) + '<br/></samp>';
			log.innerHTML += messageHTML;
			
			if (!outputLog.isHover)
			{
				log.scrollTop = log.scrollHeight;
			}
            outputLog.filterEvent({target : log.parentElement.querySelector('input[type="search"]')});
		});
	},
    
    escapeHtml : function(unsafe)
    {
        return unsafe
             .replace(/&/g, "&amp;")
             .replace(/</g, "&lt;")
             .replace(/>/g, "&gt;")
             .replace(/"/g, "&quot;")
             .replace(/'/g, "&#039;");
    },
    
    toggleLevelVisible : function(event)
    {
        var target = event.target;
        target.parentElement.parentElement.nextElementSibling.classList.toggle(target.name, !target.checked);
    },
    
    filterEvent : function(event)
    {
        const timestampRegEx = /<span class="timestamp">.*?<\/span>/;
        const filterRegEx = new RegExp(event.target.value, 'gi');
        const logElements = event.target.parentElement.parentElement.nextElementSibling;
        Array.from(logElements.getElementsByTagName('samp')).forEach(function(samp)
        {
            const timeStamp = samp.innerHTML.match(timestampRegEx)[0];
            const searchableText = samp.innerHTML.substr(samp.innerHTML.search(timestampRegEx) + timeStamp.length);
            const cleanSearchableText = searchableText.replace(/<[\/\w\d]*?>/g, "");
            const filtered = cleanSearchableText.search(filterRegEx) == -1
            samp.classList.toggle('filtered', filtered);
            if(filtered || !event.target.value)
            {
                samp.innerHTML = samp.innerHTML.replace(searchableText, cleanSearchableText);
            }
            else
            {
                samp.innerHTML = samp.innerHTML.replace(searchableText, cleanSearchableText.replace(filterRegEx, "<mark>$&</mark>"));
            }
            samp.innerHTML += "</br>";
        });
    },
    
    clear: function(event)
    {
        event.target.parentElement.parentElement.parentElement.querySelector('.logElement').innerHTML='';
    }
}

document.addEventListener('DOMContentLoaded', outputLog.init, false);
