## How to Import This Project into Eclipse

1. **Start Eclipse**
2. **File -> Import**
3. **Select Gradle -> Existing Gradle Project**
4. **Select the root folder of this project**
    - The correct root folder is the folder where this ReadMe file is stored.
5. **Press "Next", then "Finish"**
    - Wait till a) Gradle configuration and b) Gradle based library (dependency) download is finished.
    - An Internet connection is required for b).

## Description

This project provides a foundation for your own implementation of the Server. Import it in Eclipse to get an excellent
starting point. You will only need to add your own classes, code, and so on, as all the build management is
already configured nicely along with the integration of the provided network message implementation.

It is recommended that you also read the comments provided inside the classes. They give tips and help you understand
the provided example code. The latter code, e.g., provides a foundation to define the required endpoints similarly to
the given ones. Further, it contains a complete example and foundation to handle/signal business rule violations almost
entirely automatically.