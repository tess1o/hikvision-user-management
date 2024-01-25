The application is used to take a picture from webcam and automatically create a user in HikVision.

Prerequisites:
* Java 11 must be installed 
* Update `application.conf` file:
    * `hikvision.server.url` - set it to HikVision server URL (including protocol http)
    * `hikvision.server.username` - username for HikVision (will be used to create new users)
    * `hikvision.server.password` - username's password for HikVision
    * `hikvision.cleanup.cron` - when to delete all users from HikVision (by default it's 19:30 each day)

To run the application you should execute: `java -jar hikvision-user-management-1.0-SNAPSHOT-jar-with-dependencies.jar` in your terminal (CMD)

The logic of the app
1. The app takes a default webcam from the machine where it's running
2. When the user preses a button the application does the following:
   * Takes screenshot from webcam
   * Tries to find all users in HikVision, sort them and finds the latest user (with maximum employee number)
   * Creates a user with `max employee number + 6` code
   * Uploads the photo from webcam
3. The app can show an error when:
   * HikVision does not recognize face on the image - the user has to try again
   * Some unexpected error (network issue, bug in application, etc)

The app logs everything in file `/logs/hikvision-user-management.log`, this file is mandatory for issue investigation