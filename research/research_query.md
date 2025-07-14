https://bidyut-pixel-ruler.en.aptoide.com/app

I am working on an application that requires similar functionality to this referenced app. I need the source code to implement this app with one exception, I need 7 lines on the screen in landscape mode, not just the two displace in this pixel ruler.

Look for the source code to implement this app and provide sources and code to create the android app.

1.  must be open source to share with other people
2. Framework can be any language as long as it can be sideloaded to an android app
3. lines must be user moveable as they need to measure items on the screen
4. Yes user must be able to drag the lines, lines must be numbered, with a button area for the number and the user to move the line.
5. User MUST be able to interact with any other application on the phone while the rulers are visible

[media pointer="file-service://file-6CXMBhDnkQ5EdA8SFCaR6u"]
[media pointer="file-service://file-YJb9p3DTu15KtGcK3XhDC6"]
[media pointer="file-service://file-YKKGJm3bmNBgxzv4UCYANN"]
[media pointer="file-service://file-CDwKnKm41vd6nGE262sNVW"]
lets revisit this app with more detail and develop a more comprehensive plan for this application.  here are the app the requirements:

User will take a reference image of an object that has 7 items that need measurements of the height of the 7 items.  they open that image adjust 7 lines to match the heights of the items, then they switch to the app of interest with those 7 lines still remaining overlayed on the screen to align with the contents of the app.  basically a reference image with heights, then a calculated image to see if those heights correspond, correlate with the overlay lines

the reference image and measuring apps are completely closed code and the app owners are not willing or able to add new features or development.

workflow:

user takes reference image with tool. image is save.  user then opens the overlay grid app with 7 lines, they adjust the 7 measurements with the reference image, keeping the line app open with the lines overlayed on the screen the user opens the measurement app they collects the new measurements.  they can then interpret the results with the lines overlaying the measurement screen.  they can leave the overlay app on allow complete use of the application.

the measurement app runs in horizontal mode of the screen as it has the most screen area for left to right measurement and view of the 7 items.  The app must have 7 lines, each line can be 7 distinct colors that are bight enough for a variety of backgrounds, most of the measurements and reference images re black settings about want good contrast of the app.

they user must be able to move the 7 lines vertical up and down when holding the phone horizontally.  when the phone is in portrait mode or vertical the lines remain vertical. when rotated back to the horizontal mode they lines remain. 

The overlay line app must run overlayed on the screen while allowing all other app and phone functions underneath.  the line adjustments must be moveable with slide of the line number using touch and finder adjustments.  the colors are 7 distinct.  we do NOT need lines left to right in the portrait mode, only the horizontal mode left to right

review the chat history, review the internet search.

Then create a complete project plan to develop and android app that implements this user functionality.  this must include all functionality, features, code examples, and steps to develop a fully functioning android application.

the current app the user is using this app at this link, read it throughly and navigate its pages to understand what it can do: 
https://bidyut-pixel-ruler.en.aptoide.com/app
See attached photos of the app.  they use the app, ignoring the portrait horizontal likes they only use the ones that are left to right when the phone is horizontal (landscape mode).
If we could get the source code for this app and delete the left to right lines in portrait mode and made 7 lines top to bottom lines in portrait mode this will solve their problem!
I downloaded their apk file, not sure if it can be modified
you can use your code tool calls to look at the apk to see what the code looks like to make updates and suggestions and use the photos to understand the requirements.

Ask me any questions to clarify for understanding to ensure the app project plan is comprehensive and complete.

1. android mobile devices
2. yes the 7 line positions must be persistent when closing and reopening the app
3. yes screenshot and export
4. lines 1 through 7 with one being at the top 7 at the bottom when in landscape mode and the line orders must not be able drag over another.  for example line. can not be dragged above line 6
5. the reference image would overlap the current measurement view, they only need the measurement heights, hence the overlay app to measure the hight in the reference measurement, then transferring to new measurement for comparison with these 7 lines