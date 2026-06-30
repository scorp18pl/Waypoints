package org.scorp.waypoints.command.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.scorp.waypoints.InternalErrorException;
import org.scorp.waypoints.Utils;
import org.scorp.waypoints.Waypoint.Waypoint;
import org.scorp.waypoints.Waypoint.WaypointBadWorldException;
import org.scorp.waypoints.Waypoint.WaypointManager;
import org.scorp.waypoints.Waypoint.WaypointNameExistsException;
import org.scorp.waypoints.Waypoint.WaypointNotFoundException;
import org.scorp.waypoints.command.CommandUtils;
import org.scorp.waypoints.command.InvalidCommandException;
import org.scorp.waypoints.command.PlayerOnlyCommandException;

import java.util.List;

public class PointSubCommand implements SubCommand
{
  @Override
  public String getName()
  {
    return "point";
  }

  @Override
  public String getUsageMessage()
  {
    return "/waypoint point <waypointName>\n" +
        " * <waypointName> - (Required) " +
        CommandUtils.getAlreadyExistRequirementString();
  }

  @Override
  public List<Integer> getPossibleArgCounts()
  {
    return List.of(1);
  }

  @Override
  public void onCommand(CommandSender sender, String[] args) throws
      InvalidCommandException, WaypointNotFoundException, WaypointBadWorldException
  {
    if (!(sender instanceof Player player))
    {
      throw new PlayerOnlyCommandException();
    }

    String waypointName = args[0];

    Waypoint waypoint = WaypointManager.getVisibleWaypoint(player.getName(), waypointName);
    if (player.getLocation().getWorld().getName().compareTo(waypoint.worldName) != 0)
    {
      throw new WaypointBadWorldException(waypointName, waypoint.worldName);
    }

    Vector pXyz = new Vector(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    Vector wpXyz = waypoint.getXyz();

    Location newPlayerLocation = player.getLocation();
    newPlayerLocation.setDirection(wpXyz.clone().subtract(pXyz));

    player.teleport(newPlayerLocation);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, int argIndex) throws
      PlayerOnlyCommandException
  {
    if (!(sender instanceof Player player))
    {
      throw new PlayerOnlyCommandException();
    }

    return switch (argIndex)
    {
      case 0 -> Utils.waypointListToStringlist(
          WaypointManager.getVisibleWaypoints(player.getName()));
      default -> throw new InternalErrorException();
    };
  }
}
