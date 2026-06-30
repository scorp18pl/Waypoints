package org.scorp.waypoints.Waypoint;

import org.scorp.waypoints.command.InvalidCommandException;

public class WaypointBadWorldException extends Exception
{
  public WaypointBadWorldException(String waypointName, String worldName)
  {
    super("Waypoint with name " + waypointName + " exists in a different world (" + worldName + ").");
  }
}
