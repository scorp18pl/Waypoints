package org.scorp.waypoints;

import org.bukkit.ChatColor;
import org.scorp.waypoints.Waypoint.Waypoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Utils
{
  public static String getSuccessString(String text)
  {
    return ChatColor.GREEN + text;
  }

  public static String getInfoString(String text)
  {
    return ChatColor.YELLOW + text;
  }

  public static String getErrorString(String text)
  {
    return ChatColor.RED + text;
  }

  public static ArrayList<String> waypointListToStringlist(
      ArrayList<Waypoint> waypoints)
  {
    return waypoints.stream().map(waypoint -> waypoint.waypointName)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static void sendTCPMessage(String serverAddress, int port,
                                    String message)
  {
    try (Socket socket = new Socket(serverAddress, port))
    {
      OutputStream out = socket.getOutputStream();
      out.write(message.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
