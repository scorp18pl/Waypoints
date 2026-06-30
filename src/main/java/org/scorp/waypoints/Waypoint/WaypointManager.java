package org.scorp.waypoints.Waypoint;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.scorp.waypoints.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

public class WaypointManager
{
  static final String WaypointsFileName = "waypoints.json";
  static private File dataFolder;

  static private boolean requiresDiscordUpdate = false;
  static private String host = "localhost";
  static private int port = 8888;

  static private ArrayList<Waypoint> waypoints = new ArrayList<>();

  public static void initializeDataFolder(File dataFolder)
  {
    WaypointManager.dataFolder = dataFolder;
    readWaypoints();
  }

  public static void initializeSocketAddress(String host, int port)
  {
    WaypointManager.host = host;
    WaypointManager.port = port;
  }

  public static Waypoint getWaypoint(String playerName,
                                     String waypointName) throws
      WaypointNotFoundException
  {
    for (Waypoint waypoint : waypoints)
    {
      if (waypoint.ownerName.equals(playerName) &&
          waypoint.waypointName.equals(waypointName))
      {
        return waypoint;
      }
    }

    throw new WaypointNotFoundException(waypointName);
  }

  public static Waypoint getVisibleWaypoint(String username,
                                            String waypointName) throws
      WaypointNotFoundException
  {
    // Prioritize user waypoints
    try
    {
      return getWaypoint(username, waypointName);
    } catch (WaypointNotFoundException exception)
    {
      ArrayList<Waypoint> publicWaypoints = getPublicWaypoints();
      for (Waypoint waypoint : publicWaypoints)
      {
        if (waypoint.waypointName.equals(waypointName))
        {
          return waypoint;
        }
      }

      throw new WaypointNotFoundException(waypointName);
    }
  }

  public static ArrayList<Waypoint> getVisibleWaypoints(String username)
  {
    return waypoints.stream().filter(
            waypoint -> waypoint.ownerName.equals(username) || waypoint.isPublic)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static ArrayList<Waypoint> getUserWaypoints(String userName)
  {
    return waypoints.stream()
        .filter(waypoint -> waypoint.ownerName.equals(userName))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static ArrayList<Waypoint> getPublicWaypoints()
  {
    return waypoints.stream().filter(waypoint -> waypoint.isPublic)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static void addWaypoint(Waypoint waypoint) throws
      WaypointNameExistsException
  {
    try
    {
      getWaypoint(waypoint.ownerName, waypoint.waypointName);
    } catch (WaypointNotFoundException e)
    {
      waypoints.add(waypoint);
      writeWaypoints();
      updateRequiresDiscordUpdate(waypoint.isPublic);
      return;
    }
    throw new WaypointNameExistsException(waypoint.waypointName);
  }

  public static void renameWaypoint(String owner, String oldName,
                                    String newName) throws
      WaypointNotFoundException, WaypointNameExistsException
  {
    try
    {
      getWaypoint(owner, newName);
    } catch (WaypointNotFoundException e)
    {
      Waypoint waypoint = getWaypoint(owner, oldName);
      waypoint.waypointName = newName;
      writeWaypoints();
      updateRequiresDiscordUpdate(waypoint.isPublic);
      return;
    }
    throw new WaypointNameExistsException(newName);
  }

  public static void setWaypointIsPublic(String owner, String waypointName,
                                         boolean isPublic) throws
      WaypointNotFoundException
  {
    Waypoint waypoint = getWaypoint(owner, waypointName);

    boolean requiresUpdate = waypoint.isPublic == isPublic;
    waypoint.isPublic = isPublic;
    writeWaypoints();
    updateRequiresDiscordUpdate(requiresUpdate);
  }

  public static void removeWaypoint(String owner, String name) throws
      WaypointNotFoundException
  {
    Waypoint waypoint = getWaypoint(owner, name);
    boolean isPublic = waypoint.isPublic;
    waypoints.remove(waypoint);
    writeWaypoints();
    updateRequiresDiscordUpdate(isPublic);
  }

  public static void onTimeElapsed()
  {
    if (!requiresDiscordUpdate)
    {
      return;
    }

    requiresDiscordUpdate = false;
    Utils.sendTCPMessage(host, port, getDiscordCsv());
  }

  private static void readWaypoints()
  {
    try
    {
      Gson gson = new Gson();
      FileReader reader = new FileReader(dataFolder + "/" + WaypointsFileName);
      waypoints = gson.fromJson(reader, new TypeToken<List<Waypoint>>()
      {
      }.getType());
      reader.close();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private static void writeWaypoints()
  {
    try
    {
      Gson gson = new Gson();
      FileWriter writer = new FileWriter(dataFolder + "/" + WaypointsFileName);
      gson.toJson(waypoints, writer);
      writer.close();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private static void updateRequiresDiscordUpdate(boolean condition)
  {
    requiresDiscordUpdate = requiresDiscordUpdate || condition;
  }

  private static String getDiscordCsv()
  {
    StringBuilder csv = new StringBuilder("owner,name,world,x,y,z\n");
    getPublicWaypoints().stream().sorted().forEach(
        (waypoint) -> csv.append(waypoint.ownerName).append(',')
            .append(waypoint.waypointName).append(',')
            .append(waypoint.worldName).append(',')
            .append(waypoint.x).append(',')
            .append(waypoint.y).append(',')
            .append(waypoint.z).append('\n'));

    return csv.toString();
  }
}
