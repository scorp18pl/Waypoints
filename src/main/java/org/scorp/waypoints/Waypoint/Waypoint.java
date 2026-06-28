package org.scorp.waypoints.Waypoint;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class Waypoint implements
    Comparable<Waypoint>
{
  public String ownerName, waypointName, worldName;
  int x, y, z;
  boolean isPublic;

  public Waypoint(Location location, String ownerName, String waypointName)
  {
    this(location, ownerName, waypointName, false);
  }

  public Waypoint(Location location, String ownerName, String waypointName,
                  Boolean isPublic)
  {
    this.ownerName = ownerName;
    this.waypointName = waypointName;
    this.worldName = location.getWorld().getName();

    x = location.getBlockX();
    y = location.getBlockY();
    z = location.getBlockZ();

    this.isPublic = isPublic;
  }

  public boolean isPublic()
  {
    return isPublic;
  }

  public Vector getXyz()
  {
    return new Vector(x, y, z);
  }

  @Override
  public String toString()
  {
    return waypointName + " ( " + worldName + " )" + ": " + "(" + x + ", " +
        y + ", " + z + ")";
  }

  public String toMcString()
  {
    return ChatColor.BOLD + "" + ChatColor.YELLOW + waypointName +
        ChatColor.RESET + " ( " +
        ChatColor.BOLD + "" + ChatColor.YELLOW + worldName + ChatColor.RESET +
        " )" + ": " + "(" +
        ChatColor.RED + x + ChatColor.WHITE +
        ", " +
        ChatColor.GREEN + y + ChatColor.WHITE + ", " + ChatColor.AQUA + z +
        ChatColor.RESET + ")";
  }

  @Override
  public int compareTo(@NotNull Waypoint o)
  {
    int compareOwner = this.ownerName.compareTo(o.ownerName);
    if (compareOwner != 0)
    {
      return compareOwner;
    }

    int compareWorld = this.worldName.compareTo(o.worldName);
    if (compareWorld != 0)
    {
      return compareWorld;
    }

    return this.waypointName.compareTo(o.waypointName);
  }
}
