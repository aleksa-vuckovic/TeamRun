package com.example.runpal.models

/**
 * This is the class used for keeping and communicating run data.
 * It has 3 use cases:
 *  1. When sending and receiving updates, the path contains the latest (new) path points.
 *  2. When obtaining the data of the entire run, the path contains the entire run path
 *  3. When obtaining data of many runs, to display in a list, the path can be left empty.
 * The run field always contains the relevant run data, and the location field always contains
 * the latest (or final) location update. PathPoint.INIT is used for the initial run state.
 *
 * @param location Contains the latest or final location info about the run.
 * @param path Contains the entire path, the latest part of it, or nothing ,depending on the use case.
 */
class RunData(
    var run: Run = Run(),
    var location: PathPoint = PathPoint.INIT,
    var path: List<PathPoint> = listOf()
) {
}