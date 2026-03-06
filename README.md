
# iROM

## Overview

iROM is an Android application designed to facilitate wrist movement analysis using IMUs.
The app enables users to record videos of patients performing wrist tasks using a tablet and IMUs, collect relevant measurement data, and securely send this data to a server for processing and analysis. A key feature of the project is its integration with Python scripts, which handle the core data processing and analysis. By modifying the Python files, it’s easy to extend or adjust the app’s functionality, making the system highly flexible and adaptable to different research or clinical needs.

## Features

- **Patient Authentication**: Login system to access specific patient and case id.
- **Movela Dot Sensor Data Scan and Connection**: Scan nearby sensors, connect and sync them.
- **Video Recording**: Capture high-quality videos of patients walking using the tablet's camera.
- **Sensor Data Collection**: Collect sensor data from the tablet during recording sessions.
- **Movela Dot Sensor Data Collection**: Collect data from connected movella dot sensors during recording sessions.
- **Session Management**: Organize and manage multiple recording sessions for each patient.
- **Data Transmission**: Securely upload recorded videos and sensor data to a remote server for further analysis.

## Prerequisites

- Android tablet running Android 9.0 (Marshmallow) or higher.
- Internet connection for data transmission.
- Permissions for Camera, Microphone, Bluetooth, and Storage.

## Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/xinyaoict/IMU_Python_APP

2. Open the project in Android Studio.
3. Build the project and run it on your Android tablet.
   
## Usage

### Login

1. Open the app on your tablet.
2. Enter the patient's credentials and log in.

### Scan, Connect and Sync Sensors (if needed)

1. Click on the button "Scan" to scan for nearby sensors.
2. When displayed click on the scanned sensors to connect to them.
3. Once all the needed sensors are connected and green click on the button "Sync" to sync them together.
4. Once synced, click on start measurement session to access measurements.

### Record Data
1. Click on a measurement button to start a recording measurement.
2. Click on the button "Start Recording" to start the recording and "Stop recording" to stop it.
3. Do as many measurements as needed. 3 is adviced.
4. Once done, click on "End Measurement Session"
5. Repeat to do as many measurement sessions as wanted with different sensors configurations.

### Send Data

1. Once all the measurement sessions are complete, click the "Send Data" button.
2. If there is recorded data, a confirmation dialog will appear asking if you are sure you want to send the data. Confirm to proceed.
3. The data will be securely uploaded to the server.
4. Upon successful upload, you will be logged out automatically.

## Code Structure

- '**Activities**': Contains all the activity classes (e.g., LoginActivity, MenuActivity).
- '**Utils**': Utility classes for various functionalities (e.g., FileManager, DialogUtils).
- '**Models**': Data models representing different entities (e.g., PatientInfo).
- '**Adapters**': Custom adapters for handling data in views.



## Permissions

The app requires the following permissions:

 - '**CAMERA**': To capture video recordings.
 - '**RECORD_AUDIO**': To record audio during sessions.
 - '**BlUETOOTH**': To handle sensors.
 - '**WRITE_EXTERNAL_STORAGE**': To save recorded videos and data on the device.
 - '**READ_EXTERNAL_STORAGE**' To read saved data for uploading.
   
## Troubleshooting

### Common Issues
- **Sensor Issues**: If anything goes wrong with the sensors or you have trouble connecting them try to turn them off and restart them.
- **Recording Issues**: Check if the camera and microphone permissions are granted.
- **Data Upload Failure**: Ensure the server is reachable and you have a stable internet connection.
  
### Logs

Enable logging to capture detailed information for debugging purposes. Logs can be viewed in the Logcat window in Android Studio.

## Contributing

1. Fork the repository.
2. Create a new branch (git checkout -b feature/your-feature).
3. Make your changes and commit them (git commit -m 'Add your feature').
4. Push to the branch (git push origin feature/your-feature).
5. Create a new Pull Request.

## License
TODO

## Contact
TODO


## TODO
### When SuperWalk has been tested and approved 
- Implement the file uploading of SuperWalk
 
