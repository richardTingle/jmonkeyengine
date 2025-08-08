# jme3-screenshot-tests

This module contains tests that compare screenshots of the JME3 test applications to reference images. Think of these like visual unit tests

## Running the tests

### Running locally (directly)

The tests are run using the following command:

```
 ./gradlew :jme3-screenshot-test:screenshotTest
```

This will create a report in `jme3-screenshot-test/build/reports/ScreenshotDiffReport.html` that shows the differences between the reference images and the screenshots taken during the test run. Note that this is an ExtentReport. 

### Running locally (using Docker)

To run the tests in a stable environment that matches the CI server, you can use Docker:

1. Make sure you have Docker and Docker Compose installed
2. Navigate to the jme3-screenshot-tests directory
3. Run the following command:

```
docker-compose up
```

This will run the tests in a Docker container using the same environment as the CI server. The report will be created in the same location as when running directly.

> **Note for Windows users**: The Docker setup automatically handles line ending conversion for the gradlew script. Windows uses CRLF line endings, but Linux (used in the Docker container) requires LF line endings. Even though there are separate scripts for different operating systems (gradlew.bat for Windows and gradlew for Linux), when running in Docker on Windows, the Linux script (gradlew) is used inside the container but might have Windows-style line endings if checked out on a Windows system. The docker-compose.yml file includes commands to fix this automatically.

### Running on CI

This is most reliable when run on the CI server. The report can be downloaded from the artifacts section of the pipeline (once the full pipeline has completed). If you go into
the Actions tab (on GitHub) and find your pipeline you can download the report from the Artifacts section. It will be called screenshot-test-report.

## Machine variability

It is important to be aware that the tests are sensitive to machine variability. Different GPUs may produce subtly different pixel outputs
(that look identical to a human user). The tests are run on a specific machine and the reference images are generated on that machine. If the tests are run on a different machine, the images may not match the reference images and this is "fine". If you run these on your local machine compare the differences by eye in the report, don't wory about failing tests.

## Parameterised tests

By default, the tests use the class and method name to produce the screenshot image name. E.g. org.jmonkeyengine.screenshottests.effects.TestExplosionEffect.testExplosionEffect_f15.png is the testExplosionEffect test at frame 15. If you are using parameterised tests this won't work (as all the tests have the same function name). In this case you should specify the image name (including whatever parameterised information to make it unique). E.g.

```
    screenshotTest(
        ....
    ).setFramesToTakeScreenshotsOn(45)
    .setBaseImageFileName("some_unique_name_" + theParameterGivenToTest)
    .run();
)
```

## Non-deterministic (and known bad) tests

By default, screenshot variability will cause the pipeline to fail. If a test is non-deterministic (e.g. includes randomness) or 
is a known accepted failure (that will be fixed "at some point" but not now) that can be non-desirable. In that case you can 
change the behaviour of the test such that these are marked as warnings in the generated report but don't fail the test

```
    screenshotTest(
        ....
    ).setFramesToTakeScreenshotsOn(45)
    .setTestType(TestType.NON_DETERMINISTIC)
    .run();
)
```

## Accepting new images

It may be the case that a change makes an improvement to the library (or the test is entirely new) and the new image should be accepted as the new reference image. To do this, copy the new image to the `src/test/resources` directory. The new image can be found in the `build/changed-images` directory, however it is very important that the image come from the reference machine. This can be obtained from the CI server. The job runs only if there is an active pull request (to one of the mainline branches; e.g. master or 3.7). If you go into the Actions tab and find your pipeline you can download the report and changed images from the Artifacts section.
