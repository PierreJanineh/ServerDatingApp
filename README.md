# ServerDatingApp

## About the Project

A Java server that communicates with [iOS](https://github.com/PierreJanineh/iOSDatingApp) and [Android](https://github.com/PierreJanineh/AndroidDatingApp) Apps and manages data in a MySQL database, both on a virtual instance. The Server is still under construction!

The dating app and server are currently under development. Note that some changes (such as database schema modifications) are not backwards compatible and may cause the app to crach. In the case, please uninstall and reinstall the app.

## Libraries used
* Architecture and Data management
  * [Threads](https://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html)
  * [MySQL Connector](https://dev.mysql.com/doc/connector-j/8.0/en/)
* Third party libraries
  * [Gson](https://github.com/google/gson)

## Other Technologies
* MySQL Server
* Google Cloud Console - Compute Engine
  Used to run the App server and MySQL server on a virtual machine to enable mobile devices communications with the server.
  
## Upcoming features
### Updates will include:
* Login/Signup
