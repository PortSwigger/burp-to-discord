# burp-to-discord

burp-to-discord listens to all issues reported (from **any** source) and then sends the issue's details off to a discord webhook. 

I build it to use in combination with long-running research scans from tools build using BulkScan however it'll work just fine with burp's regular scanner.

Instructions for setup are included in your burp settings but to summarize:
1) Create a Discord webhook and paste the URL into your settings
2) Grab user Discord user ID and paste that into your settings
3) Profit...

FAQ:
1) Why is the configuration a project setting rather than a user setting?

    This is to avoid notifications being sent that contain sensitive information. For example in my research file, nothing is sensitive. But if I have a work-project and accidentally enable this extension, I don't want my settings from other files to suddenly result in me leaking sensitive info to Discord.
2) My request / response got cut-off in the Discord message

    Discord webhooks have a max size. To bypass  this limit, the issues arrive in multiple messages, but even then, if a request or response is too large it will be truncated to prevent errors. 