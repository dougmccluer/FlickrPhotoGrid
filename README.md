# notes
- There's room to reduce recompositions.  Currently state gets updated every time scroll position changes.  But you know what they say about premature optimization... :)  
- If you keep scrolling forever and ever, eventually the list of photos is going to get really big. Might consider something that prunes the list when it grows beyond a certian size.
- The error handling is pretty ugly.  Right now it takes over the entire screen, but in an intermittent failure secenario, it might be better to to display a message without interrupting the user's flow.
- The "bonus points" sections (detail view, unit tests) are a little quick and dirty.  
- Multiple modules is probably overkill in such a smll project, but in a hypothetical universe where this would be scaled up into a much large app, the modules will help maintain separation of conerns and promote short incremental build times.  As new features get added, we'd probably want to move them into separate modules too...
